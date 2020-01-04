package com.brevity.gmall.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

// 接受用户从ES查询出的结果数据封装到实体类
@Data
public class SkuLsResult implements Serializable {
    private List<SkuLsInfo> skuLsInfoList;
    private Long total;
    private Long totalPages;
    private List<String> attrValueIdList;
}
