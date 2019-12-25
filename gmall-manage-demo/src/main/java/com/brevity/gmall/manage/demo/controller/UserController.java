package com.brevity.gmall.manage.demo.controller;

import com.brevity.gmall.manage.demo.bean.UserInfo;
import com.brevity.gmall.manage.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {
    // 控制器调用服务层，注入service
    @Autowired
    private UserService userService;

    @RequestMapping("findAll")
    public List<UserInfo> findAll() {
        return userService.findAll();
    }
}
