package org.xxpay.dubbo.service.mq;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ScheduledMessage;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;
import org.xxpay.common.util.DateUtil;

/**
 * 
 * @author qiyu
 * @date 2017年11月30日 上午10:34:18
 * @version 1.0
 */
@Component
public class Mq4DealQueryOrder {

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	@Qualifier(MqConfig.DELAY_QUERY_ORDER)
	private Queue dealQueryOrderQueue;

	@Autowired
	@Qualifier(MqConfig.WeChar_DELAY_QUERY)
	private Queue wecharDealayQuery;

	private Log log = LogFactory.getLog(Mq4DealQueryOrder.class);

	/**
	 * 发送延迟消息
	 * 
	 * @param msg
	 *            消息
	 * @param delay
	 *            等待时间
	 */
	public void send(String msg, long delay) {
		log.info("发送MQ延时消息时间为:" + DateUtil.getCurrentDate() + ",msg={}" + msg + ",delay:" + delay);
		jmsTemplate.send(this.dealQueryOrderQueue, new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				TextMessage message = session.createTextMessage(msg);
				// 等待的时间
				message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delay);
				;
				// 重新开始之前的等待
				message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_PERIOD, 1 * 1000);
				// 从新发送的次数
				message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_REPEAT, 1);
				return message;
			}
		});
	}

	/**
	 * 微信延迟发送查询消息
	 * 
	 * @param msg
	 * @param delay
	 */
	public void wecharSend(String msg, long delay) {
		log.info("发送MQ延时消息时间为:" + DateUtil.getCurrentDate() + ",msg={}" + msg + ",delay:" + delay);
		jmsTemplate.send(this.wecharDealayQuery, new MessageCreator() {
			public Message createMessage(Session session) throws JMSException {
				TextMessage message = session.createTextMessage(msg);
				// 等待的时间
				message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delay);
				// 重新开始之前的等待
				message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_PERIOD, 1 * 1000);
				// 从新发送的次数
				message.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_REPEAT, 1);
				return message;
			}
		});
	}
}
