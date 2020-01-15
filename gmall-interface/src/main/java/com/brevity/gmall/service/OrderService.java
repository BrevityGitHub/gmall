package com.brevity.gmall.service;

import com.brevity.gmall.bean.OrderInfo;
import com.brevity.gmall.bean.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

public interface OrderService {

    /**
     * @param orderInfo
     * @return 返回订单号
     */
    String saveOrderInfo(OrderInfo orderInfo);

    /**
     * @param userId 利用userId为key在缓存存储流水号
     * @return 制作流水号，防止表单重复提交
     */
    String getTradeNo(String userId);

    /**
     * @param userId
     * @param tradeCodeNo
     * @return 比较流水号
     */
    boolean checkTradeCode(String userId, String tradeCodeNo);

    /**
     * 删除流水号
     *
     * @param userId
     */
    void deleteTradeCode(String userId);

    boolean checkStock(String skuId, Integer skuNum);

    OrderInfo getOrderInfo(String orderId);

    // 更订单状态
    void updateOrderStatus(String orderId, ProcessStatus processStatus);

    // 根据订单号发送消息给库存
    void sendOrderStatus(String orderId);

    // 查询过期订单
    List<OrderInfo> getExpiredOrderList();

    // 处理过期订单
    void escExpiredOrder(OrderInfo orderInfo);

    Map initWareOrder(OrderInfo orderInfo);

    List<OrderInfo> orderSplit(String orderId, String wareSkuMap);
}
