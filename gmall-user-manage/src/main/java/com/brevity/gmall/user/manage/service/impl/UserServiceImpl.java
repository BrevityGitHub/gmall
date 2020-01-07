package com.brevity.gmall.user.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.brevity.gmall.bean.UserAddress;
import com.brevity.gmall.bean.UserInfo;
import com.brevity.gmall.config.RedisUtil;
import com.brevity.gmall.service.UserService;
import com.brevity.gmall.user.manage.mapper.UserAddressMapper;
import com.brevity.gmall.user.manage.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service // 使用的是dubbo的注解
public class UserServiceImpl implements UserService {

    public String userKey_prefix = "user:";
    public String userinfoKey_suffix = ":info";
    public int userKey_timeOut = 60 * 60 * 24;

    //调用mapper层
    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private UserAddressMapper userAddressMapper;
    @Autowired
    private RedisUtil redisUtil;

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

    @Override
    public UserInfo login(UserInfo userInfo) {
        String passwd = userInfo.getPasswd();
        // 加密密码
        String newPwd = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfo.setPasswd(newPwd);
        UserInfo info = userInfoMapper.selectOne(userInfo);
        if (info != null) {
            // 放入redis中
            Jedis jedis = redisUtil.getJedis();
            String userKey = userKey_prefix + info.getId() + userinfoKey_suffix;
            // 设置用户信息在缓存的过期时间
            jedis.setex(userKey, userKey_timeOut, JSON.toJSONString(info));
            jedis.close();
            return info;
        }
        return null;
    }

    @Override
    public UserInfo verify(String userId) {
        Jedis jedis = redisUtil.getJedis();
        // 定义key user:userId:info
        String userKey = userKey_prefix + userId + userinfoKey_suffix;
        String userJson = jedis.get(userKey);
        if (!StringUtils.isEmpty(userJson)) {
            UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
            return userInfo;
        }
        jedis.close();
        return null;
    }
}
