package com.nowcoder.community.controller;

import com.nowcoder.community.annotation.LoginRequired;
import com.nowcoder.community.entity.User;
import com.nowcoder.community.service.FollowService;
import com.nowcoder.community.service.LikeService;
import com.nowcoder.community.service.UserService;
import com.nowcoder.community.util.CommunityConstant;
import com.nowcoder.community.util.CommunityUtil;
import com.nowcoder.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController {
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;
    @Value("${community.path.domain}")
    private String domain;
    @Value("${server.servlet.context-path}")
    private String contextPath;
    @Autowired
    private UserService userService;
    @Autowired
    private LikeService likeService;
    @Autowired
    private HostHolder hostHolder;
    @Autowired
    private FollowService followService;
    @LoginRequired
    @RequestMapping(path = "/setting", method = RequestMethod.GET)
    public  String getSettingPage(){
        return "/site/setting";
    }
    @LoginRequired
    @RequestMapping(path ="/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        if(headerImage==null){
            model.addAttribute("error","没有选择图片");
            return "/site/setting";
        }
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式异常");
            return "/site/setting";
        }
        //生成随机文件名
        fileName = CommunityUtil.generateUUID()+suffix;
        File dest = new File(uploadPath+"/"+fileName);
        try {
            //存文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败"+e.getMessage());
            throw new RuntimeException("上传文件失败");
        }
        //更新头像文件路径(web路径)
        User user = hostHolder.getUser();
        String headUrl = domain+contextPath+"/user/header/"+fileName;
        userService.updateHeader(user.getId(), headUrl);
        return "redirect:/index";


    }

    @RequestMapping(path="/header/{filename}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("filename") String fileName, HttpServletResponse response){
        //服务器存放路径名
        fileName = uploadPath+"/"+fileName;
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        response.setContentType("image/"+suffix);
        try (OutputStream os = response.getOutputStream();
             FileInputStream fis = new FileInputStream(fileName);
             ){

            byte[] buffer = new byte[2048];
            int b = 0;
            while((b=fis.read(buffer))!=-1){
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取图像失败");
        }
    }

    @RequestMapping(path="/changePw",method = RequestMethod.POST)
    public String changePw(String password, String password1, String password2, Model model){
        User user = hostHolder.getUser();
        if(StringUtils.isBlank(password)){
            model.addAttribute("passwordMsg","原密码不能为空");
            return "/site/setting";
        }
        if(!user.getPassword().equals(CommunityUtil.md5(password+user.getSalt()))){
            model.addAttribute("passwordMsg","原密码不正确");
            return "/site/setting";
        }

        if(StringUtils.isBlank(password1)){
            model.addAttribute("password1Msg","新密码不能为空");
            return "/site/setting";
        }
        if(StringUtils.isBlank(password2)){
            model.addAttribute("password2Msg","请再输入一次密码");
            return "/site/setting";
        }
        if (!password1.equals(password2)){
            model.addAttribute("password1Msg","两次密码不一致");
            return "/site/setting";
        }
        if(user.getPassword().equals(CommunityUtil.md5(password2+user.getSalt()))){
            model.addAttribute("password1Msg","不能与原密码一致");
            return "/site/setting";
        }
        String newpassword = CommunityUtil.md5(password1+user.getSalt());
        userService.updatePassword(user.getId(),newpassword);
        return "redirect:/index";

    }
    //个人主页
    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId, Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("用户不存在");

        }
        //用户信息
        model.addAttribute("user",user);
        //点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        //关注数量
        long followeeCount = followService.findFolloweeCount(userId,3);
        model.addAttribute("followeeCount",followeeCount);
        long followerCount = followService.findFollowerCount(3,userId);
        model.addAttribute("followerCount",followerCount);
        boolean hasFollowed = false;
        if(hostHolder.getUser()!=null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),3,userId);

        }
        model.addAttribute("hasFollowed",hasFollowed);
        //粉丝数量
        //是否已关注

        return "/site/profile";
    }
}
