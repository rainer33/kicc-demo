package com.example.kiccdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KiccDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(KiccDemoApplication.class, args);
    }
}
