package com.brevity.gmall.user.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.brevity.gmall.bean.UserAddress;
import com.brevity.gmall.bean.UserInfo;
import com.brevity.gmall.service.UserService;
import com.brevity.gmall.user.manage.mapper.UserAddressMapper;
import com.brevity.gmall.user.manage.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service // 使用的是dubbo的注解
public class UserServiceImpl implements UserService {

    //调用mapper层
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserAddressMapper userAddressMapper;

    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public List<UserInfo> findUserInfoByProperty(UserInfo userInfo) {
        return null;
    }

    @Override
    public List<UserInfo> findByRange(UserInfo userInfo) {
        return null;
    }

    @Override
    public void addUser(UserInfo userInfo) {

    }

    @Override
    public void deleteUser(UserInfo userInfo) {

    }

    @Override
    public void updateUser(UserInfo userInfo) {

    }

    @Override
    public List<UserAddress> getUserAddressByUserId(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        return userAddressMapper.select(userAddress);
    }

    @Override
    public List<UserAddress> getUserAddressByUserId(UserAddress userAddress) {
        return userAddressMapper.select(userAddress);
    }
}
