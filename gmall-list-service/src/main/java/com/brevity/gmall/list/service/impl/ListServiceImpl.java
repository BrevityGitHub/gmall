package com.brevity.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.brevity.gmall.bean.SkuLsInfo;
import com.brevity.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

@Service
public class ListServiceImpl implements ListService {

    // 把商品信息保存到ES中，不需要注入mapper去操作数据库
    @Autowired
    private JestClient jestClient;

    public static final String ES_INDEX = "gmall";
    public static final String ES_TYPE = "skuInfo";

    /**
     * 从数据库中查询并赋值给skuLsInfo实体类
     *
     * @param skuLsInfo
     */
    @Override
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo) {
        // 定义执行的动作  PUT /index/type/id {skuLsInfo}
        Index index = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();
        // 执行动作
        try {
            jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
