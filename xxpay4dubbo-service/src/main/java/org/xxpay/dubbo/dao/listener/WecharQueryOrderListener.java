package org.xxpay.dubbo.dao.listener;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import org.xxpay.common.util.ClientCustomSSL;
import org.xxpay.common.util.DateUtil;
import org.xxpay.common.util.HttpKit;
import org.xxpay.common.util.JsonUtil;
import org.xxpay.common.util.PayUtils;
import org.xxpay.dubbo.model.Order;
import org.xxpay.dubbo.model.WecharMapModel;
import org.xxpay.dubbo.service.mq.Mq4DealQueryOrder;
import org.xxpay.dubbo.service.mq.Mq4PayOrder;
import org.xxpay.dubbo.service.mq.MqConfig;
import org.xxpay.dubbo.service.redis.RedisService;
import com.hongsou.alipay.model.AlipayTradePayRequestBuilder;
import com.hongsou.config.Constants;
import com.hongsou.config.WecharConfig;

/**
 * 微信的延迟消息查询监听者
 * 
 * @author qiyu
 * @date 2017年12月4日 下午8:36:51
 * @version 1.0
 */
@Component
public class WecharQueryOrderListener {

	private final static Log log = LogFactory.getLog(WecharQueryOrderListener.class);

	@Value("${order_PayingTime}")
	private String payingTime;

	@Autowired
	private RedisService redisService;

	@Autowired
	private Mq4PayOrder orderMessage;

	/** 延迟查询订单消息 */
	@Autowired
	private Mq4DealQueryOrder queryOrder;

	/**
	 * 
	 * @param msg
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@JmsListener(destination = MqConfig.WeChar_DELAY_QUERY)
	public void wecharDealyQuery(String builderStr) throws Exception {
		log.info("接收到了延迟消息,时间:" + DateUtil.getCurrentDate());
		AlipayTradePayRequestBuilder builder = JsonUtil.getObjectFromJson(builderStr,
				AlipayTradePayRequestBuilder.class);
		Map map = WecharMapModel.getRequestMap();
		map.put("sub_mch_id", "1491740052");
		// 子商户公众账户id
		map.put("sub_appid", "");
		map.put("out_trade_no", builder.getOutTradeNo());
		// 微信订单号
		map.put("transaction_id", "");
		map.put("sign", PayUtils.createSign(map, WecharConfig.getPrivateKey()));
		Map<String, String> result = PayUtils.queryOrderResult(map);
		log.info("打印延迟结果:");
		PayUtils.MapToString(result);
		if (PayUtils.tradeSuccess(result)) {
			log.info("延迟查询订单支付成功");

			/**
			 * 我发送一次执行.数据库执行多次.
			 */
			ManageMethod4Map.sendOrderSuccess(orderMessage, result);
			return;
		} else if (userPaying(result)) {
			// 第一次还是支付中.设置一个订单时间
			if (Constants.PAYING.equals(redisService.getObj(result.get("out_trade_no")))) {
				log.info("支付中1");
				queryOrder.wecharSend(builderStr, WecharConfig.getQueryDuration());
				redisService.setExpire(result.get("out_trade_no"), Constants.WECHAR_PAYING, Long.valueOf(payingTime));
				// 判断订单还存在不,存在继续查询
			} else if (Constants.WECHAR_PAYING.equals(redisService.getObj(result.get("out_trade_no")))) {
				log.info("支付中2");
				queryOrder.wecharSend(builderStr, WecharConfig.getQueryDuration());
			} else {
				// 不存在就该调用撤销了
				if (Constants.FAILED.equals(ManageMethod4Map.reverseOrder(map))) {
					// 最好要通知管理员.通知店员
					log.error("撤销失败,订单号:" + map.get("out_trade_no"));
				}

			}
		} else if (TradeError(result)) {
			// 第一次查询异常,再次查询,成功同步数据库.
			Map<String, String> queryerrorOrderResult = PayUtils.queryOrderResult(map);
			if (PayUtils.tradeSuccess(queryerrorOrderResult)) {
				/**
				 * 这里如果用户还在支付中.怎么处理????
				 */
				log.info("延迟查询异常.再次查询成功的记录时间:" + DateUtil.getCurrentDate());
				ManageMethod4Map.sendOrderSuccess(orderMessage, result);
			} else {
				// 调用撤销
				if (Constants.FAILED.equals(ManageMethod4Map.reverseOrder(map))) {
					// 最好要通知管理员.通知店员
					log.error("撤销失败,订单号:" + map.get("out_trade_no"));
				}
			}
		}
	}

	/**
	 * 系统异常
	 * 
	 * @param result
	 * @return
	 */
	private boolean TradeError(Map<String, String> result) {
		if (PayUtils.codeIsOK(result.get("return_code")) && Constants.SYSTEMERROR.equals(result.get("err_code"))) {
			return true;
		}
		return false;
	}

	/**
	 * 用户支付中
	 * 
	 * @param result
	 * @return
	 */
	private boolean userPaying(Map<String, String> result) {
		if (PayUtils.codeIsOK(result.get("return_code")) && PayUtils.codeIsOK(result.get("result_code"))
				&& Constants.WECHAR_PAYING.equals(result.get("trade_state"))) {
			return true;
		}
		return false;
	}

}
