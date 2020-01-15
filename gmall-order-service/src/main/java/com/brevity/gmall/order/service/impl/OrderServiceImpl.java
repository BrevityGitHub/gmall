package com.brevity.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.brevity.gmall.bean.OrderDetail;
import com.brevity.gmall.bean.OrderInfo;
import com.brevity.gmall.bean.enums.OrderStatus;
import com.brevity.gmall.bean.enums.ProcessStatus;
import com.brevity.gmall.config.ActiveMQUtil;
import com.brevity.gmall.config.RedisUtil;
import com.brevity.gmall.order.mapper.OrderDetailMapper;
import com.brevity.gmall.order.mapper.OrderInfoMapper;
import com.brevity.gmall.service.OrderService;
import com.brevity.gmall.service.PaymentService;
import com.brevity.gmall.util.HttpClientUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private ActiveMQUtil activeMQUtil;
    @Reference
    private PaymentService paymentService;

    @Override
    @Transactional
    public String saveOrderInfo(OrderInfo orderInfo) {
        orderInfo.sumTotalAmount();
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        // 生成的交易编号
        String outTradeNo = "brevity" + System.currentTimeMillis() + "" + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setCreateTime(new Date());
        Calendar calendar = Calendar.getInstance();
        // 过期时间是当前系统时间加一天
        calendar.add(Calendar.DATE, 1);
        orderInfo.setExpireTime(calendar.getTime());
        // 订单状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);
        // 保存订单
        orderInfoMapper.insertSelective(orderInfo);
        // 保存订单明细
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if (orderDetailList != null && orderDetailList.size() > 0) {
            for (OrderDetail orderDetail : orderDetailList) {
                orderDetail.setId(null);
                orderDetail.setOrderId(orderInfo.getId());
                orderDetailMapper.insertSelective(orderDetail);
            }
        }
        return orderInfo.getId();
    }

    @Override
    public String getTradeNo(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:" + userId + "tradeCode";
        String tradeNo = UUID.randomUUID().toString().replace("-", "");
        // 放入缓存
        jedis.set(tradeNoKey, tradeNo);
        jedis.close();
        return tradeNo;
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCodeNo) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:" + userId + ":tradeCode";
        String tradeNoRedis = jedis.get(tradeNoKey);
        return tradeCodeNo.equals(tradeNoRedis);
    }

    @Override
    public void deleteTradeCode(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:" + userId + ":tradeCode";
        String tradeCode = jedis.get(tradeNoKey);
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        jedis.eval(script, Collections.singletonList(tradeNoKey), Collections.singletonList(tradeCode));
        jedis.close();
    }

    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);
        return "1".equals(result);
    }

    @Override
    public OrderInfo getOrderInfo(String orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        // 将订单明细放入订单中
        List<OrderDetail> orderDetails = orderDetailMapper.select(orderDetail);
        orderInfo.setOrderDetailList(orderDetails);
        return orderInfo;
    }

    @Override
    public void updateOrderStatus(String orderId, ProcessStatus processStatus) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(processStatus);
        orderInfo.setOrderStatus(processStatus.getOrderStatus());

        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);
    }

    @Override
    public void sendOrderStatus(String orderId) {
        Connection connection = activeMQUtil.getConnection();
        String order_Json = initWareOrder(orderId);
        try {
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue order_result_queue = session.createQueue("ORDER_RESULT_QUEUE");
            MessageProducer producer = session.createProducer(order_result_queue);
            ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
            activeMQTextMessage.setText(order_Json);
            producer.send(activeMQTextMessage);
            session.commit();

            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    // 获取发送的订单信息
    private String initWareOrder(String orderId) {
        OrderInfo orderInfo = getOrderInfo(orderId);

        // 将orderInfo中的部分字段放入map
        Map map = initWareOrder(orderInfo);

        return JSON.toJSONString(map);
    }

    // 将orderInfo中的部分字段放入map
    public Map initWareOrder(OrderInfo orderInfo) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("orderId", orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel", orderInfo.getConsigneeTel());
        map.put("orderComment", orderInfo.getOrderComment());
        map.put("orderBody", "自定义内容");
        map.put("deliverAddress", orderInfo.getDeliveryAddress());
        map.put("paymentWay", "2");
        map.put("wareId", orderInfo.getWareId());

        ArrayList<HashMap> hashMapArrayList = new ArrayList<>();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            HashMap<String, Object> detailMap = new HashMap<>();
            detailMap.put("skuId", orderDetail.getSkuId());
            detailMap.put("skuNum", orderDetail.getSkuNum());
            detailMap.put("skuName", orderDetail.getSkuName());
            hashMapArrayList.add(detailMap);
        }

        map.put("details", hashMapArrayList);
        return map;
    }

    @Override
    public List<OrderInfo> getExpiredOrderList() {
        // select * from order_info where expire_time < ?
        Example example = new Example(OrderInfo.class);
        example.createCriteria().andLessThan("expireTime", new Date()).andEqualTo("processStatus", ProcessStatus.UNPAID);
        return orderInfoMapper.selectByExample(example);
    }

    @Async  // 异步线程池轮询
    @Override
    public void escExpiredOrder(OrderInfo orderInfo) {
        updateOrderStatus(orderInfo.getId(), ProcessStatus.CLOSED);
        // 关闭交易记录
        paymentService.closePayment(orderInfo.getId());
    }

    @Override
    public List<OrderInfo> orderSplit(String orderId, String wareSkuMap) {
        /*
        1.根据orderId获取原始订单
        2.wareSkuMap类型转换
        3.创建新的子订单，并赋值
        4.子订单添加到集合中
        5.保存子订单到数据库
        6.修改原始订单的状态
         */
        List<OrderInfo> subOrderInfoList = new ArrayList<>();
        // 原始订单
        OrderInfo orderInfoOrigin = getOrderInfo(orderId);

        List<Map> maps = JSON.parseArray(wareSkuMap, Map.class);
        if (maps != null && maps.size() > 0) {
            for (Map map : maps) {
                // 仓库id
                String wareId = (String) map.get("wareId");
                // 商品的id
                List<String> skuIds = (List<String>) map.get("skuIds");
                // 创建新的子订单
                OrderInfo subOrderInfo = new OrderInfo();

                BeanUtils.copyProperties(orderInfoOrigin, subOrderInfo);
                subOrderInfo.setId(null);
                subOrderInfo.setParentOrderId(orderId);
                subOrderInfo.setWareId(wareId);

                List<OrderDetail> orderDetailList = orderInfoOrigin.getOrderDetailList();

                // 存储子订单明细
                ArrayList<OrderDetail> subOrderDetailList = new ArrayList<>();

                if (orderDetailList != null && orderDetailList.size() > 0) {
                    for (OrderDetail orderDetail : orderDetailList) {
                        for (String skuId : skuIds) {
                            if (skuId.equals(orderDetail.getSkuId())) {
                                subOrderDetailList.add(orderDetail);
                            }
                        }
                    }
                }

                // 将子订单明细赋值给子订单
                subOrderInfo.setOrderDetailList(subOrderDetailList);
                // 重新计算子订单的金额
                subOrderInfo.sumTotalAmount();
                // 将新的子订单放入子订单集合
                subOrderInfoList.add(subOrderInfo);
                // 保存子订单
                saveOrderInfo(subOrderInfo);
            }
        }

        // 修改原始订单的状态
        updateOrderStatus(orderId, ProcessStatus.SPLIT);

        return subOrderInfoList;
    }
}
