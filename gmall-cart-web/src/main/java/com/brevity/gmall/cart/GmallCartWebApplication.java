package com.brevity.gmall.cart;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.brevity.gmall")
public class GmallCartWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(GmallCartWebApplication.class, args);
    }
}
