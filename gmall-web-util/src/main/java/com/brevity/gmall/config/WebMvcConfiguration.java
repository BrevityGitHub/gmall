package com.brevity.gmall.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration // 等价于WebMvcConfiguration.xml
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

    @Autowired // 获取自定义拦截器
    private AuthInterceptor authInterceptor;

    public void addInterceptors(InterceptorRegistry registry) {
        // 设置自定义拦截器拦截的请求，拦截所有请求
        registry.addInterceptor(authInterceptor).addPathPatterns("/**");
        // 将拦截器放入拦截器栈
        super.addInterceptors(registry);
    }
}
