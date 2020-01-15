package com.brevity.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.brevity.gmall.bean.CartInfo;
import com.brevity.gmall.bean.SkuInfo;
import com.brevity.gmall.cart.constant.CartConst;
import com.brevity.gmall.cart.mapper.CartInfoMapper;
import com.brevity.gmall.config.RedisUtil;
import com.brevity.gmall.service.CartService;
import com.brevity.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartInfoMapper cartInfoMapper;
    @Reference
    private ManageService manageService;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {

        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;

        // 缓存中没有数据，从数据库中加载并放入缓存
        if (!jedis.exists(cartKey)) {
            loadCartCache(userId);
        }

        /* 1.查询当前商品是否在购物车中
         * select * from cart_info where sku_id = ? and user_id = ?
         */
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        cartInfo.setSkuId(skuId);
        CartInfo cartInfoExist = cartInfoMapper.selectOne(cartInfo);

        if (cartInfoExist != null) {
            // 数据库中有数据，直接数量相加
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum() + skuNum);
            // 获取实时价格
            cartInfoExist.setSkuPrice(cartInfoExist.getCartPrice());
            // 更新数据库
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
        } else {
            // 第一次添加到购物车，数据来源于商品详情
            CartInfo cartInfo1 = new CartInfo();
            // 商品详情的数据
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setSkuNum(skuNum);
            cartInfo1.setSkuId(skuId);
            cartInfo1.setUserId(userId);
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setSkuName(skuInfo.getSkuName());

            cartInfoMapper.insertSelective(cartInfo1);

            cartInfoExist = cartInfo1;
        }

        // 放入redis缓存
        jedis.hset(cartKey, skuId, JSON.toJSONString(cartInfoExist));
        cartKeyExpire(userId, jedis, cartKey);
        jedis.close();
    }

    public void cartKeyExpire(String userId, Jedis jedis, String cartKey) {
        /* 设置缓存的过期时间，让整个购物车过期，
         * 再次查询时从数据库获取，
         * 购物车的过期时间以用户的过期时间为依据
         */

        // 获取用户的key
        String userKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;

        // 用户未登录
        if (!jedis.exists(userKey)) {
            // 设置一个购物车的过期时间 (可以根据用户等级或者购买力设置)
            jedis.expire(cartKey, 7 * 24 * 3600);
        } else {
            // 用户已经登录，购物车的过期时间以用户的过期时间为准
            Long ttl = jedis.ttl(userKey);
            jedis.expire(cartKey, ttl.intValue());
        }
    }

    @Override
    public List<CartInfo> getCartList(String userId) {

        // 声明一个集合来存储购物车数据
        List<CartInfo> cartInfoArrayList = new ArrayList<>();

        /*
         1.先获取缓存中的数据，如果有数据，直接返回
         2.如果缓存中没有数据，从数据库中获取，并放入redis中
         */
        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        // 获取cartKey中的所有数据
        List<String> list = jedis.hvals(cartKey);  // jedis.hget()每次只能获取一条数据
        if (list != null && list.size() > 0) {
            for (String cartJson : list) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                cartInfoArrayList.add(cartInfo);
            }

            // cartInfoArrayList：购物车集合 (应该按照修改时间降序排序)
            cartInfoArrayList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    // 按照id比较
                    return o1.getId().compareTo(o2.getId());
                }
            });
            // 返回缓存中的数据
            return cartInfoArrayList;
        } else {
            // 从数据库中查询然后放入缓存
            cartInfoArrayList = loadCartCache(userId);
            return cartInfoArrayList;
        }
    }

    // 从数据库中获取购物车的数据放入缓存
    public List<CartInfo> loadCartCache(String userId) {
        // 获取数据库中的购物车数据，保证购物车中商品价格的实时性
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurrentPrice(userId);

        // 数据库中为空
        if (cartInfoList == null) {
            return null;
        }

        // 集合数据添加到缓存
        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        for (CartInfo cartInfo : cartInfoList) {
            jedis.hset(cartKey, cartInfo.getSkuId(), JSON.toJSONString(cartInfo));
        }

        // 设置过期时间
        cartKeyExpire(userId, jedis, cartKey);
        jedis.close();
        return cartInfoList;
    }

    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartInfoTempList, String userId) {
        List<CartInfo> cartInfoListDB = cartInfoMapper.selectCartListWithCurrentPrice(userId);
        if (cartInfoListDB != null && cartInfoListDB.size() > 0) {
            for (CartInfo cartInfoTemp : cartInfoTempList) {
                boolean isMatch = false;
                for (CartInfo cartInfoDB : cartInfoListDB) {
                    if (cartInfoDB.getSkuId().equals(cartInfoTemp.getSkuId())) {
                        cartInfoDB.setSkuNum(cartInfoDB.getSkuNum() + cartInfoTemp.getSkuNum());
                        cartInfoDB.setIsChecked(cartInfoTemp.getIsChecked());
                        cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
                        isMatch = true;
                    }
                }

                // 没有相同的商品，直接添加
                if (!isMatch) {
                    cartInfoTemp.setId(null);
                    cartInfoTemp.setUserId(userId);
                    cartInfoMapper.insertSelective(cartInfoTemp);
                }
            }
        }

        // 汇总数据
        List<CartInfo> cartInfoList = loadCartCache(userId);
        // 合并勾选的状态
        for (CartInfo cartInfoDB : cartInfoList) {
            for (CartInfo cartInfoTemp : cartInfoTempList) {
                if (cartInfoTemp.getSkuId().equals(cartInfoDB.getSkuId())) {
                    if ("1".equals(cartInfoTemp.getIsChecked())) {
                        checkCart("1", cartInfoDB.getSkuId(), userId);
                    }
                }
            }
        }
        return cartInfoList;
    }

    @Override
    public void deleteCartList(String userId) {
        // 清空未登录的数据库和缓存的信息
        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        jedis.del(cartKey);
        jedis.close();

        // 从数据库删除未登录的数据
        // delete from cart_info where user_id = ?
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId", userId);
        cartInfoMapper.deleteByExample(example);
    }

    @Override
    public void checkCart(String isChecked, String skuId, String userId) {
        // 根据userId和skuId修改状态
        CartInfo cartInfo = new CartInfo();
        cartInfo.setIsChecked(isChecked);
        Example example = new Example(CartInfo.class);
        example.createCriteria().andEqualTo("userId", userId).andEqualTo("skuId", skuId);
        // 第一个参数传递的是修改的数据内容，第二个参数传递的是按照什么条件修改
        cartInfoMapper.updateByExampleSelective(cartInfo, example);

        // 修改缓存数据
        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        String cartJson = jedis.hget(cartKey, skuId);
        CartInfo cInfo = JSON.parseObject(cartJson, CartInfo.class);
        cInfo.setIsChecked(isChecked);
        jedis.hset(cartKey, skuId, JSON.toJSONString(cInfo));
        jedis.close();
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        Jedis jedis = redisUtil.getJedis();
        String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        List<String> list = jedis.hvals(cartKey);
        if (list != null && list.size() > 0) {
            for (String cartJson : list) {
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                if ("1".equals(cartInfo.getIsChecked())) {
                    cartInfoList.add(cartInfo);
                }
            }
        }
        return cartInfoList;
    }
}