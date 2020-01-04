package com.brevity.gmall.bean;

import lombok.Data;

import java.io.Serializable;

// 把用户可能查询的条件封装为一个对象
@Data
public class SkuLsParams implements Serializable {
    // 根据输入的关键字查询
    private String keyword;
    // 根据三级分类id查询
    private String catalog3Id;
    // 根据平台属性查询
    private String[] valueId;
    // 默认从第一页查询
    private int pageNo = 1;
    // 默认每页显示的条数
    private int pageSize = 20;
}
