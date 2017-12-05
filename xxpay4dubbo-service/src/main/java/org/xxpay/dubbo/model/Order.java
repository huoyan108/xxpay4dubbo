package org.xxpay.dubbo.model;

/**
 * 数据库需要的信息类
 * 
 * @author qiyu
 * @date 2017年12月4日 下午7:30:25
 * @version 1.0
 */
public class Order {

	/** 订单号 */
	private String outTradeNo;

	/** 总金额 */
	private String totalFee;

	/** 交易类型,支付宝?微信 */
	private String payType;

	/** 第三方订单号 */
	private String tradeNo;

	/** 交易时间 */
	private String date;

	/** 订单状态 */
	private String state;

	/** 第三方下用户唯一标识 */
	private String openId;

	/** 商户编号 */
	private String storeId;

	/** 店员编号 */
	private String operatorId;

	/** 设备编号 */
	private String equipmentNumber;

	/** 设备类型 */
	private String equipmentType;

	/** 第三方错误编号 */
	private String errorCode;

	/** 第三方错误描述 */
	private String errorDesc;

	/** 数据库返回码 */
	private String returnCode;

	/** 数据库返回描述 */
	private String returnDesc;

	public String getOutTradeNo() {
		return outTradeNo;
	}

	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}

	public String getTotalFee() {
		return totalFee;
	}

	public void setTotalFee(String totalFee) {
		this.totalFee = totalFee;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public String getTradeNo() {
		return tradeNo;
	}

	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public String getStoreId() {
		return storeId;
	}

	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	public String getEquipmentNumber() {
		return equipmentNumber;
	}

	public void setEquipmentNumber(String equipmentNumber) {
		this.equipmentNumber = equipmentNumber;
	}

	public String getEquipmentType() {
		return equipmentType;
	}

	public void setEquipmentType(String equipmentType) {
		this.equipmentType = equipmentType;
	}

	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorDesc() {
		return errorDesc;
	}

	public void setErrorDesc(String errorDesc) {
		this.errorDesc = errorDesc;
	}

	public String getReturnCode() {
		return returnCode;
	}

	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}

	public String getReturnDesc() {
		return returnDesc;
	}

	public void setReturnDesc(String returnDesc) {
		this.returnDesc = returnDesc;
	}

	@Override
	public String toString() {
		return "Order [outTradeNo=" + outTradeNo + ", totalFee=" + totalFee + ", payType=" + payType + ", tradeNo="
				+ tradeNo + ", date=" + date + ", state=" + state + ", openId=" + openId + ", storeId=" + storeId
				+ ", operatorId=" + operatorId + ", equipmentNumber=" + equipmentNumber + ", equipmentType="
				+ equipmentType + ", errorCode=" + errorCode + ", errorDesc=" + errorDesc + ", returnCode=" + returnCode
				+ ", returnDesc=" + returnDesc + "]";
	}

}
