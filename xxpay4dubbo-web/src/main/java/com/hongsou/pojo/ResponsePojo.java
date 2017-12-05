package com.hongsou.pojo;

/**
 * 用来接受数据库的结果.
 * 
 * @author qiyu
 * @date 2017年11月16日 下午3:55:18
 * @version 1.0
 */
public class ResponsePojo {

	
	/** 请求响应码*/
	private  String resultCode;
	
	/** 请求响应信息 */
	private  String resultInfo;

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

	/**
	 * 判断返回码的方法
	 * 
	 * @param resultCode
	 * @return
	 */
	public boolean validate(){
		if("0000".equals(resultCode)){
			return true;
		}
		return false;
	}
}
