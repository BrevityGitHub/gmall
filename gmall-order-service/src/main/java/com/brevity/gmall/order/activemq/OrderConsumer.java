package com.brevity.gmall.order.activemq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.brevity.gmall.bean.enums.ProcessStatus;
import com.brevity.gmall.service.OrderService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
// 消费支付模块发送的消息
public class OrderConsumer {
    @Reference
    private OrderService orderService;

    // 监听的队列
    @JmsListener(destination = "PAYMENT_RESULT_QUEUE", containerFactory = "jmsQueueListener")
    public void consumerPaymentResult(MapMessage mapMessage) throws JMSException {
        // 获取消息队列中的数据
        String orderId = mapMessage.getString("orderId");
        String result = mapMessage.getString("result");

        // 返回支付结果
        if ("success".equals(result)) {
            // 支付成功，更新订单状态为已支付
            orderService.updateOrderStatus(orderId, ProcessStatus.PAID);

            // 发送消息给库存系统
            orderService.sendOrderStatus(orderId);
            orderService.updateOrderStatus(orderId, ProcessStatus.NOTIFIED_WARE);
        }
    }

    // 监听的队列
    @JmsListener(destination = "SKU_DEDUCT_QUEUE", containerFactory = "jmsQueueListener")
    public void consumerSkuDeduct(MapMessage mapMessage) throws JMSException {
        // 获取消息队列中的数据
        String orderId = mapMessage.getString("orderId");
        String status = mapMessage.getString("status");

        // 更新订单状态为已发货
        if ("DEDUCTED".equals(status)) {
            orderService.updateOrderStatus(orderId, ProcessStatus.DELEVERED);
        }
    }
}
