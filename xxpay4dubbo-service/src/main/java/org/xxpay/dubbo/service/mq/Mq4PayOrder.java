package org.xxpay.dubbo.service.mq;

import javax.jms.Queue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

/**
 * 数据库插入订单,消息队列的重复发送
 * #@Scheduled(fixedDelay=3000)//每3s执行1次
 * 
 * @author qiyu
 * @date 2017年11月30日 上午10:18:35
 * @version 1.0
 */

@Component
public class Mq4PayOrder {

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	@Qualifier(MqConfig.CREATE_PAY_ORDER)
	private Queue createPayOrderQueue;
	
	@Autowired
	@Qualifier(MqConfig.SUCCESS_ORDER)
	private Queue orderSuccessQueue;

	private Log log = LogFactory.getLog(Mq4PayOrder.class);

	/**
	 * 发送消息创建订单
	 * 
	 * @param msg
	 */
	public void sendCreateOrder(String msg) {
		log.info("发送MQ消息:msg={}" + msg);
		this.jmsTemplate.convertAndSend(this.createPayOrderQueue, msg);
	}

	/**
	 * 发送订单成功
	 * 
	 * @param msg
	 */
	public void sendSuccessOrder(String msg) {
		log.info("发送成功MQ消息:msg={}" + msg);
		this.jmsTemplate.convertAndSend(this.orderSuccessQueue, msg);
	}
}
