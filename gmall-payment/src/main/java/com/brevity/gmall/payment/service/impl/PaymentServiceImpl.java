package com.brevity.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.brevity.gmall.bean.OrderInfo;
import com.brevity.gmall.bean.PaymentInfo;
import com.brevity.gmall.bean.enums.PaymentStatus;
import com.brevity.gmall.config.ActiveMQUtil;
import com.brevity.gmall.payment.mapper.PaymentInfoMapper;
import com.brevity.gmall.service.OrderService;
import com.brevity.gmall.service.PaymentService;
import com.brevity.gmall.util.HttpClient;
import com.github.wxpay.sdk.WXPayUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private PaymentInfoMapper paymentInfoMapper;
    @Autowired
    private AlipayClient alipayClient;
    @Reference
    private OrderService orderService;
    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String partner;
    @Value("${partnerkey}")
    private String partnerKey;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery) {
        return paymentInfoMapper.selectOne(paymentInfoQuery);
    }

    @Override
    public void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpdate) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo", out_trade_no);
        paymentInfoMapper.updateByExampleSelective(paymentInfoUpdate, example);
    }

    @Override
    public boolean refund(String orderId) {
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no", orderInfo.getOutTradeNo());
        map.put("refund_amount", orderInfo.getTotalAmount());
        map.put("refund_reason", "钱不够!!!");

        request.setBizContent(JSON.toJSONString(map));

        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        if (response.isSuccess()) {
            // 更新订单状态
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setPaymentStatus(PaymentStatus.ClOSED);
            updatePaymentInfo(orderInfo.getOutTradeNo(), paymentInfo);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        // 制作参数给微信支付接口
        HashMap<String, String> param = new HashMap<>();
        param.put("appid", appid);
        param.put("partner", partner);
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        param.put("body", "买买买");
        param.put("out_trade_no", out_trade_no);
        param.put("total_fee", total_fee);
        param.put("spill_create_ip", "127.0.0.1");
        param.put("notify_url", "内网穿透工具生成的地址");
        param.put("trade_type", "NATIVE");

        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerKey);
            // 发送参数给微信接口
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");

            httpClient.setXmlParam(xmlParam);
            // httpClient.setParameter(param); xml格式错误

            // 设置https发送
            httpClient.setHttps(true);
            // 设置发送方式
            httpClient.post();

            // 获取结果集
            String result = httpClient.getContent();
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            // 接收返回值
            HashMap<String, Object> map = new HashMap<>();
            map.put("code_url", resultMap.get("code_url"));
            map.put("total_fee", total_fee);
            map.put("out_trade_no", out_trade_no);

            return map;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void sendPaymentResult(PaymentInfo paymentInfo, String result) {
        Connection connection = activeMQUtil.getConnection();
        try {
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue payment_result_queue = session.createQueue("PAYMENT_RESULT_QUEUE");
            // 创建提供者对象
            MessageProducer producer = session.createProducer(payment_result_queue);
            // 创建消息对象
            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();
            activeMQMapMessage.setString("orderId", paymentInfo.getOrderId());
            activeMQMapMessage.setString("result", result);
            producer.send(activeMQMapMessage);
            // 提交消息
            session.commit();
            // 关闭
            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    // 支付宝主动询问支付结果
    @Override
    public boolean checkPayment(PaymentInfo paymentInfoQuery) {
        if (paymentInfoQuery == null) {
            return false;
        }

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        // 根据out_trade_no查询
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no", paymentInfoQuery.getOutTradeNo());
        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeQueryResponse response = null;

        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        if (response.isSuccess()) {
            // 调用成功不等于支付成功
            String tradeStatus = response.getTradeStatus();
            if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                // 修改交易记录的状态
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setPaymentStatus(PaymentStatus.PAID);
                paymentInfo.setCallbackTime(new Date());
                paymentInfo.setCallbackContent("延迟队列验签成功!");
                updatePaymentInfo(paymentInfoQuery.getOutTradeNo(), paymentInfo);

                // 判断交易记录状态
                if (paymentInfoQuery.getPaymentStatus() == PaymentStatus.PAID || paymentInfoQuery.getPaymentStatus() == PaymentStatus.ClOSED) {
                    return false;
                }

                // 发送消息给activemq
                sendPaymentResult(paymentInfoQuery, "success");
                return true;
            }
        } else {
            return false;
        }
        return false;
    }

    @Override
    public void sendDelayPaymentResult(String outTradeNo, int delaySec, int checkCount) {
        Connection connection = activeMQUtil.getConnection();
        try {
            connection.start();

            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue payment_result_check_queue = session.createQueue("PAYMENT_RESULT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(payment_result_check_queue);

            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();
            activeMQMapMessage.setString("outTradeNo", outTradeNo);
            activeMQMapMessage.setInt("delaySec", delaySec);
            activeMQMapMessage.setInt("checkCount", checkCount);

            // 开启延迟队列，规定延迟时间
            activeMQMapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delaySec * 1000);

            producer.send(activeMQMapMessage);

            session.commit();

            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void closePayment(String orderId) {
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus(PaymentStatus.ClOSED);

        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("orderId", orderId);

        paymentInfoMapper.updateByExampleSelective(paymentInfo, example);
    }
}
