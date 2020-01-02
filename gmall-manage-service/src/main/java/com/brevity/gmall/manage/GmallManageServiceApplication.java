package com.brevity.gmall.manage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableTransactionManagement
@ComponentScan(basePackages = "com.brevity.gmall")
@MapperScan(basePackages = "com.brevity.gmall.manage.mapper")
public class GmallManageServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(GmallManageServiceApplication.class, args);
    }
}
