package com.brevity.gmall.bean;

import com.brevity.gmall.bean.enums.OrderStatus;
import com.brevity.gmall.bean.enums.PaymentWay;
import com.brevity.gmall.bean.enums.ProcessStatus;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderInfo implements Serializable {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @Column
    private String userId;
    @Column
    private String consignee;
    @Column
    private String consigneeTel;
    @Column
    private BigDecimal totalAmount;
    @Column
    private Date expireTime;
    @Column
    private String deliveryAddress;
    @Column
    private String orderComment;
    @Column
    private Date createTime;
    @Column
    private String parentOrderId;
    @Column
    private String trackingNo;
    @Column
    private String outTradeNo;
    @Column
    private OrderStatus orderStatus;
    @Column
    private ProcessStatus processStatus;
    @Column
    private PaymentWay paymentWay;
    @Transient
    private List<OrderDetail> orderDetailList;
    @Transient
    private String wareId;

    // 计算总价格
    public void sumTotalAmount() {
        BigDecimal totalAmount = new BigDecimal("0");
        for (OrderDetail orderDetail : orderDetailList) {
            totalAmount = totalAmount.add(orderDetail.getOrderPrice().multiply(new BigDecimal(orderDetail.getSkuNum())));
        }
        this.totalAmount = totalAmount;
    }
}
