package com.nowcoder.community.event;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nowcoder.community.entity.DiscussPost;
import com.nowcoder.community.entity.Event;
import com.nowcoder.community.entity.Message;
import com.nowcoder.community.service.DiscussPostService;
import com.nowcoder.community.service.ElasticsearchService;
import com.nowcoder.community.service.MessageService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.Region;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

@Component
public class EventConsumer implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(EventConsumer.class);
    @Autowired
    private MessageService messageService;
    @Autowired
    private DiscussPostService discussPostService;

    @Value("${qiniu.key.access}")
    private String accessKey;
    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;
    @Value("${qiniu.key.secret}")
    private String secretKey;
    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;
    @Value("${qiniu.bucket.share.url}")
    private String shareBucketUrl;
    @Value("${qiniu.bucket.share.name}")
    private String shareBucketName;
    @Autowired
    private ThreadPoolTaskScheduler taskScheduler;

    @Value("${wk.image.command}")
    private String wkCommand;

    @Value("${wk.image.storage}")
    private String wkImageStorage;
    @Autowired
    private ElasticsearchService elasticsearchService;
    @KafkaListener(topics = {TOPIC_COMMENT,TOPIC_FOLLOW,TOPIC_LIKE})
    public void handleCommentMessage(ConsumerRecord record){
        if(record==null || record.value()==null){
            logger.error("内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if(event==null){
            logger.error("消息格式错误");
            return;
        }
        //发送通知
        Message message=new Message();
        message.setFromId(SYSTEM_USERID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());

        Map<String, Object> content = new HashMap<>();
        content.put("userId",event.getUserId());
        content.put("entityType",event.getEntityType());
        content.put("entityId", event.getEntityId());

        if(!event.getData().isEmpty()){
            for(Map.Entry<String, Object> entry: event.getData().entrySet()){
                content.put(entry.getKey(),entry.getValue());
            }
        }
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }

    //消费发帖事件
    @KafkaListener(topics = {TOPIC_PUBLISH})
    public void handlePublishMessage(ConsumerRecord record){
        if(record==null || record.value()==null){
            logger.error("内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if(event==null){
            logger.error("消息格式错误");
            return;
        }
        DiscussPost post = discussPostService.findDiscussPostById(event.getEntityId());
        elasticsearchService.saveDiscussPost(post);
    }
    @KafkaListener(topics = {TOPIC_DELETE})
    public void handleDeleteMessage(ConsumerRecord record){
        if(record==null || record.value()==null){
            logger.error("内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if(event==null){
            logger.error("消息格式错误");
            return;
        }
        elasticsearchService.deleteDiscussPost(event.getEntityId());
    }


    @KafkaListener(topics = {TOPIC_SHARE})
    public void handleShareMessage(ConsumerRecord record){
        if(record==null || record.value()==null){
            logger.error("内容为空");
            return;
        }
        Event event = JSONObject.parseObject(record.value().toString(),Event.class);
        if(event==null){
            logger.error("消息格式错误");
            return;
        }
        String htmlUrl = (String) event.getData().get("htmlUrl");
        String fileName = (String) event.getData().get("fileName");
        String suffix = (String) event.getData().get("suffix");
        String cmd = wkCommand+" --quality 10 "+htmlUrl+" "+wkImageStorage
                +"/"+fileName+suffix;

        try {
            Runtime.getRuntime().exec(cmd);
            logger.info("生成长图成功"+cmd);
        } catch (IOException e) {
            logger.error("生成长图失败："+e.getStackTrace());
        }
        //发送通知
        //定时器 等exec执行完毕 监视图片是否生成
        UploadTask task = new UploadTask(fileName,suffix);
        Future future = taskScheduler.scheduleAtFixedRate(task, 500);
        task.setFuture(future);

    }
    class UploadTask implements Runnable{
        //文件名称
        private String fileName;
        //文件后缀
        private String suffix;
        private Future future;
        //开始时间
        private long startTime;
        private int uploadTimes;

        public UploadTask(String fileName, String suffix) {
            this.fileName=fileName;
            this.suffix=suffix;
            this.startTime=System.currentTimeMillis();
        }

        @Override
        public void run() {
            //生成失败
            if(System.currentTimeMillis()-startTime>=30000){
                logger.error("执行时间过长，停止："+fileName);
                future.cancel(true);
                return;
            }
            //上传失败
            if(uploadTimes>=3){
                logger.error("执行次数过多，停止："+fileName);
                future.cancel(true);
                return;
            }
            String path = wkImageStorage+"/"+fileName+suffix;
            File file = new File(path);
            if(file.exists()){

                logger.info(String.format("开始第%d次上传[%s].",++uploadTimes,fileName));
                StringMap policy = new StringMap();
                policy.put("returnBody", CommunityUtil.getJSONString(0));
                Auth auth = Auth.create(accessKey,secretKey);
                String uploadToken = auth.uploadToken(shareBucketName,fileName,3600*24,policy);
                UploadManager manager = new UploadManager(new Configuration(Zone.zone0()));
                try{
                    Response response = manager.put(
                            path, fileName,uploadToken,null,"image/"+suffix.substring(1),false
                    );
                    JSONObject json = JSONObject.parseObject(response.bodyString());
                    if(json == null || json.get("code")==null|| !json.get("code").toString().equals("0")){
                        logger.info(String.format("第%d次上传失败[%s]",uploadTimes,fileName));
                    }else {
                        logger.info(String.format("第%d次上传成功[%s]",uploadTimes,fileName));
                        future.cancel(true);
                    }
                }catch (QiniuException e){
                    logger.info(String.format("第%d次上传失败[%s]",uploadTimes,fileName));

                }
            }else {
                logger.info("等待图片生成");
            }

        }

        public void setFuture(Future future) {
            this.future = future;
        }
    }
}
