package com.brevity.gmall.service;

import com.brevity.gmall.bean.SkuLsInfo;
import com.brevity.gmall.bean.SkuLsParams;
import com.brevity.gmall.bean.SkuLsResult;

public interface ListService {
    /**
     * 商品上架，把商品从数据库添加到ES中
     *
     * @param skuLsInfo
     */
    void saveSkuLsInfo(SkuLsInfo skuLsInfo);

    /**
     * @param skuLsParams 根据用户输入的条件生成DSL语句，并返回结果
     * @return
     */
    SkuLsResult search(SkuLsParams skuLsParams);

    /**
     * @param skuId 根据商品id更新ES中的热度排名
     */
    void updateHotScore(String skuId);
}
