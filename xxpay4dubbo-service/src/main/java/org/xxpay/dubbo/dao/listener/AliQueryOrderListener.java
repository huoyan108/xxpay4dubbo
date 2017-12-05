package org.xxpay.dubbo.dao.listener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Repository;
import org.xxpay.common.util.DateUtil;
import org.xxpay.common.util.JsonUtil;
import org.xxpay.dubbo.model.AliModel;
import org.xxpay.dubbo.service.mq.Mq4DealQueryOrder;
import org.xxpay.dubbo.service.mq.MqConfig;
import org.xxpay.dubbo.service.redis.RedisService;

import com.alibaba.druid.support.json.JSONUtils;
import com.alipay.api.AlipayApiException;
import com.alipay.api.request.AlipayTradeCancelRequest;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCancelResponse;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.hongsou.alipay.model.AlipayTradePayRequestBuilder;
import com.hongsou.alipay.model.RequestBuilder;
import com.hongsou.config.AliConfig;
import com.hongsou.config.Constants;

/**
 * 查询订单监听
 * 
 * @author qiyu
 * @date 2017年12月2日 上午9:33:28
 * @version 1.0
 */
@Repository
public class AliQueryOrderListener {

	private final static Log log = LogFactory.getLog(AliQueryOrderListener.class);

	@Value("${order_PayingTime}")
	private String payingTime;

	@Autowired
	private OrderJdbc orderDao;

	@Autowired
	private RedisService redisService;

	/** 延迟查询订单消息 */
	@Autowired
	private Mq4DealQueryOrder queryOrder;

	/**
	 * 这是一个延迟消息监听者. 支付宝条码同步响应结果为支付中的订单
	 * 
	 * @param msg
	 * @throws AlipayApiException
	 */
	@JmsListener(destination = MqConfig.DELAY_QUERY_ORDER)
	public void queryOrder(String msg) throws AlipayApiException {
		log.info("接收到了延迟消息,时间:" + DateUtil.getCurrentDate());
		// 从缓存中拿出预置订单信息.
		AlipayTradePayRequestBuilder builder = (AlipayTradePayRequestBuilder) redisService.getObj(msg);
		AliModel model = AliModel.getInstance();
		AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
		request.putOtherTextParam("app_auth_token", builder.getAppAuthToken());
		request.setBizContent("{" + "\"out_trade_no\":\"" + builder.getOutTradeNo() + "\"" + "}");
		AlipayTradeQueryResponse response = model.execute(request);
		if (RequestBuilder.querySuccess(response)) {
			// 支付交易明确成功
			log.info("支付中的订单成功了,订单号:" + builder.getOutTradeNo()+"订单设备编号:"+builder.getTerminalId());
			System.out.println("设备类型:"+builder.getTerminalType());
			// ---------------------消息队列---------------
			orderDao.orderSuccess(JsonUtil.object2Json(response), JsonUtil.object2Json(builder));
			// 设置订单成功.存活消息30秒,应该调用响应成功的推送
			redisService.setExpire(builder.getOutTradeNo(), Constants.SUCCESS, 30L);
		} else if (RequestBuilder.orderPaying(response)) {
			// 查询还是发送中.
			if (Constants.WAIT_PAY.equals(redisService.getObj(builder.getOutTradeNo() + Constants.PAYING))) {
				// 用户还在支付中,
				queryOrder.send(msg, AliConfig.getQueryDuration());
				// 第一次还在支付中.在redis中设置一个存活时间.
				redisService.setExpire(builder.getOutTradeNo() + Constants.PAYING, Constants.PAYING,
						Long.parseLong(payingTime));
			} else if (Constants.PAYING
					.equals((String) redisService.getObj(builder.getOutTradeNo() + Constants.PAYING))) {
				// 用户还在支付中,
				queryOrder.send(msg, AliConfig.getQueryDuration());
			} else {
				// 然后调用关闭订单
				AlipayTradeCloseRequest tradeCloseRequest = new AlipayTradeCloseRequest();
				tradeCloseRequest.putOtherTextParam("app_auth_token", builder.getAppAuthToken());
				tradeCloseRequest.setBizContent("{" + "\"out_trade_no\":\"" + builder.getOutTradeNo() + "\","
						+ "\"+trade_no+\":\"" + response.getTradeNo() + "\"" + "}");
				AlipayTradeCloseResponse tradeCloseResponse = model.execute(tradeCloseRequest);
				if (!tradeCloseResponse.isSuccess()) {
					AlipayTradeCloseResponse tradeCloseAgain = model.execute(tradeCloseRequest);
					if (!tradeCloseAgain.isSuccess()) {
						log.error("订单关闭失败,订单号:" + builder.getOutTradeNo());
					}
				}
				log.info("关闭订单,订单号:" + builder.getOutTradeNo());
			}
		} else if (RequestBuilder.tradeError(response)) {
			disposeRrrorOrder(builder, model, request, response);
		} else {
			// 其余的都是查询失败,
			log.debug("延时查询失败,失败信息:" + response.getBody());
		}
	}

	/**
	 * 异常订单处理方法, 查询订单异常.再查一次.成功同步数据库,未知状态调用撤销.
	 * 
	 * @param builder
	 * @param model
	 * @param request
	 * @param response
	 * @throws AlipayApiException
	 */
	public void disposeRrrorOrder(AlipayTradePayRequestBuilder builder, AliModel model, AlipayTradeQueryRequest request,
			AlipayTradeQueryResponse response) throws AlipayApiException {
		AlipayTradeQueryResponse queryAgain = model.execute(request);
		if (RequestBuilder.querySuccess(queryAgain)) {
			log.info("支付中的订单查询一次异常,再次查询交易成功了,订单号:" + builder.getOutTradeNo());
			// 同步数据库
			orderDao.orderSuccess(JsonUtil.object2Json(queryAgain), JsonUtil.object2Json(builder));
			// 成功的数据.放到了redis,key为订单号.value是10000,代表订单成功.
			redisService.setExpire(builder.getOutTradeNo(), Constants.SUCCESS, 30L);
		} else if (RequestBuilder.tradeError(queryAgain)) {
			// 订单状态未知.调用撤销接口
			AlipayTradeCancelRequest cancelRequest = new AlipayTradeCancelRequest();
			cancelRequest.putOtherTextParam("app_auth_token", builder.getAppAuthToken());
			cancelRequest.setBizContent("{" + " \"out_trade_no\":\" " + builder.getOutTradeNo() + "\" " + "}");
			AlipayTradeCancelResponse cancelResponse = model.execute(cancelRequest);
			// 调用撤销成功.
			if (RequestBuilder.cancelSuccess(cancelResponse)) {
				// 标记订单失败.
				log.info("订单状态未知的,撤销成功订单");
			} else if (RequestBuilder.needRetry(cancelResponse) || RequestBuilder.tradeError(cancelResponse)) {
				// 订单需要重新撤销?订单撤销异常,在执行一边.
				AlipayTradeCancelResponse cancelAgainResponse = model.execute(cancelRequest);
				if (RequestBuilder.cancelSuccess(cancelAgainResponse)) {
					// 撤销成功.
					log.info("订单状态未知的,撤销成功订单");
				} else {
					// 需要做个报警处理.
					log.error("订单状态未知.调用撤销失败!!!订单号:" + builder.getOutTradeNo());
				}
			}
		}
	}
}
