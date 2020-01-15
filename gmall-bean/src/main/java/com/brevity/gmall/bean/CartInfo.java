package com.brevity.gmall.bean;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CartInfo implements Serializable {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @Column
    private String userId;
    @Column
    private String skuId;
    @Column
    private BigDecimal cartPrice; // 加入购物车时的价格
    @Column
    private Integer skuNum;
    @Column
    private String imgUrl;
    @Column
    private String skuName;
    @Column
    private String isChecked = "1";

    // 实时价格 skuInfo.price
    @Transient
    private BigDecimal skuPrice;
}
