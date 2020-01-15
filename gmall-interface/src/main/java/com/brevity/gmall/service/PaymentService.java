package com.brevity.gmall.service;

import com.brevity.gmall.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    // 保存交易记录
    void savePaymentInfo(PaymentInfo paymentInfo);

    // 根据第三方交易编号获取paymentInfo
    PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery);

    // 更新交易记录
    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpdate);

    // 退款
    boolean refund(String orderId);

    // 微信支付接口
    Map createNative(String out_trade_no, String total_fee);

    // 发送支付结果消息给订单
    void sendPaymentResult(PaymentInfo paymentInfo, String result);

    // 查询支付结果
    boolean checkPayment(PaymentInfo paymentInfoQuery);

    /**
     * @param outTradeNo 商家的交易编号
     * @param delaySec   延迟时间
     * @param checkCount 检查的次数
     */
    void sendDelayPaymentResult(String outTradeNo, int delaySec, int checkCount);

    // 关闭过期的交易记录
    void closePayment(String orderId);
}
