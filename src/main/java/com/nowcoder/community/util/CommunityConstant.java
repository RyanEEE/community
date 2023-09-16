package com.nowcoder.community.util;

import java.security.cert.CertificateExpiredException;

public interface CommunityConstant {
    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;
    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;
    /**
     * 激活失败
     */
    int ACTIVATION_FAILURE = 2;
    /**
     * 登录超时时间
     */
    int DEFAULT_EXPIRED_SECONDS=3600*12;

    int REMEMBER_EXPIRED_SECONDS=3600*24*100;

    //实体类型 帖子
    int ENTITY_TYPE_POST=1;
    //评论
    int ENTITY_TYPE_COMMENT=2;

    /**
     * 实体类型: 用户
     */
    int ENTITY_TYPE_USER = 3;

    //评论topic
    String TOPIC_COMMENT="comment";
    //点赞
    String TOPIC_LIKE="like";
    //关注
    String TOPIC_FOLLOW="follow";

    int SYSTEM_USERID=1;
    //发帖常量
    String TOPIC_PUBLISH="publish";

    //普通用户
    String AUTHORITY_USER="user";
    //管理员
    String AUTHORITY_ADMIN="admin";
    //版主
    String AUTHORITY_MODERATOR="moderator";

}
