package com.brevity.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.brevity.gmall.bean.*;
import com.brevity.gmall.config.RedisUtil;
import com.brevity.gmall.manage.mapper.*;
import com.brevity.gmall.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service // 使用dubbo的@Service注解
public class ManageServiceImpl implements ManageService {
    // 注入mapper
    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    private SpuInfoMapper spuInfoMapper;
    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        // select * from base_catalog2 where catalog1Id = ?
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);
        return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        // select * from base_catalog3 where catalog2Id = ?
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> getAttrInfoList(String catalog3Id) {
        // select * from base_attr_info where catalog3Id = ?
        /*
         * BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
         * baseAttrInfo.setCatalog3Id(catalog3Id);
         * return baseAttrInfoMapper.select(baseAttrInfo);
         */
        return baseAttrInfoMapper.selectBaseAttrInfoListByCatalog3Id(catalog3Id);
    }

    @Override
    @Transactional
    // 添加和修改使用的是同一个控制器
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        // 修改和更新的判断
        if (!StringUtils.isEmpty(baseAttrInfo.getId())) {
            // 修改，更新baseAttrInfo
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        } else {
            // 保存baseAttrInfo
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }

        // 修改和更新baseAttrValue
        // 先删除数据，根据attrId
        BaseAttrValue baseAttrValueDelete = new BaseAttrValue();
        baseAttrValueDelete.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValueDelete);

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        // 这个if先判断!=null，即使对象为空，也不会出现空指针异常
        if (attrValueList != null && attrValueList.size() > 0) {
            // 循环遍历
            for (BaseAttrValue baseAttrValue : attrValueList) {
                // 处理平台属性Id  baseAttrValue.attrId = baseAttrInfo.Id
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(baseAttrValue);
            }
        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        // select * from base_attr_value where attrId = ?
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        return baseAttrValueMapper.select(baseAttrValue);
    }

    @Override
    public BaseAttrInfo getBaseAttrInfo(String attrId) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        // 把平台属性值集合对象放入平台属性对象中
        baseAttrInfo.setAttrValueList(getAttrValueList(attrId));
        return baseAttrInfo;
    }

    @Override
    public List<SpuInfo> getSpuInfoList(String catalog3Id) {
        return null;
    }

    @Override
    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo) {
        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {

        spuInfoMapper.insertSelective(spuInfo);

        // 获取spuImage集合
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (spuImageList != null && spuImageList.size() > 0) {
            for (SpuImage spuImage : spuImageList) {
                // 设置spuId
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(spuImage);
            }
        }
        // 获取销售属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList != null && spuSaleAttrList.size() > 0) {
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                // 设置spuId
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(spuSaleAttr);

                // 保存销售属性值
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (spuSaleAttrValueList != null && spuSaleAttrValueList.size() > 0) {
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
                    }
                }
            }
        }
    }

    @Override
    public List<SpuImage> getSpuImageList(SpuImage spuImage) {
        // select * from spu_image where spu_id = ?
        return spuImageMapper.select(spuImage);
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return spuSaleAttrMapper.getSpuSaleAttrList(spuId);
    }

    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {
        // 保存skuInfo
        skuInfoMapper.insertSelective(skuInfo);
        // 保存skuImage
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (skuImageList != null && skuImageList.size() > 0) {
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(skuImage);
            }
        }
        // 保存skuAttrValue
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (skuAttrValueList != null && skuAttrValueList.size() > 0) {
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insertSelective(skuAttrValue);
            }
        }
        // 保存skuSaleAttrValue
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValueList != null && skuSaleAttrValueList.size() > 0) {
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
            }
        }
    }

    @Override
    public SkuInfo getSkuInfo(String skuId) {

        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        /*
         * 根据skuId调用方法得到skuImageList，放到skuInfo中，
         * skuInfo设置了skuImageList属性(非数据库字段，而是业务需要的)
         */
        skuInfo.setSkuImageList(getSkuImageList(skuId));
        return skuInfo;
    }

    // select * from sku_image where sku_id = ?
    @Override
    public List<SkuImage> getSkuImageList(String skuId) {
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        return skuImageMapper.select(skuImage);
    }

    /*
     * SELECT
        sa.id ,
        sa.spu_id,
        sa.sale_attr_name,
        sa.sale_attr_id,
        sv.id sale_attr_value_id,
        sv.sale_attr_value_name,
        skv.sku_id,
        IF(skv.sku_id IS NULL,0,1) is_checked
      FROM
        spu_sale_attr sa
      INNER JOIN
        spu_sale_attr_value  sv
      ON
        sa.spu_id=sv.spu_id
      AND
        sa.sale_attr_id=sv.sale_attr_id
      LEFT JOIN
        sku_sale_attr_value skv
      ON
        skv.sale_attr_id= sa.sale_attr_id
      AND
        skv.sale_attr_value_id=sv.id
      AND
        skv.sku_id=#{arg0}
      WHERE
        sa.spu_id=#{arg1}
      ORDER BY
        sv.sale_attr_id,sv.id;
     */
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuInfo.getId(), skuInfo.getSpuId());
    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        /*
        * 保证切换销售属性值时还是同一个SPU
        SELECT
            sale_attr_value_id,
            sku_id,
            sale_attr_value_name
        FROM
            sku_sale_attr_value ssav,
            sku_info si
        WHERE
            ssav.sku_id = si.id
        AND
            si.spu_id = #{0}
        ORDER BY
            si.id,ssav.sale_attr_id;
         */
        return skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
    }

    @Override
    public Map getSkuValueIdsMap(String spuId) {
        /*
         select
             sku_id,
             group_concat(sale_attr_value_id group by sale_attr_value_id asc separator '|') values_id
         from
             sku_sale_attr_value ssav
         inner join
             sku_info si
         on
             ssav.sku_id = si.id
         where
             si.spu_id = 60
         group by
             ssav.sku_id;
         */
        List<Map> mapList = skuSaleAttrValueMapper.getSaleAttrValuesBySpu(spuId);

        // 声明一个map获取123|126|132 作为key，38当作value放入一个map中
        HashMap<Object, Object> map = new HashMap<>();
        for (Map mapSku : mapList) {
            map.put(mapSku.get("value_ids"), mapSku.get("sku_id"));
        }
        return map;
    }
}
