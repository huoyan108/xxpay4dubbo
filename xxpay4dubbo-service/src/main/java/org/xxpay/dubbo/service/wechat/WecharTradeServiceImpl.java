package org.xxpay.dubbo.service.wechat;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.common.util.AmountUtil;
import org.xxpay.common.util.DateUtil;
import org.xxpay.common.util.HttpKit;
import org.xxpay.common.util.JsonUtil;
import org.xxpay.common.util.PayUtils;
import org.xxpay.dubbo.api.service.WecharTradeService;
import org.xxpay.dubbo.dao.listener.ManageMethod4Map;
import org.xxpay.dubbo.model.Order;
import org.xxpay.dubbo.model.WecharMapModel;
import org.xxpay.dubbo.service.mq.Mq4DealQueryOrder;
import org.xxpay.dubbo.service.mq.Mq4PayOrder;
import org.xxpay.dubbo.service.redis.RedisService;
import com.alibaba.dubbo.config.annotation.Service;
import com.hongsou.alipay.model.AlipayTradePayRequestBuilder;
import com.hongsou.config.Constants;
import com.hongsou.config.WecharConfig;

@Service(version = "1.0.0")
public class WecharTradeServiceImpl implements WecharTradeService {

	private static final Log log = LogFactory.getLog(WecharTradeServiceImpl.class);

	@Autowired
	private Mq4PayOrder orderMessage;

	@Autowired
	private Mq4DealQueryOrder delayQuery;

	@Autowired
	private RedisService redisService;

	/**
	 * 微信刷卡支付
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Map tradePay(AlipayTradePayRequestBuilder builder) throws Exception {
		Map map = WecharMapModel.getRequestMap();
		// 子商户公众帐号
		map.put("sub_appid", "");
		// 子商户号
		map.put("sub_mch_id", "1491740052");
		// 设备号,这里用设备类型+设备号做传输
		map.put("device_info", PayUtils.createString("0", "ASMZF147EED"));
		map.put("body", "商户-门店-东西");
		String outTradeNo = DateUtil.getOutTradeNo();
		builder.setOutTradeNo(outTradeNo);
		log.info("订单号:" + outTradeNo);
		map.put("out_trade_no", outTradeNo);
		// 总金额
		map.put("total_fee", AmountUtil.convertDollar2Cent(builder.getTotalAmount()));
		// 条码
		map.put("auth_code", builder.getAuthCode());
		// 额外信息.放了店员店铺id
		map.put("attach", PayUtils.createString(builder.getOperatorId(), "1000171128300028758"));
		map.put("sign", PayUtils.createSign(map, WecharConfig.getPrivateKey()));
		String xmlStr = HttpKit.post(WecharConfig.getMicroPay(), PayUtils.toXml(map));
		Map<String, String> result = PayUtils.xmlToMap(xmlStr);
		log.info("被调用成功打印map:");
		
		PayUtils.MapToString(result);
		
		if (PayUtils.codeIsOK(result.get("return_code")) && PayUtils.codeIsOK(result.get("result_code"))) {
			// 用户支付成功,发送消息同步数据库,响应页面.
			ManageMethod4Map.sendOrderSuccess(orderMessage, result);
			return ManageMethod4Map.finalResult(result, Constants.SUCCESS);
		} else if (Constants.WECHAR_PAYING.equals(result.get("err_code"))) {
			redisService.setObj(outTradeNo, Constants.PAYING);
			// 用户支付中,延迟消息
			delayQuery.wecharSend(JsonUtil.object2Json(builder), WecharConfig.getQueryDuration());
			return ManageMethod4Map.finalResult(result, Constants.WAIT_PAY);
		} else if (Constants.SYSTEMERROR.equals(result.get("err_code"))
				|| Constants.BANKERROR.equals(result.get("err_code"))) {
			// 订单状态未知,查询一次.成功还是失败.
			Map<String, String> queryErrorOrderResult = PayUtils.queryOrderResult(map);
			if (PayUtils.tradeSuccess(queryErrorOrderResult)) {
				/**
				 * 这里如果用户还在支付中.怎么处理????
				 */
				log.info("延迟查询异常.再次查询成功的记录时间:" + DateUtil.getCurrentDate());
				ManageMethod4Map.sendOrderSuccess(orderMessage, result);
				// 用户支付成功
				return ManageMethod4Map.finalResult(result, Constants.SUCCESS);
			} else {
				// 调用撤销
				if (Constants.FAILED.equals(ManageMethod4Map.reverseOrder(map))) {
					// 最好要通知管理员.通知店员
					log.error("撤销失败,订单号:" + map.get("out_trade_no"));
				}
			}
		}
		return ManageMethod4Map.finalResult(result, Constants.FAILED);
	}

}
