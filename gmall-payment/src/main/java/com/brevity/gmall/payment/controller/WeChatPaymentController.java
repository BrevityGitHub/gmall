package com.brevity.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.brevity.gmall.service.PaymentService;
import com.brevity.gmall.util.IdWorker;
import com.brevity.gmall.util.StreamUtil;
import com.github.wxpay.sdk.WXPayUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller
public class WeChatPaymentController {
    @Reference
    private PaymentService paymentService;

    @Value("${partnerkey}")
    private String partnerKey;

    @ResponseBody
    @RequestMapping("wx/submit")
    public Map WeChatSubmit() {
        //生成二维码，主要要得到code_url去生成
        IdWorker idWorker = new IdWorker();
        // 类似于UUID
        long orderId = idWorker.nextId();
        String out_trade_no = orderId + "";

        // 第一个参数是订单号，第二个是付款的金额1分
        Map map = paymentService.createNative(out_trade_no, "1");
        return map;
    }

    @ResponseBody
    @RequestMapping("wx/callback/notify")
    public String callbackNotify(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletInputStream inputStream = request.getInputStream();
        String xmlString = StreamUtil.inputStream2String(inputStream, "UTF-8");

        try {
            if (WXPayUtil.isSignatureValid(xmlString, partnerKey)) {
                // 验签成功
                Map<String, String> paramMap = WXPayUtil.xmlToMap(xmlString);
                String result_code = paramMap.get("result_code");
                if (result_code != null || "SUCCESS".equals(result_code)) {
                    // 支付成功，修改交易状态，发送消息队列更改订单状态


                    // 存储相应数据给微信
                    HashMap<String, String> returnMap = new HashMap<>();
                    returnMap.put("return_code", "SUCCESS");
                    returnMap.put("return_msg", "OK");

                    String returnXml = WXPayUtil.mapToXml(returnMap);
                    // 设置返回文件格式
                    response.setContentType("text/xml");

                    return returnXml;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
