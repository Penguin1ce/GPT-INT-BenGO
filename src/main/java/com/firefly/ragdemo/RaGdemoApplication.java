package com.firefly.ragdemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.firefly.ragdemo.mapper")
public class RaGdemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(RaGdemoApplication.class, args);
    }

}
