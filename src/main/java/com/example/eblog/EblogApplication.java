package com.example.eblog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EblogApplication {

	public static void main(String[] args) {

		// 解决elasticsearch启动保存问题
		// 包冲突问题
		System.setProperty("es.set.netty.runtime.available.processors", "false");

		SpringApplication.run(EblogApplication.class, args);
	}

}
