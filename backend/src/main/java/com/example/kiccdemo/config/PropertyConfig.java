package com.example.kiccdemo.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AppProperties.class)
/**
 * @ConfigurationProperties 클래스(AppProperties)를 스프링 빈으로 등록합니다.
 */
public class PropertyConfig {
}
