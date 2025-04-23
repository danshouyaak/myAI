package com.myAI.myAI;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.CrossOrigin;

//@CrossOrigin(origins = "http://192.168.43.1:5173/", allowCredentials = "true")
@MapperScan("com.myAI.myAI.mapper")
@SpringBootApplication(scanBasePackages = "com.myAI.myAI")
@EnableScheduling
public class MyAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyAiApplication.class, args);
    }
}
