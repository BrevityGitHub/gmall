package com.brevity.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.brevity.gmall.bean.*;
import com.brevity.gmall.service.ListService;
import com.brevity.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {

    @Reference
    private ListService listService;
    @Reference
    private ManageService manageService;

    @RequestMapping("list.html")
    public String search(SkuLsParams skuLsParams, HttpServletRequest request) {
        skuLsParams.setPageSize(3);

        SkuLsResult skuLsResult = listService.search(skuLsParams);
        // 得到skuLsInfo的数据集合
        List<SkuLsInfo> skuLsInfoList = skuLsResult.getSkuLsInfoList();
        // 得到平台属性值id的集合
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        // 根据valueId查询平台属性、平台属性值
        List<BaseAttrInfo> baseAttrInfoList = manageService.getAttrList(attrValueIdList);
        // 记录查询的参数条件
        String urlParam = makeUrlParam(skuLsParams);
        // 声明一个集合存储面包屑
        ArrayList<BaseAttrValue> baseAttrValueArrayList = new ArrayList<>();

        for (Iterator<BaseAttrInfo> iterator = baseAttrInfoList.iterator(); iterator.hasNext(); ) {
            BaseAttrInfo baseAttrInfo = iterator.next();
            // 获取平台属性值对象集合
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            // 与参数上的valueId进行匹配
            if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    for (String valueId : skuLsParams.getValueId()) {
                        if (valueId.equals(baseAttrValue.getId())) {
                            // 删除平台属性
                            iterator.remove();

                            // 添加面包屑
                            BaseAttrValue baseAttrValued = new BaseAttrValue();
                            baseAttrValued.setValueName(baseAttrInfo.getAttrName() + "：" + baseAttrValue.getValueName());
                            // 调用制作urlParam的参数的方法
                            String newParam = makeUrlParam(skuLsParams, valueId);
                            baseAttrValued.setUrlParam(newParam);
                            // 存储面包屑
                            baseAttrValueArrayList.add(baseAttrValued);
                        }
                    }
                }
            }
        }

        request.setAttribute("baseAttrValueArrayList", baseAttrValueArrayList);
        request.setAttribute("urlParam", urlParam);
        request.setAttribute("baseAttrInfoList", baseAttrInfoList);
        request.setAttribute("totalPages", skuLsResult.getTotalPages());
        request.setAttribute("pageNo", skuLsParams.getPageNo());
        // 将skuLsInfo保存到域中
        request.setAttribute("skuLsInfoList", skuLsInfoList);
        request.setAttribute("keyword", skuLsParams.getKeyword());
        return "list";
    }

    /**
     * @param skuLsParams     记录查询的参数
     * @param excludeValueIds 点击面包屑时所得到的平台属性值id
     * @return
     */
    public String makeUrlParam(SkuLsParams skuLsParams, String... excludeValueIds) {
        String urlParam = "";

        // 说明第一次是用关键字查询的
        if (skuLsParams.getKeyword() != null && skuLsParams.getKeyword().length() > 0) {
            urlParam += "keyword=" + skuLsParams.getKeyword();
        }

        // 说明是按照三级分类id查询的
        if (skuLsParams.getCatalog3Id() != null && skuLsParams.getCatalog3Id().length() > 0) {
            urlParam += "catalog3Id=" + skuLsParams.getCatalog3Id();
        }

        // 判断是否有平台属性值id查询
        if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
            // 循环拼接
            for (String valueId : skuLsParams.getValueId()) {

                if (excludeValueIds != null && excludeValueIds.length > 0) {
                    // 获取点击的平台属性值id
                    String excludeValueId = excludeValueIds[0];
                    if (excludeValueId.equals(valueId)) {
                        continue;
                    }
                }

                if (urlParam.length() > 0) {
                    urlParam += "&";
                }
                urlParam += "valueId=" + valueId;
            }
        }
        return urlParam;
    }
}
