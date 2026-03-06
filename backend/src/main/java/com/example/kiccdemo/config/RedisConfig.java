package com.example.kiccdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.util.StringUtils;

/**
 * Redis 연결을 애플리케이션 설정(app.redis)으로 고정합니다.
 *
 * 목적:
 * - bootRun 시 Docker Compose 서비스 연결 자동 주입보다
 *   명시적인 계정/비밀번호 설정을 우선 적용해 인증 실패를 방지합니다.
 */
@Configuration
public class RedisConfig {

    @Bean
    public LettuceConnectionFactory redisConnectionFactory(AppProperties appProperties) {
        AppProperties.Redis redis = appProperties.getRedis();
        RedisStandaloneConfiguration configuration =
                new RedisStandaloneConfiguration(redis.getHost(), redis.getPort());

        if (StringUtils.hasText(redis.getUsername())) {
            configuration.setUsername(redis.getUsername());
        }
        if (StringUtils.hasText(redis.getPassword())) {
            configuration.setPassword(RedisPassword.of(redis.getPassword()));
        }
        return new LettuceConnectionFactory(configuration);
    }
}
