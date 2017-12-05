package org.xxpay.dubbo.dao.listener;

import java.util.LinkedHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Repository;
import org.xxpay.common.util.JdbcBean;
import org.xxpay.common.util.JsonUtil;
import org.xxpay.common.util.ResultBean;
import org.xxpay.dubbo.model.Order;
import org.xxpay.dubbo.service.mq.MqConfig;
import org.xxpay.dubbo.service.redis.RedisService;

import com.hongsou.config.Constants;

/**
 * 处理成功订单
 * 
 * @author qiyu
 * @date 2017年12月4日 下午8:02:44
 * @version 1.0
 */
@Repository
public class OrderSuccessListener {

	private final static Log log = LogFactory.getLog(OrderSuccessListener.class);

	@Autowired
	private DataDao dao;

	/**
	 * 监听成功的订单消息
	 * 
	 * @param order
	 * @return
	 */
	@JmsListener(destination = MqConfig.SUCCESS_ORDER)
	public String manageOrder(String orderStr) {
		log.info("接受到微信的支付成功订单消息");
		Order order = JsonUtil.getObjectFromJson(orderStr, Order.class);
		JdbcBean jdbcBean = new JdbcBean();
		LinkedHashMap<String, Object> param = new LinkedHashMap<>();
		// 订单号
		param.put("outTradeNo", order.getOutTradeNo());
		// 支付宝交易号
		param.put("tradeNo", order.getTradeNo());
		// 日期时间,没有用支付宝的日期,跟我们数据库格式不对应
		param.put("date", order.getDate());
		// 成功 1. 失败 -1
		param.put("state", order.getState());
		// 金额转换成分
		param.put("totalAmount", order.getTotalFee());
		param.put("PayType", order.getPayType());
		param.put("openId", order.getOpenId());
		param.put("bankId", "");
		param.put("shopNum", order.getStoreId());
		System.out.println("店员编号:" + order.getOperatorId());
		param.put("clerkNumber", order.getOperatorId());
		System.out.println("设备编号:" + order.getEquipmentNumber());
		// 设备编号
		param.put("equipmentNumber", order.getEquipmentNumber());
		System.out.println("设备类型:" + order.getEquipmentType());
		// --0 post机，1 扫码枪，2
		param.put("equipmentType", order.getEquipmentType());
		// 支付宝那边的错误信息
		param.put("errorCode", order.getErrorCode());
		param.put("errorDesc", order.getErrorDesc());
		param.put(jdbcBean.getOuput("returnCode"), "");
		param.put(jdbcBean.getOuput("returnDesc"), "");
		jdbcBean.setParam(param);
		jdbcBean.setProc("[hongsou].[Interface_Transaction_Receipt]	");
		ResultBean jdbcData = dao.getJdbcData(jdbcBean);
		String code = (String) param.get(jdbcBean.getOuput("returnCode"));
		String desc = (String) param.get(jdbcBean.getOuput("returnDesc"));
		if ("0001".equals(code) && code != null) {
			log.info("数据库返回描述:" + desc + ",订单生成失败,订单信息" + order.toString());
			return Constants.ERROR;
		}
		return Constants.SUCCESS;
	}
}
