package com.myAI.myAI;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.myAI.myAI.mapper")
@SpringBootApplication
public class MyAiApplication {

	public static void main(String[] args) {
		SpringApplication.run(MyAiApplication.class, args);
	}

}
