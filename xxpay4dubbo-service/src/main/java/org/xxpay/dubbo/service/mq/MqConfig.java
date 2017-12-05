package org.xxpay.dubbo.service.mq;

import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.jms.Queue;

/**
 * 消息队列的消息
 * 
 * @author qiyu
 * @date 2017年11月30日 上午9:58:04
 * @version 1.0
 */
@Configuration
public class MqConfig {

	/** 创建订单 */
	public static final String CREATE_PAY_ORDER = "createPayOrder";

	/** 支付宝延迟查询订单 */
	public static final String DELAY_QUERY_ORDER = "delayQueryOrder";

	/** 微信延迟查询订单 */
	public static final String WeChar_DELAY_QUERY = "WecharDelay";

	/** 交易成功订单 */
	public static final String SUCCESS_ORDER = "successOrder";

	@Bean(name = CREATE_PAY_ORDER)
	public Queue createPayOrderQueue() {
		return new ActiveMQQueue(CREATE_PAY_ORDER);
	}
	
	@Bean(name=WeChar_DELAY_QUERY)
	public Queue wecharDealayQuery() {
		return new ActiveMQQueue(WeChar_DELAY_QUERY);
	}

	@Bean(name=DELAY_QUERY_ORDER)
	public Queue delayQueryOrder() {
		return new ActiveMQQueue(DELAY_QUERY_ORDER);
	}

	@Bean(name=SUCCESS_ORDER)
	public Queue successOrder() {
		return new ActiveMQQueue(SUCCESS_ORDER);
	}
}
