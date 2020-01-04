package com.brevity.gmall.service;

import com.brevity.gmall.bean.SkuLsInfo;

public interface ListService {
    /**
     * 商品上架，把商品从数据库添加到ES中
     *
     * @param skuLsInfo
     */
    void saveSkuLsInfo(SkuLsInfo skuLsInfo);
}
