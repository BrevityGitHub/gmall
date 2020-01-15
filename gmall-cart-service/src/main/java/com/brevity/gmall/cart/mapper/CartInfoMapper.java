package com.brevity.gmall.cart.mapper;

import com.brevity.gmall.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface CartInfoMapper extends Mapper<CartInfo> {

    /**
     * @param userId 根据用户Id查询商品的最新价格
     * @return
     */
    List<CartInfo> selectCartListWithCurrentPrice(String userId);
}
