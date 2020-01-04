package com.brevity.gmall.bean;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

// 与ES中的type对应的实体类，作为数据的载体
@Data
public class SkuLsInfo implements Serializable {
    private String id;
    private BigDecimal price;
    private String skuName;
    private String catalog3Id;
    private String skuDefaultImg;
    // 做热度排名使用，默认为0
    private Long hotScore = 0L;
    // 平台属性值对象
    private List<skuLsAttrValue> skuAttrValueList;
}
