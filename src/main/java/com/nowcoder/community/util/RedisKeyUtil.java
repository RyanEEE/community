package com.nowcoder.community.util;

public class RedisKeyUtil {
    private  static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";

    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_UV="uv";
    private static final String PREFIX_DAU="dau";
    private static final String PREFIX_POST="post";
    //like:entity:entityType:entityId -> set(userId)
    public static String getEntityLikeKey(int entityType, int entityId){

        return PREFIX_ENTITY_LIKE+SPLIT+entityType+SPLIT+entityId;
    }
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE+SPLIT+userId;
    }

    //followee:userId:entityType  ->  zset(entityId, now )
    public static String getFolloweeKey(int userId, int entityType){
        return PREFIX_FOLLOWEE+SPLIT+userId+SPLIT+entityType;
    }

    //follower:entityType:entityId  ->  zset(userId, now )
    public static String getFollowerKey(int entityId, int entityType){
        return PREFIX_FOLLOWER+SPLIT+entityType+SPLIT+entityId;
    }

    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA+SPLIT+owner;

    }
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET+SPLIT+ticket;
    }
    public static String getUserKey(int userId){
        return PREFIX_USER+SPLIT+userId;
    }


    public static String getUvKey(String date) {
        return PREFIX_UV+SPLIT+date;
    }
    //区间UV
    public static String getUvKey(String startDate, String endDate){
        return PREFIX_UV+SPLIT+startDate+SPLIT+endDate;
    }
    //日活
    public static String getDauKey(String date) {
        return PREFIX_DAU+SPLIT+date;
    }
    public static String getDauKey(String startDate, String endDate) {
        return PREFIX_DAU+SPLIT+startDate+SPLIT+endDate;
    }

    public static String getPostKey() {
        return PREFIX_POST+SPLIT+"score";
    }
}
