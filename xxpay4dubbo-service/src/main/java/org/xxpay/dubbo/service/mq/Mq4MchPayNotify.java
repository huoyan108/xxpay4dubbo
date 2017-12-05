package org.xxpay.dubbo.service.mq;

import javax.jms.Queue;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

//交易结果通知
@Component
public class Mq4MchPayNotify  {

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	@Qualifier(MqConfig.SUCCESS_ORDER)
	private Queue successOrder;
	

	private Log log = LogFactory.getLog(Mq4PayOrder.class);

	/**
	 * 交易成功
	 * 
	 * @param msg
	 */
	public void sendSuccessOrder(String msg) {
		log.info("发送MQ消息:msg={}" + msg);
		this.jmsTemplate.convertAndSend(this.successOrder, msg);
	}

	@JmsListener(destination = MqConfig.SUCCESS_ORDER)
	public String manageOrder(String orderStr) {
		
		//更新数据库和redis
		return "";
	}
	
}
