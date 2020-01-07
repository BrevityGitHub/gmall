package com.brevity.gmall.service;

import com.brevity.gmall.bean.UserAddress;
import com.brevity.gmall.bean.UserInfo;

import java.util.List;

public interface UserService {

    // 查询所有用户信息
    List<UserInfo> findAll();

    // 根据用户的name或者loginName等字段查询数据
    List<UserInfo> findUserInfoByProperty(UserInfo userInfo);

    // 区间范围查询
    List<UserInfo> findByRange(UserInfo userInfo);

    // 添加用户
    void addUser(UserInfo userInfo);

    // 删除用户
    void deleteUser(UserInfo userInfo);

    // 更新用户
    void updateUser(UserInfo userInfo);

    /**
     * 根据用户id获取用户地址列表
     *
     * @param userId
     * @return
     */
    List<UserAddress> getUserAddressByUserId(String userId);

    List<UserAddress> getUserAddressByUserId(UserAddress userAddress);

    UserInfo login(UserInfo userInfo);

    /**
     * 根据用户Id获取缓存中的用户对象
     *
     * @param userId
     * @return
     */
    UserInfo verify(String userId);
}
