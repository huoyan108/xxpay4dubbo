package com.hongsou.pojo;

import java.util.Date;

/**
 * 支付订单实体类
 * 
 * @author qiyu
 * @date 2017年11月18日 下午1:41:23
 * @version 1.0
 */
public class Order {

	// 订单流水
	private String orderId;
	
	// 订单金额
	private String money;
	
	// 订单类型
	private TradeType type;
	
	// 店铺id
	private String storeId;
	
	// 店员id
	private String employeeId;
	
	// 设备编号
	private String equipmentId;
	
	/** --0，1，2 设备标识 */
	private int  equipmentMarker;
	
	// 第三方单号
	private String uniquId;
	
	// 交易时间
	private Date date;
	
	// 交易状态
	private int state;
	
	// 消费者唯一id
	private String oppenId;
	
	// 可以为空.数据库处理了'hongsou'
	private String bankId;
	
	// 返回码
	private String resultCode;
	
	// 响应描述
	private String resultInfo;

	
	public String getUniquId() {
		return uniquId;
	}

	public void setUniquId(String uniquId) {
		this.uniquId = uniquId;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getOppenId() {
		return oppenId;
	}

	public void setOppenId(String oppenId) {
		this.oppenId = oppenId;
	}

	public String getBankId() {
		return bankId;
	}

	public void setBankId(String bankId) {
		this.bankId = bankId;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getMoney() {
		return money;
	}

	public void setMoney(String money) {
		this.money = money;
	}

	public TradeType getType() {
		return type;
	}

	public void setType(TradeType type) {
		this.type = type;
	}

	public String getStoreId() {
		return storeId;
	}

	public void setStoreId(String storeId) {
		this.storeId = storeId;
	}

	public String getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(String employeeId) {
		this.employeeId = employeeId;
	}

	public String getEquipmentId() {
		return equipmentId;
	}

	public void setEquipmentId(String equipmentId) {
		this.equipmentId = equipmentId;
	}

	public int getEquipmentMarker() {
		return equipmentMarker;
	}

	public void setEquipmentMarker(int equipmentMarker) {
		this.equipmentMarker = equipmentMarker;
	}

	public String getResultCode() {
		return resultCode;
	}

	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}

	public String getResultInfo() {
		return resultInfo;
	}

	public void setResultInfo(String resultInfo) {
		this.resultInfo = resultInfo;
	}
	
	
}
