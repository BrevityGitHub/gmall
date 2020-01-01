package com.brevity.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.brevity.gmall.bean.BaseSaleAttr;
import com.brevity.gmall.bean.SpuInfo;
import com.brevity.gmall.service.ManageService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
public class SpuManageController {
    @Reference
    private ManageService manageService;

    // 使用springMVC的对象传值特性
    // http://localhost:8082/spuList?catalog3Id=61
    // select * form spu_info where spu_info.catalog3Id = ?
    @RequestMapping("spuList")
    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo) {
        return manageService.getSpuInfoList(spuInfo);
    }

    @RequestMapping("baseSaleAttrList")
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return manageService.getBaseSaleAttrList();
    }

    @RequestMapping("saveSpuInfo")
    public void saveSpuInfo(@RequestBody SpuInfo spuInfo) {
        manageService.saveSpuInfo(spuInfo);
    }
}
