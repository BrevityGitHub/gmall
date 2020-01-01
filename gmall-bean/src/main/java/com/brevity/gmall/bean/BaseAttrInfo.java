package com.brevity.gmall.bean;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Data
public class BaseAttrInfo implements Serializable {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @Column
    private String attrName;
    @Column
    private String catalog3Id;
    // 平台属性值的集合对象
    @Transient // 表示此属性不是数据库的字段，而是业务需要的字段
    private List<BaseAttrValue> attrValueList;
}
