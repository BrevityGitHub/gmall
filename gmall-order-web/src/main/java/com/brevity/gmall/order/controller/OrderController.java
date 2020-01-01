package com.brevity.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.brevity.gmall.bean.UserAddress;
import com.brevity.gmall.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController {

    // @Autowired  启动后会报找不到的异常，不在同一个项目，端口也不一样
    @Reference // 使用dubbo的注解
    private UserService userService;

    @RequestMapping("tradeByUserId")
    public List<UserAddress> trade(String userId) {
        return userService.getUserAddressByUserId(userId);
    }

    @RequestMapping("trade")
    public List<UserAddress> getUserAddress(UserAddress userAddress) {
        return userService.getUserAddressByUserId(userAddress);
    }
}
