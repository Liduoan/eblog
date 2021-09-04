package com.example.eblog.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate();
        template.setConnectionFactory(redisConnectionFactory);

        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        jackson2JsonRedisSerializer.setObjectMapper(new ObjectMapper());
        //redis的键进行序列化 是String
        template.setKeySerializer(new StringRedisSerializer());
        //对值进行序列化
        template.setValueSerializer(jackson2JsonRedisSerializer);
        //对模板的Hash键进行序列化
        template.setHashKeySerializer(new StringRedisSerializer());
        //对模板的值进行序列化
        template.setHashValueSerializer(jackson2JsonRedisSerializer);
        //返回模板
        return template;
    }

}
