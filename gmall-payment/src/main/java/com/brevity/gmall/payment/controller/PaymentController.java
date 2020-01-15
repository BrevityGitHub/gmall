package com.brevity.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.brevity.gmall.bean.OrderInfo;
import com.brevity.gmall.bean.PaymentInfo;
import com.brevity.gmall.bean.enums.PaymentStatus;
import com.brevity.gmall.config.LoginRequire;
import com.brevity.gmall.payment.config.AlipayConfig;
import com.brevity.gmall.service.OrderService;
import com.brevity.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.jboss.netty.handler.codec.rtsp.RtspHeaders.Values.CHARSET;

@Controller
public class PaymentController {
    @Reference
    private OrderService orderService;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private AlipayClient alipayClient;

    @LoginRequire
    @RequestMapping("index")
    public String index(HttpServletRequest request) {
        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());
        request.setAttribute("orderId", orderId);
        return "index";
    }

    @LoginRequire
    @ResponseBody
    @RequestMapping("alipay/submit")
    public String alipaySubmit(HttpServletRequest request, HttpServletResponse response) {
        // 记录交易状态
        String orderId = request.getParameter("orderId");

        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(orderId);
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        paymentInfo.setSubject("买买买");
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());

        paymentService.savePaymentInfo(paymentInfo);

        // 生成二维码
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        // 同步回调路径
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        // 异步回调路径(需要内网穿透)
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);

        // 业务数据获取
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no", paymentInfo.getOutTradeNo());
        map.put("product_code", "FAST_INSTANT_TRADE_PAY");
        map.put("total_amount", paymentInfo.getTotalAmount());
        map.put("subject", paymentInfo.getSubject());

        // 业务数据填充
        alipayRequest.setBizContent(JSON.toJSONString(map));
        String form = "";

        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        response.setContentType("text/html;charset=" + CHARSET);

        // 调用发送消息队列，主动询问支付结果
        paymentService.sendDelayPaymentResult(paymentInfo.getOutTradeNo(), 15, 3);

        return form;
    }

    // 同步回调方法
    @RequestMapping("alipay/callback/return")
    public String callback() {
        return "redirect:" + AlipayConfig.return_order_url;
    }

    // 得到异步通知中的参数，在url后面
    @ResponseBody
    @RequestMapping("alipay/callback/notify")
    public String callbackNotify(@RequestParam Map<String, String> paramMap, HttpServletRequest request) {
        boolean flag = false;
        // 获取交易状态
        String trade_status = paramMap.get("trade_status");
        String out_trade_no = paramMap.get("out_trade_no");

        try {
            flag = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, CHARSET, AlipayConfig.sign_type);
            if (flag) {
                // 验签成功，进行二次校验，校验交易状态
                if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)) {
                    // 通过out_trade_no查询交易状态
                    PaymentInfo paymentInfoQuery = new PaymentInfo();
                    paymentInfoQuery.setOutTradeNo(out_trade_no);
                    PaymentInfo paymentInfo = paymentService.getPaymentInfo(paymentInfoQuery);
                    // 校验交易状态,避免重复付款
                    if (paymentInfo.getPaymentStatus() == PaymentStatus.PAID || paymentInfo.getPaymentStatus() == PaymentStatus.ClOSED) {
                        return "failure";
                    }

                    // 更改交易状态
                    PaymentInfo paymentInfoUpdate = new PaymentInfo();
                    paymentInfoUpdate.setPaymentStatus(PaymentStatus.PAID);
                    paymentInfoUpdate.setCallbackTime(new Date());

                    paymentService.updatePaymentInfo(out_trade_no, paymentInfoUpdate);
                    // 更新订单状态，发送给消息队列
                    paymentService.sendPaymentResult(paymentInfo, "success");
                    return "success";
                }
            } else {
                // 验签失败，记录异常日志
                return "failure";
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return "";
    }

    // 发送验证
    @ResponseBody
    @RequestMapping("sendPaymentResult")
    public String sendPaymentResult(PaymentInfo paymentInfo, @RequestParam("result") String result) {
        paymentService.sendPaymentResult(paymentInfo, result);
        return "send payment result";
    }

    // 退款
    @ResponseBody
    @RequestMapping("refund")
    public String refund(String orderId) {
        boolean flag = paymentService.refund(orderId);
        return "" + flag;
    }

    @ResponseBody
    @RequestMapping("queryPaymentResult")
    public String queryPaymentResult(HttpServletRequest request) {
        String orderId = request.getParameter("orderId");
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        PaymentInfo paymentInfoQuery = paymentService.getPaymentInfo(paymentInfo);
        boolean result = paymentService.checkPayment(paymentInfoQuery);
        return "" + result;
    }
}

