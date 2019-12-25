package com.brevity.gmall.manage.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
// 扫描指定包下的mapper文件
@MapperScan(basePackages = "com.brevity.gmall.manage.demo.mapper")
public class GmallManageDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallManageDemoApplication.class, args);
    }

}
