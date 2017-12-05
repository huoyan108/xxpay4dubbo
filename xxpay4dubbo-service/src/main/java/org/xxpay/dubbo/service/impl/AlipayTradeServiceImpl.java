package org.xxpay.dubbo.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.common.util.DateUtil;
import org.xxpay.common.util.ExtendParams;
import org.xxpay.common.util.JsonUtil;
import org.xxpay.dubbo.api.service.AlipayTradeService;
import org.xxpay.dubbo.dao.listener.OrderJdbc;
import org.xxpay.dubbo.model.AliModel;
import org.xxpay.dubbo.service.mq.Mq4DealQueryOrder;
import org.xxpay.dubbo.service.redis.RedisService;

import com.alibaba.dubbo.config.annotation.Service;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayResponse;
import com.alipay.api.request.AlipayTradeCancelRequest;
import com.alipay.api.request.AlipayTradePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeCancelResponse;
import com.alipay.api.response.AlipayTradePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.hongsou.alipay.model.AlipayTradePayRequestBuilder;
import com.hongsou.alipay.model.AlipayTradePrecreateRequestBuilder;
import com.hongsou.alipay.model.AlipayTradeQueryRequestBuilder;
import com.hongsou.alipay.model.AlipayTradeRefundRequestBuilder;
import com.hongsou.alipay.model.RequestBuilder;
import com.hongsou.alipay.status.TradeStatus;
import com.hongsou.config.AliConfig;
import com.hongsou.config.Constants;
import com.hongsou.trade.result.AlipayF2FPayResult;
import com.hongsou.trade.result.AlipayF2FPrecreateResult;
import com.hongsou.trade.result.AlipayF2FQueryResult;
import com.hongsou.trade.result.AlipayF2FRefundResult;

/**
 * 当面付实现逻辑 #@Service这个注解提供服务
 * 
 * @author qiyu
 * @date 2017年11月29日 下午3:54:29
 * @version 1.0
 */
@Service(version = "1.0.0")
public class AlipayTradeServiceImpl implements AlipayTradeService {

	private static Log log = LogFactory.getLog(AlipayTradeServiceImpl.class);

	private static AliModel aliModel;

	/** 延迟查询订单消息 */
	@Autowired
	private Mq4DealQueryOrder queryOrder;

	@Autowired
	private OrderJdbc orderDao;

	@Autowired
	private RedisService redisService;

	/**
	 * 读取配置文件,初始化请求对象
	 */
	static {
		AliConfig.init("shared/ali.properties");
		aliModel = AliModel.getInstance();
	}

	/**
	 * 设置builder的参数进request
	 * @throws AlipayApiException 
	 */
	@Override
	public AlipayF2FPayResult tradePay(AlipayTradePayRequestBuilder builder) throws AlipayApiException {
		String outTradeNo = DateUtil.getOutTradeNo();
		// 打印订单号
		System.out.println(outTradeNo);
		builder.setOutTradeNo(outTradeNo);
		// 页面传
		builder.setStoreId("1000171128300028758");
		/**
		 * 页面传 --0(pos机)，1(扫码强)，2(桌牌) 查询[hongsou].[2.0_设备_类型对应表]
		 */
		builder.setTerminalType("0");
		/**
		 * 页面给 数据库里的设备码ASMZF147EED
		 */
		builder.setTerminalId("ASMZF147EED");
		// 页面给
		builder.setAppAuthToken("201711BB3623768a6b414f37855f4968bca2aC78");
		// 主题 店铺的店员卖的什么东西
		builder.setSubject("店铺的店员卖的什么东西");
		ExtendParams params = new ExtendParams();
		// 签约我们服务商Id,先用沙箱里自己的id
		params.setSysServiceProviderId(AliConfig.getPid());
		builder.setExtendParams(params);
		AlipayTradePayRequest request = new AlipayTradePayRequest();
		// 回调地址
		request.setNotifyUrl(AliConfig.getNotify());
		// 商户token
		request.putOtherTextParam("app_auth_token", builder.getAppAuthToken());
		// 设置业务参数
		request.setBizContent(builder.toJsonString());
		log.info("trade.pay bizContent:" + request.getBizContent());
		AlipayTradePayResponse response = (AlipayTradePayResponse) getResponse(aliModel, request);
		// 打印
		System.out.println(response.getBody());
		AlipayF2FPayResult result = new AlipayF2FPayResult(response);
		// 明确成功
		if (response != null && Constants.SUCCESS.equals(response.getCode())) {
			// 支付交易明确成功
			orderDao.orderSuccess(JsonUtil.object2Json(response), JsonUtil.object2Json(builder));
			result.setTradeStatus(TradeStatus.SUCCESS);
		} else if (response != null && Constants.PAYING.equals(response.getCode())) {
			// 订单信息放入redis.用回调地址处理.
			redisService.setObj(outTradeNo, builder);
			// 即使支付为支付中的标记
			redisService.setObj(builder.getOutTradeNo()+Constants.PAYING, Constants.WAIT_PAY);
			// 返回用户处理中，则调用延迟消息队列查询5秒中，如果查询超时，则调用撤销
			queryOrder.send(outTradeNo, AliConfig.getQueryDuration());
			log.info("用户支付中");
			result.setTradeStatus(TradeStatus.PAYING);
		} else if (tradeError(response)) {
			// 系统错误，则查询一次交易，如果交易没有支付成功，则调用撤销
			log.info("系统错误,人工通知");
			disposeRrrorOrder(builder, aliModel);
			// 状态未知咋处理
			result.setTradeStatus(TradeStatus.UNKNOWN);
		} else {
			// 其他情况表明该订单支付明确失败
			log.info("订单支付失败");
			result.setTradeStatus(TradeStatus.FAILED);
		}
		return result;
	}

	@Override
	public AlipayF2FQueryResult queryTradeResult(AlipayTradeQueryRequestBuilder builder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AlipayF2FRefundResult tradeRefund(AlipayTradeRefundRequestBuilder builder) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AlipayF2FPrecreateResult tradePrecreate(AlipayTradePrecreateRequestBuilder builder) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 执行请求
	 * 
	 * @param client
	 * @param request
	 * @return
	 */
	private AlipayResponse getResponse(AliModel client, AlipayTradePayRequest request) {
		try {
			AlipayResponse response = client.execute(request);
			if (response != null) {
				log.info(response.getBody());
			}
			return response;

		} catch (AlipayApiException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * 交易异常,
	 * 
	 * @param response
	 * @return
	 */
	private boolean tradeError(AlipayTradePayResponse response) {
		return response == null || Constants.ERROR.equals(response.getCode());
	}

	/**
	 * 异常订单处理方法, 查询订单异常.再查一次.成功同步数据库,未知状态调用撤销.
	 * 
	 * @param builder
	 * @param model
	 * @throws AlipayApiException
	 */
	public void disposeRrrorOrder(AlipayTradePayRequestBuilder builder, AliModel model) throws AlipayApiException {
		AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
		request.putOtherTextParam("app_auth_token", builder.getAppAuthToken());
		request.setBizContent("{" + "\"out_trade_no\":\"" + builder.getOutTradeNo() + "\"" + "}");
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
