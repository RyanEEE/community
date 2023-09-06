package com.nowcoder.community.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Controller;

@Configuration
public class RedisConfig {
    @Autowired
    RedisTemplate template;
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        //设置key序列化方式
        template.setKeySerializer(RedisSerializer.string());
        //value 序列化
        template.setValueSerializer(RedisSerializer.json());
        //hash的key
        template.setHashKeySerializer(RedisSerializer.json());
        //hash的value
        template.setHashValueSerializer(RedisSerializer.json());
        return template;
    }
    private void testTransaction(){
        Object obj = template.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations redisOperations) throws DataAccessException {
                String redisKey = "text:tx";
                redisOperations.multi();
                redisOperations.opsForSet().add(redisKey, "张三");
                return redisOperations.exec();
            }
        });
    }
}
