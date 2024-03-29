package com.brevity.gmall.list;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.brevity.gmall")
public class GmallListServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallListServiceApplication.class, args);
    }
}
