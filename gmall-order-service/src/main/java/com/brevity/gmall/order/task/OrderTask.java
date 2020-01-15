package com.brevity.gmall.order.task;

import com.alibaba.dubbo.config.annotation.Reference;
import com.brevity.gmall.bean.OrderInfo;
import com.brevity.gmall.service.OrderService;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@EnableScheduling
public class OrderTask {

    /*
    每隔多长时间触发一次 cron 定义执行的规则：每分钟的第五秒执行
    @Scheduled(cron = "5 * * * * ?")
    public void test01() {
        System.out.println(Thread.currentThread().getName() + "--------------");
    }

    // 每隔五秒执行
    @Scheduled(cron = "0/5 * * * * ?")
    public void test02() {
        System.out.println(Thread.currentThread().getName() + "**************");
    }
    */

    @Reference
    private OrderService orderService;

    @Scheduled(cron = "0/20 * * * * ?")
    public void checkOrder() {
        // 查询过期订单
        List<OrderInfo> orderInfoList = orderService.getExpiredOrderList();
        if (orderInfoList != null && orderInfoList.size() > 0) {
            for (OrderInfo orderInfo : orderInfoList) {
                // 更改订单的状态
                orderService.escExpiredOrder(orderInfo);
            }
        }
    }
}
