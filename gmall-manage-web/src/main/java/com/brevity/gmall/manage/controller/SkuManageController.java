package com.brevity.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.brevity.gmall.bean.SkuInfo;
import com.brevity.gmall.bean.SkuLsInfo;
import com.brevity.gmall.bean.SpuImage;
import com.brevity.gmall.bean.SpuSaleAttr;
import com.brevity.gmall.service.ListService;
import com.brevity.gmall.service.ManageService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
public class SkuManageController {
    @Reference
    private ManageService manageService;

    @Reference
    private ListService listService;

    @RequestMapping("spuImageList")
    public List<SpuImage> getSpuImageList(SpuImage spuImage) {
        return manageService.getSpuImageList(spuImage);
    }

    // http://localhost:8082/spuSaleAttrList?spuId=60
    @RequestMapping("spuSaleAttrList")
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return manageService.getSpuSaleAttrList(spuId);
    }

    // http://localhost:8082/saveSkuInfo
    @RequestMapping("saveSkuInfo")
    public void saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        manageService.saveSkuInfo(skuInfo);
    }

    // 根据商品的id上架
    @RequestMapping("onSale")
    public void onSale(String skuId) {
        // 根据skuId获取数据

        SkuLsInfo skuLsInfo = new SkuLsInfo();
        // skuLsInfo赋值
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        // 属性拷贝
        BeanUtils.copyProperties(skuInfo, skuLsInfo);
        listService.saveSkuLsInfo(skuLsInfo);
    }
}
