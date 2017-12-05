package com.hongsou.pojo;

import java.io.Serializable;

/**
 * 用于接受页面传过来的参数而封装的bean
 * 
 * @author qiyu
 * @date 2017年11月7日 上午9:58:27
 * @version V1.0
 */
public class TabbyPojo implements Serializable {

	private static final long serialVersionUID = 8905626868039671603L;

	/** 收银员id */
	private String operator_id;

	/** 商品的价格 */
	private double money;

	/** 扫描的条码编号 */
	private String tabby;

	/** 店铺编号 */
	private String storeId;
	
	/** 支付宝授权的令牌 */
	private String appAutoToken;
	
	/** 微信子商户号 */
	private String subMchId;
	
	/** 设备号 */
	private String deviceInfo;
	
	/** 自定义订单号 */
	private String outTradeNo;
	
	/** 微信订单号*/
	private String wecharTransactionId;
	
	/** 阿里订单号 */
	private String AlitradeNo;
	
	
	public String getOutTradeNo() {
		return outTradeNo;
	}

	public void setOutTradeNo(String outTradeNo) {
		this.outTradeNo = outTradeNo;
	}

	public String getWecharTransactionId() {
		return wecharTransactionId;
	}

	public void setWecharTransactionId(String wecharTransactionId) {
		this.wecharTransactionId = wecharTransactionId;
	}

	public String getAlitradeNo() {
		return AlitradeNo;
	}

	public void setAlitradeNo(String alitradeNo) {
		AlitradeNo = alitradeNo;
	}

	public String getDeviceInfo() {
		return deviceInfo;
	}

	public void setDeviceInfo(String deviceInfo) {
		this.deviceInfo = deviceInfo;
	}

	public String getAppAutoToken() {
		return appAutoToken;
	}

	public void setAppAutoToken(String appAutoToken) {
		this.appAutoToken = appAutoToken;
	}

	public String getSubMchId() {
		return subMchId;
	}

	public void setSubMchId(String subMchId) {
		this.subMchId = subMchId;
	}

	public String getStoreId() {
		return storeId;
	}

	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	public String getOperator_id() {
		return operator_id;
	}

	public void setOperator_id(String operator_id) {
		this.operator_id = operator_id;
	}

	public double getMoney() {
		return money;
	}

	public void setMoney(double money) {
		this.money = money;
	}

	public String getTabby() {
		return tabby;
	}

	public void setTabby(String tabby) {
		this.tabby = tabby;
	}

}
