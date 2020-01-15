package com.brevity.gmall.service;

import com.brevity.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {
    // 添加购物车 用户id，商品id，商品数量
    void addToCart(String skuId, String userId, Integer skuNum);

    List<CartInfo> getCartList(String userId);

    List<CartInfo> loadCartCache(String userId);

    /**
     * 合并购物车
     *
     * @param cartInfoTempList
     * @param userId
     * @return
     */
    List<CartInfo> mergeToCartList(List<CartInfo> cartInfoTempList, String userId);

    void deleteCartList(String userId);

    /**
     * 修改商品的状态
     *
     * @param isChecked
     * @param skuId
     * @param userId
     */
    void checkCart(String isChecked, String skuId, String userId);

    /**
     * 根据用户Id获取选中的商品
     *
     * @param userId
     * @return
     */
    List<CartInfo> getCartCheckedList(String userId);
}
