package com.brevity.gmall.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.io.Serializable;

@Data
public class SpuSaleAttrValue implements Serializable {
    @Id
    @Column
    private String id;
    @Column
    private String spuId;
    @Column
    private String saleAttrId;
    @Column
    private String saleAttrValueName;
    /*
     * 判断当前销售属性是否被锁定，
     * 设定1为锁定，0为未锁定
     */
    @Transient
    private String isChecked;
}
