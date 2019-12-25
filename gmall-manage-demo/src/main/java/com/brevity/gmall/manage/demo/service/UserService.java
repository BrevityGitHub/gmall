package com.brevity.gmall.manage.demo.service;

import com.brevity.gmall.manage.demo.bean.UserInfo;

import java.util.List;

public interface UserService {
    // 查询所有用户信息
    List<UserInfo> findAll();
}
