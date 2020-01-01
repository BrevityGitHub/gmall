package com.brevity.gmall.manage.demo.service.impl;

import com.brevity.gmall.manage.demo.bean.UserInfo;
import com.brevity.gmall.manage.demo.mapper.UserInfoMapper;
import com.brevity.gmall.manage.demo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

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

    @Override
    public List<UserInfo> findUserInfoByProperty(UserInfo userInfo) {
        return userInfoMapper.select(userInfo);
    }

    @Override
    public List<UserInfo> findByRange(UserInfo userInfo) {
        // select * from user_info where nick_name like %?%
        Example example = new Example(UserInfo.class);
        // 第一个参数表示实体类的属性，第二个参数表示给属性赋值
        example.createCriteria().andLike("nickName", "%" + userInfo.getNickName() + "%");
        return userInfoMapper.selectByExample(example);
    }

    @Override
    public void addUser(UserInfo userInfo) {
        userInfoMapper.insertSelective(userInfo);
    }

    @Override
    public void deleteUser(UserInfo userInfo) {
        userInfoMapper.delete(userInfo);
        /* Example example = new Example(UserInfo.class);
         * example.createCriteria().andEqualTo("loginName", userInfo.getLoginName());
         * userInfoMapper.deleteByExample(example);
         */
    }

    @Override
    public void updateUser(UserInfo userInfo) {
        // update user_info set phone_num = ? where login_name = ?
        Example example = new Example(UserInfo.class);
        example.createCriteria().andEqualTo("loginName", userInfo.getLoginName());

        // 第一个参数表示要修改的数据，第二个参数表示要修改的条件
        userInfoMapper.updateByExampleSelective(userInfo, example);
    }
}
