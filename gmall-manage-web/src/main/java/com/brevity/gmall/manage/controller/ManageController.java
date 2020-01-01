package com.brevity.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.brevity.gmall.bean.*;
import com.brevity.gmall.service.ManageService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// vue页面接受的数据为json格式的数据
@RestController // 等价于@ResponseBody + @Controller
@CrossOrigin // (spring的注解) 不使用dubbo的原因是：dubbo发送的是实现类，而模块访问的是控制器
public class ManageController {

    @Reference // RPC远程调用Service，使用dubbo调用
    private ManageService manageService;

    /* http://localhost:8082/getCatalog1
     * 查询所有的一级分类数据
     */
    @RequestMapping("getCatalog1")
    public List<BaseCatalog1> getCatalog1() {
        return manageService.getCatalog1();
    }

    @RequestMapping("getCatalog2")
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        return manageService.getCatalog2(catalog1Id);
    }

    @RequestMapping("getCatalog3")
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        return manageService.getCatalog3(catalog2Id);
    }

    @RequestMapping("attrInfoList")
    public List<BaseAttrInfo> getAttrInfoList(String catalog3Id) {
        return manageService.getAttrInfoList(catalog3Id);
    }

    // http://localhost:8082/saveAttrInfo 添加平台属性
    // 前端页面传递的是json数据，后端使用java对象接收，必须使用@RequestBody注解
    @RequestMapping("saveAttrInfo")
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo) {
        // 调用服务层
        manageService.saveAttrInfo(baseAttrInfo);
    }

    // http://localhost:8082/getAttrValueList?attrId=106
    // 修改平台属性值回显
    @RequestMapping("getAttrValueList")
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        /* 通过平台属性Id查找平台属性值对象集合
         * return manageService.getAttrValueList(attrId);
         */
        // select * from base_attr_info where id = attrId
        BaseAttrInfo baseAttrInfo = manageService.getBaseAttrInfo(attrId);
        // baseAttrInfo.getAttrValueList();
        return baseAttrInfo.getAttrValueList();
    }
}
