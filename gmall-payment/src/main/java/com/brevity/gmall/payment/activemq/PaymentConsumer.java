package com.brevity.gmall.payment.activemq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.brevity.gmall.bean.PaymentInfo;
import com.brevity.gmall.service.PaymentService;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

@Component
public class PaymentConsumer {
    @Reference
    private PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_RESULT_CHECK_QUEUE", containerFactory = "jmsQueueListener")
    public void consumeSkuDeduct(MapMessage mapMessage) throws JMSException {
        // 获取消息队列的数据
        String outTradeNo = mapMessage.getString("outTradeNo");
        int delaySec = mapMessage.getInt("delaySec");
        int checkCount = mapMessage.getInt("checkCount");

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(outTradeNo);
        PaymentInfo paymentInfoQuery = paymentService.getPaymentInfo(paymentInfo);

        boolean result = paymentService.checkPayment(paymentInfoQuery);

        // 未支付成功
        if (!result && checkCount > 0) {
            // 再次查询
            paymentService.sendDelayPaymentResult(outTradeNo, delaySec, checkCount - 1);
        }
    }
}
