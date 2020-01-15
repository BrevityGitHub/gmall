package com.brevity.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.brevity.gmall.bean.SkuInfo;
import com.brevity.gmall.bean.SpuSaleAttr;
import com.brevity.gmall.service.ListService;
import com.brevity.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Reference
    private ManageService manageService;
    @Reference
    private ListService listService;

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId, HttpServletRequest request) {
        // 根据skuId获取sku对象
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        /* 根据skuId获取skuImage列表
         * List<SkuImage> skuImageList = manageService.getSkuImageList(skuId);
         * 保存skuImageList到页面渲染
         * request.setAttribute("skuImageList", skuImageList);
         */

        // 获取销售属性和销售属性值
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrListCheckBySku(skuInfo);

        /*
         * 通过spuId查询销售属性值id所对应的skuId
         */

        /*
         * 第一种方法(通过后台代码拼接)：

        List<SkuSaleAttrValue> skuSaleAttrValueListBySpu = manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());

        // 拼接json字符串
        HashMap<String, String> map = new HashMap<>();
        String key = "";
        // 拼接规则：skuId与下一条数据的skuId不相同，不拼接；拼接到集合末尾的时候，不拼接。
        for (int i = 0; i < skuSaleAttrValueListBySpu.size(); i++) {
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueListBySpu.get(i);

         * 第一次拼接：key = 123
         * 第二次拼接：key = 123|
         * 第三次拼接：key = 123|126
         * ……
         * 当停止拼接的时候，将key放入map中，并清空key，key = ""
         * 当key不为空时拼接“|”
            if (key.length() > 0) {
                key += "|";
            }

            key += skuSaleAttrValue.getSaleAttrValueId();

            // 停止拼接的时候
            if ((i + 1) == skuSaleAttrValueListBySpu.size() || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueListBySpu.get(i + 1).getSkuId())) {
                map.put(key, skuSaleAttrValue.getSkuId());
                key = ""; // 清空key
            }
        }
        */

        /*
         * 第二种方法(通过SQL语句拼接)：
         */
        Map map = manageService.getSkuValueIdsMap(skuInfo.getSpuId());

        String valueSkuJson = JSON.toJSONString(map);

        // 保存转换后的map到页面渲染
        request.setAttribute("valueSkuJson", valueSkuJson);

        // 保存spuSaleAttrList到页面渲染
        request.setAttribute("spuSaleAttrList", spuSaleAttrList);

        // 保存skuInfo到页面渲染
        request.setAttribute("skuInfo", skuInfo);

        listService.updateHotScore(skuId);
        return "item";
    }
}
