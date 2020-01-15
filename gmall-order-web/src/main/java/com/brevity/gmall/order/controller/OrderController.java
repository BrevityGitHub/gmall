package com.brevity.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.brevity.gmall.bean.*;
import com.brevity.gmall.config.LoginRequire;
import com.brevity.gmall.service.CartService;
import com.brevity.gmall.service.ManageService;
import com.brevity.gmall.service.OrderService;
import com.brevity.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class OrderController {

    // @Autowired  启动后会报找不到的异常，不在同一个项目，端口也不一样
    @Reference // 使用dubbo的注解
    private UserService userService;
    @Reference
    private CartService cartService;
    @Reference
    private OrderService orderService;
    @Reference
    private ManageService manageService;

    @RequestMapping("tradeByUserId")
    public List<UserAddress> trade(String userId) {
        return userService.getUserAddressByUserId(userId);
    }

    @LoginRequire
    @RequestMapping("trade")
    public String trade(HttpServletRequest request) {
        // 获取用户Id
        String userId = (String) request.getAttribute("userId");
        List<UserAddress> userAddressesList = userService.getUserAddressByUserId(userId);

        // 获取订单明细列表
        List<CartInfo> cartInfoList = cartService.getCartCheckedList(userId);
        // 存储OrderDetail集合
        List<OrderDetail> orderDetailList = new ArrayList<>();

        // 遍历购物车赋值给OrderDetail
        for (CartInfo cartInfo : cartInfoList) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setSkuNum(cartInfo.getSkuNum());

            orderDetailList.add(orderDetail);
        }

        // 计算总价格
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();

        request.setAttribute("userAddressesList", userAddressesList);
        request.setAttribute("orderDetailList", orderDetailList);
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());

        String tradeNo = orderService.getTradeNo(userId);
        request.setAttribute("tradeNo", tradeNo);
        return "trade";
    }

    @LoginRequire
    @RequestMapping("submitOrder")
    public String submitOrder(OrderInfo orderInfo, HttpServletRequest request) {
        String userId = (String) request.getAttribute("userId");
        String tradeNo = (String) request.getAttribute("tradeNo");
        orderInfo.setUserId(userId);
        boolean result = orderService.checkTradeCode(userId, tradeNo);
        if (!result) {
            // 记录重复提交
            request.setAttribute("errMsg", "请勿重复提交订单!");
            return "tradeFail";
        }

        // 删除流水号
        orderService.deleteTradeCode(userId);
        // 验证库存，验证订单明细中的每个商品
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            boolean flag = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
            // 返回true有库存，返回false没有库存
            if (!flag) {
                request.setAttribute("errMsg", orderDetail.getSkuName() + "库存不足!");
                return "tradeFail";
            }

            // 验证实时价格
            SkuInfo skuInfoFromDB = manageService.getSkuInfoFromDB(orderDetail.getSkuId());
            int res = orderDetail.getOrderPrice().compareTo(skuInfoFromDB.getPrice());
            if (res != 0) {
                request.setAttribute("errMsg", orderDetail.getSkuName() + "价格有变动!");
                cartService.loadCartCache(userId);
                return "tradeFail";
            }
        }

        String orderId = orderService.saveOrderInfo(orderInfo);
        return "redirect://payment.gmall.com/index?orderId=" + orderId;
    }

    @ResponseBody
    @RequestMapping("orderSplit")
    public String orderSplit(HttpServletRequest request) {
        String orderId = request.getParameter("orderId");
        String wareSkuMap = request.getParameter("wareSkuMap");

        // 拆单
        List<OrderInfo> orderInfoList = orderService.orderSplit(orderId, wareSkuMap);

        ArrayList<Map> mapArrayList = new ArrayList<>();
        if (orderInfoList != null && orderInfoList.size() > 0) {
            for (OrderInfo orderInfo : orderInfoList) {
                Map map = orderService.initWareOrder(orderInfo);
                mapArrayList.add(map);
            }
        }
        return JSON.toJSONString(mapArrayList);
    }
}
