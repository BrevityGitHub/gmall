package com.brevity.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.brevity.gmall.bean.CartInfo;
import com.brevity.gmall.bean.SkuInfo;
import com.brevity.gmall.config.CookieUtil;
import com.brevity.gmall.config.LoginRequire;
import com.brevity.gmall.service.CartService;
import com.brevity.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class CartController {

    @Reference
    private CartService cartService;
    @Reference
    private ManageService manageService;

    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response) {
        String userId = (String) request.getAttribute("userId");
        String skuId = request.getParameter("skuId");
        String skuNum = request.getParameter("skuNum");

        // 判断用户是否登录
        if (userId == null) {

            //用户未登录，但是可以从cookie中取出临时的userId说明之前向购物车添加过商品
            userId = CookieUtil.getCookieValue(request, "my-userId", false);

            /*
             * 用户未登录，而且从cookie中取不到临时的userId，
             * 说明是第一次向购物车添加商品，必须给一个临时的userId，存放到cookie中
             */
            if (userId == null) {
                userId = UUID.randomUUID().toString().replace("-", "");
                // 将临时的userId放入cookie中
                CookieUtil.setCookie(request, response, "my-userId", userId, 30 * 24 * 3600, false);
            }
        }

        // 用户已经登录的情况下添加购物车
        cartService.addToCart(skuId, userId, Integer.parseInt(skuNum));

        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        request.setAttribute("skuInfo", skuInfo);
        request.setAttribute("skuNum", skuNum);
        return "success";
    }

    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request) {
        // 判断用户是否登录必须获取到userId
        List<CartInfo> cartInfoList = new ArrayList<>();
        String userId = (String) request.getAttribute("userId");

        if (userId != null) {
            String userTempId = CookieUtil.getCookieValue(request, "my-userId", false);
            // 存储未登录的购物车的数据
            List<CartInfo> cartInfoTempList = new ArrayList<>();

            if (userTempId != null) {
                cartInfoTempList = cartService.getCartList(userTempId);
                // 合并未登录与已经登录的数据，并删除未登录的数据
                if (cartInfoTempList != null && cartInfoTempList.size() > 0) {
                    // 返回合并之后的购物车数据
                    cartInfoList = cartService.mergeToCartList(cartInfoTempList, userId);
                    // 清空未登录数据库
                    cartService.deleteCartList(userTempId);
                }
            }

            // 未登录购物车，则从数据库获取
            if (userId == null || (cartInfoTempList == null || cartInfoTempList.size() == 0)) {
                // 通过用户id得到购物车列表
                cartInfoList = cartService.getCartList(userId);
            }
        } else {
            String userTempId = CookieUtil.getCookieValue(request, "my-userId", false);
            if (userTempId != null) {
                cartInfoList = cartService.getCartList(userTempId);
            }
        }

        request.setAttribute("cartInfoList", cartInfoList);
        return "cartList";
    }

    @ResponseBody
    @RequestMapping("checkCart")
    @LoginRequire(autoRedirect = false)
    public void checkCart(HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        String isChecked = request.getParameter("isChecked");
        String skuId = request.getParameter("skuId");
        if (userId == null) {
            // 获取临时的用户id
            userId = CookieUtil.getCookieValue(request, "my-userId", false);
        }
        // 改变商品的状态
        cartService.checkCart(isChecked, skuId, userId);
    }

    @LoginRequire
    @RequestMapping("toTrade")
    public String toTrade(HttpServletRequest request) {
        // 获取用户id
        String userId = (String) request.getAttribute("userId");
        // 获取未登录的临时用户id
        String userTempId = CookieUtil.getCookieValue(request, "my-userId", false);
        if (userTempId != null) {
            List<CartInfo> cartTempList = cartService.getCartList(userTempId);
            if (cartTempList != null && cartTempList.size() > 0) {
                // 合并购物车商品
                cartService.mergeToCartList(cartTempList, userId);
                // 删除未登录的购物车
                cartService.deleteCartList(userTempId);
            }
        }
        return "redirect://trade.gmall.com/trade";
    }
}
