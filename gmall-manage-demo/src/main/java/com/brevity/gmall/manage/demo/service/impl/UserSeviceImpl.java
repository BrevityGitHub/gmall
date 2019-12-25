package com.brevity.gmall.manage.demo.service.impl;

import com.brevity.gmall.manage.demo.bean.UserInfo;
import com.brevity.gmall.manage.demo.mapper.UserInfoMapper;
import com.brevity.gmall.manage.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserSeviceImpl implements UserService {

    // 服务层调用数据访问层，注入mapper
    @Autowired // 自动注入，从容器中获取
    private UserInfoMapper userInfoMapper;

    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }
}
