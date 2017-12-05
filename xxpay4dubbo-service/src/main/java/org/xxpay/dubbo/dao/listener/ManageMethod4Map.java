package org.xxpay.dubbo.dao.listener;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xxpay.common.util.ClientCustomSSL;
import org.xxpay.common.util.DateUtil;
import org.xxpay.common.util.JsonUtil;
import org.xxpay.common.util.PayUtils;
import org.xxpay.dubbo.model.Order;
import org.xxpay.dubbo.model.WecharMapModel;
import org.xxpay.dubbo.service.mq.Mq4PayOrder;
import com.hongsou.config.Constants;
import com.hongsou.config.WecharConfig;

/**
 * 一些微信判断的公用方法,抽取处理啊
 * 
 * @author qiyu
 * @date 2017年12月5日 上午11:58:00
 * @version 1.0
 */
public class ManageMethod4Map {

	private final static Log log = LogFactory.getLog(ManageMethod4Map.class);

	/**
	 * 发送成功消息到队列,同步数据库
	 * 
	 * @param result
	 *            微信结果集
	 */
	public static void sendOrderSuccess(Mq4PayOrder orderMessage, Map<String, String> result) {
		Order order = new Order();
		order.setOpenId(result.get("openid"));
		order.setOutTradeNo(result.get("out_trade_no"));
		order.setTradeNo(result.get("transaction_id"));
		order.setPayType("微信支付");
		order.setTotalFee(result.get("total_fee"));
		order.setDate(DateUtil.MyDate4Sql());
		String equipment = result.get("device_info");
		order.setEquipmentType(PayUtils.getValue(equipment).get(0));
		order.setEquipmentNumber(PayUtils.getValue(equipment).get(1));
		String store = result.get("attach");
		order.setOperatorId(PayUtils.getValue(store).get(0));
		order.setStoreId(PayUtils.getValue(store).get(1));
		// 成功
		order.setState("1");
		order.setErrorDesc(result.get("trade_state_desc"));
		orderMessage.sendSuccessOrder(JsonUtil.object2Json(order));
	}

	/**
	 * 用户调用撤销
	 * 
	 * @param map
	 * @return 调用状态
	 * @throws Exception
	 */
	public static String reverseOrder(Map map) throws Exception {
		Map request = WecharMapModel.getRequestMap();
		request.remove("spbill_create_ip");
		request.put("out_trade_no", map.get("out_trade_no"));
		request.put("sub_mch_id", map.get("sub_mch_id"));
		request.put("sign", PayUtils.createSign(request, WecharConfig.getPrivateKey()));
		String strReverse = ClientCustomSSL.doRefund(WecharConfig.getOrderReverse(), PayUtils.toXml(request));
		Map<String, String> result = PayUtils.xmlToMap(strReverse);
		log.info("撤销订单:");
		PayUtils.MapToString(result);
		if (reverseIsSuccess(result)) {
			// 告诉订单支付失败.撤销成功
			log.info("一次撤销成功");
			return Constants.SUCCESS;
		}
		if (reverseIsRecall(result)) {
			String reverseOrderAgainStr = ClientCustomSSL.doRefund(WecharConfig.getOrderReverse(), PayUtils.toXml(request));
			Map<String, String> reverseOrderAgain = PayUtils.xmlToMap(reverseOrderAgainStr);
			if (reverseIsSuccess(reverseOrderAgain)) {
				// 告诉订单支付失败.撤销成功
				log.info("二次撤销成功");
				return Constants.SUCCESS;
			}
		}
		return Constants.FAILED;
	}

	/**
	 * 是否重调的方法撤销的方法
	 * 
	 * @param reverseOrder
	 * @return
	 */
	public static boolean reverseIsRecall(Map<String, String> reverseOrder) {
		if (PayUtils.codeIsOK(reverseOrder.get("return_code")) && Constants.RECALL.equals(reverseOrder.get("recall"))) {
			return true;
		}
		return false;
	}

	/**
	 * 撤销是成功
	 * 
	 * @param reverseOrder
	 * @return
	 */
	public static boolean reverseIsSuccess(Map<String, String> reverseOrder) {
		if (PayUtils.codeIsOK(reverseOrder.get("return_code")) && PayUtils.codeIsOK(reverseOrder.get("result_code"))) {
			return true;
		}
		return false;
	}

	/**
	 * 结果信息,结果为Constants中的信息
	 * 
	 * @param info
	 *            最终信息
	 * @param result
	 *            成功,失败.或者错误.
	 * @return
	 */
	public static Map<String, String> finalResult(Map<String, String> info, String result) {
		info.put("msg", result);
		return info;
	}

}
