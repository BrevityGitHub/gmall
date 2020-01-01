package com.brevity.gmall.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.io.Serializable;

@Data
public class UserInfo implements Serializable {
    //根据数据库中的表字段编写实体类的属性,使用驼峰命名规则

    @Id // 表示表中的主键
    @Column //表示表中的普通字段

    /* @GeneratedValue  获取主键自增
     * GenerationType.IDENTITY  获取MySQL主键自增
     * GenerationType.AUTO  获取Oracle主键自增
     */
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @Column
    private String loginName;
    @Column
    private String nickName;
    @Column
    private String passwd;
    @Column
    private String name;
    @Column
    private String phoneNum;
    @Column
    private String email;
    @Column
    private String headImg;
    @Column
    private String userLevel;
}
