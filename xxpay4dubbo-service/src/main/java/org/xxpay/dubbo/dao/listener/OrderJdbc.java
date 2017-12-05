package org.xxpay.dubbo.dao.listener;

import java.util.LinkedHashMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.xxpay.common.util.AmountUtil;
import org.xxpay.common.util.DateUtil;
import org.xxpay.common.util.JdbcBean;
import org.xxpay.common.util.JsonUtil;
import org.xxpay.common.util.ResultBean;

import com.alibaba.druid.support.json.JSONUtils;
import com.alipay.api.response.AlipayTradePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.hongsou.alipay.model.AlipayTradePayRequestBuilder;
import com.hongsou.config.Constants;

/**
 * 订单的处理数据持久层
 * 
 * @author qiyu
 * @date 2017年11月30日 上午11:05:41
 * @version 1.0
 */
@Repository
public class OrderJdbc {

	private final Log log = LogFactory.getLog(OrderJdbc.class);

	@Autowired
	private DataDao dao;

	/**
	 * 创建订单,原先使用监听.
	 * 
	 * @param builder
	 */

	public String createOrderMessage(String builder) {
		AlipayTradePayRequestBuilder bean = JsonUtil.getObjectFromJson(builder, AlipayTradePayRequestBuilder.class);
		JdbcBean jdbcBean = new JdbcBean();
		LinkedHashMap<String, Object> param = new LinkedHashMap<>();
		param.put("outTradeNo", bean.getOutTradeNo());
		System.out.println("创建的订单号" + bean.getOutTradeNo());
		// 元
		param.put("totalAmound", bean.getTotalAmount());
		param.put("payType", "支付宝");
		param.put("storeId", bean.getStoreId());
		param.put("operatorId", bean.getOperatorId());
		param.put("terminalId", bean.getTerminalId());
		/**
		 * --0(pos机)，1(扫码强)，2(桌牌) 查询[hongsou].[2.0_设备_类型对应表]
		 */
		param.put("terminalType", bean.getTerminalType());
		param.put(jdbcBean.getOuput("returnCode"), "");
		param.put(jdbcBean.getOuput("returnDesc"), "");
		jdbcBean.setParam(param);
		jdbcBean.setProc("[hongsou].[Interface_Transaction_Order]");
		ResultBean jdbcData = dao.getJdbcData(jdbcBean);
		String code = (String) param.get(jdbcBean.getOuput("returnCode"));
		String desc = (String) param.get(jdbcBean.getOuput("returnDesc"));
		if ("0001".equals(code) && code != null) {
			log.info("数据库描述:-->" + desc + "---<,订单生成失败,订单信息" + builder);
			return Constants.ERROR;
		}
		return Constants.SUCCESS;
	}

	/**
	 * 成功的订单,返回的结果都一样. 这里当时成功的.跟过一段时间经过查询成功的都直接插入数据库.
	 * 但是响应的response类型不一样.所以直接转换为JSON调用的. 响应的结果是一样的
	 * 
	 * @param alipayTradeQueryResponse
	 * @param builder
	 *            构建的请求条件
	 * @return
	 */
	public String orderSuccess(String alipayTradeQueryResponse, String builder) {
		AlipayTradeQueryResponse result = JsonUtil.getObjectFromJson(alipayTradeQueryResponse,
				AlipayTradeQueryResponse.class);
		AlipayTradePayRequestBuilder startOrder = JsonUtil.getObjectFromJson(builder,
				AlipayTradePayRequestBuilder.class);
		JdbcBean jdbcBean = new JdbcBean();
		LinkedHashMap<String, Object> param = new LinkedHashMap<>();
		// 订单号
		param.put("outTradeNo", result.getOutTradeNo());
		// 支付宝交易号
		param.put("tradeNo", result.getTradeNo());
		// 日期时间,没有用支付宝的日期,跟我们数据库格式不对应
		param.put("date", DateUtil.MyDate4Sql());
		// 成功 1. 失败 -1
		param.put("state", 1);
		// 金额转换成分
		param.put("totalAmount", AmountUtil.convertDollar2Cent(result.getTotalAmount()));
		param.put("PayType", "支付宝支付");
		param.put("openId", result.getOpenId());
		param.put("bankId", "");
		System.out.println("店铺编号:" + startOrder.getStoreId());
		param.put("shopNum", startOrder.getStoreId());
		System.out.println("店员编号:" + startOrder.getOperatorId());
		param.put("clerkNumber", startOrder.getOperatorId());
		System.out.println("设备编号:" + startOrder.getTerminalId());
		// 设备编号
		param.put("equipmentNumber", startOrder.getTerminalId());
		System.out.println("设备编号:" + startOrder.getTerminalType());
		// --0 post机，1 扫码枪，2
		param.put("equipmentType", startOrder.getTerminalType());
		// 支付宝那边的错误信息
		param.put("errorCode", result.getSubCode());
		param.put("errirDesc", result.getSubMsg());
		param.put(jdbcBean.getOuput("returnCode"), "");
		param.put(jdbcBean.getOuput("returnDesc"), "");
		jdbcBean.setParam(param);
		jdbcBean.setProc("[hongsou].[Interface_Transaction_Receipt]	");
		ResultBean res = dao.getJdbcData(jdbcBean);
		String code = (String) param.get(jdbcBean.getOuput("returnCode"));
		String desc = (String) param.get(jdbcBean.getOuput("returnDesc"));
		if ("0001".equals(code) && code != null) {
			log.info("数据库返回描述:" + desc + ",订单生成失败,订单信息" + result.getBody());
			return Constants.ERROR;
		}
		return Constants.SUCCESS;
	}

	/**
	 * 查询订单状态,支付成功的
	 * 
	 * @return
	 */
	public String payingOrder2Success(String alipayTradeQueryResponse, String builder) {
		return null;
	}

}
