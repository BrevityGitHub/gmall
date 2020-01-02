package com.brevity.gmall.service;

import com.brevity.gmall.bean.*;

import java.util.List;
import java.util.Map;

public interface ManageService {
    // 查询所有的一级分类数据，不需要参数
    List<BaseCatalog1> getCatalog1();

    /**
     * 根据一级分类的id获取二级分类的数据列表
     *
     * @param catalog1Id
     * @return
     */
    List<BaseCatalog2> getCatalog2(String catalog1Id);

    /**
     * 根据二级分类的id获取三级分类的数据列表
     *
     * @param catalog2Id
     * @return
     */
    List<BaseCatalog3> getCatalog3(String catalog2Id);

    /**
     * 根据三级分类id获取平台属性列表
     *
     * @param catalog3Id
     * @return
     */
    List<BaseAttrInfo> getAttrInfoList(String catalog3Id);

    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    /**
     * 通过平台属性Id查找平台属性值对象集合
     *
     * @param attrId
     * @return
     */
    List<BaseAttrValue> getAttrValueList(String attrId);

    /**
     * 根据平台属性id获取平台属性对象
     *
     * @param attrId
     * @return
     */
    BaseAttrInfo getBaseAttrInfo(String attrId);

    /**
     * 只能通过三级分类id查询SpuInfo集合
     *
     * @param catalog3Id
     * @return
     */
    List<SpuInfo> getSpuInfoList(String catalog3Id);

    /**
     * 根据SpuInfo属性查询数据
     *
     * @param spuInfo
     * @return
     */
    List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);

    /**
     * 查询所有的销售属性名称
     *
     * @return
     */
    List<BaseSaleAttr> getBaseSaleAttrList();

    /**
     * 保存spuInfo
     *
     * @param spuInfo
     */
    void saveSpuInfo(SpuInfo spuInfo);

    /**
     * 根据spuImage属性查询spuImage列表
     *
     * @param spuImage
     * @return
     */
    List<SpuImage> getSpuImageList(SpuImage spuImage);

    /**
     * 根据spuId查询数据
     *
     * @param spuId
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    /**
     * 保存skuInfo对象
     *
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);

    /**
     * 根据skuId获取skuInfo对象
     *
     * @param skuId
     * @return
     */
    SkuInfo getSkuInfo(String skuId);

    /**
     * 根据skuId获取skuImage列表
     *
     * @param skuId
     * @return
     */
    List<SkuImage> getSkuImageList(String skuId);

    /**
     * 根据skuInfo对象获取销售属性与销售属性值
     *
     * @param skuInfo
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo);

    /**
     * @param spuId
     * @return
     */
    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);

    /**
     * 根据spuId查询由销售属性值与skuId组成的map
     *
     * @param spuId
     * @return
     */
    Map getSkuValueIdsMap(String spuId);
}
