package com.example.kiccdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
/**
 * 애플리케이션 진입점이며 스케줄러 기능을 활성화해 백그라운드 보정 작업을 함께 실행합니다.
 */
public class KiccDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(KiccDemoApplication.class, args);
    }
}
