package com.hongsou.pojo;

/**
 * 登录用户的实体类
 * 
 * @author qiyu
 * @date 2017年11月16日 下午2:58:33
 * @version 1.0
 */
public class UserPojo {

	/** 用户名 */
	private String username;
	
	/** 用户密码 */
	private String password;
	
	/** 验证码 */
	private String verifyCode;
	
	/** 设备码 */
	private  String deviceInfo;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getVerifyCode() {
		return verifyCode;
	}

	public void setVerifyCode(String verifyCode) {
		this.verifyCode = verifyCode;
	}

	public String getDeviceInfo() {
		return deviceInfo;
	}

	public void setDeviceInfo(String deviceInfo) {
		this.deviceInfo = deviceInfo;
	}
	
	
}
