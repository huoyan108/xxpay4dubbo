package com.hongsou.pojo;

import java.io.Serializable;

/**
 * 退款接口需要的请求参数构建的pojo
 * 
 * @author qiyu
 * @date 2017年11月9日 下午5:21:18
 * @version 1.0
 */
public class RefundPojo implements Serializable {

	/** 商户订单号 */
	private String outTradeNo;

	/** 支付宝订单号 */
	private String tradeNo;

	/** 退款金额 */
	private String refundAmount;

	/** 退款说明 */
	private String refundReason;

	/** 部分退款的时候用一个标识 目前感觉没用 */
	private String outRequestNo;

	/** 商户的操作员编号 */
	private String operatorId;

	/** 门店编号 */
	private String storeId;

	/** 商户的终端编号 */
	private String terminalId;

	/** 阿里商户的token */
	private String appAuthToken;

	public String getAppAuthToken() {
		return appAuthToken;
	}

	public void setAppAuthToken(String appAuthToken) {
		this.appAuthToken = appAuthToken;
	}

	public String getOutTradeNo() {
		return outTradeNo;
	}

	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}

	public String getTradeNo() {
		return tradeNo;
	}

	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

	public String getRefundAmount() {
		return refundAmount;
	}

	public void setRefundAmount(String refundAmount) {
		this.refundAmount = refundAmount;
	}

	public String getRefundReason() {
		return refundReason;
	}

	public void setRefundReason(String refundReason) {
		this.refundReason = refundReason;
	}

	public String getOutRequestNo() {
		return outRequestNo;
	}

	public void setOutRequestNo(String outRequestNo) {
		this.outRequestNo = outRequestNo;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public String getStoreId() {
		return storeId;
	}

	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	public String getTerminalId() {
		return terminalId;
	}

	public void setTerminalId(String terminalId) {
		this.terminalId = terminalId;
	}

}
