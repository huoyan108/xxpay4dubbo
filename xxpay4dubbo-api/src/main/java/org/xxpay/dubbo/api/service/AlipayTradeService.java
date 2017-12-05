package org.xxpay.dubbo.api.service;

import com.alipay.api.AlipayApiException;
import com.hongsou.alipay.model.AlipayTradePayRequestBuilder;
import com.hongsou.alipay.model.AlipayTradePrecreateRequestBuilder;
import com.hongsou.alipay.model.AlipayTradeQueryRequestBuilder;
import com.hongsou.alipay.model.AlipayTradeRefundRequestBuilder;
import com.hongsou.trade.result.AlipayF2FPayResult;
import com.hongsou.trade.result.AlipayF2FPrecreateResult;
import com.hongsou.trade.result.AlipayF2FQueryResult;
import com.hongsou.trade.result.AlipayF2FRefundResult;

public interface AlipayTradeService {

	/**
	 * 当面付2.0流程支付
	 * 
	 * @param builder
	 * @return
	 * @throws AlipayApiException 
	 */
	 AlipayF2FPayResult tradePay(AlipayTradePayRequestBuilder builder) throws AlipayApiException;

	/**
	 * 当面付2.0消费查询
	 * 
	 * @param builder
	 * @return
	 */
	 AlipayF2FQueryResult queryTradeResult(AlipayTradeQueryRequestBuilder builder);

	/**
	 * 当面付2.0消费退款
	 * 
	 * @param builder
	 * @return
	 */
	 AlipayF2FRefundResult tradeRefund(AlipayTradeRefundRequestBuilder builder);

	/**
	 * 当面付2.0预下单(生成二维码)
	 * 
	 * @param builder
	 * @return
	 */
	 AlipayF2FPrecreateResult tradePrecreate(AlipayTradePrecreateRequestBuilder builder);
}
