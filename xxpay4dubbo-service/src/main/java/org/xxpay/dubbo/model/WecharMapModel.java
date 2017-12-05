package org.xxpay.dubbo.model;

import java.util.HashMap;
import java.util.Map;

import org.xxpay.common.util.IPUtility;

import com.hongsou.config.WecharConfig;

public class WecharMapModel {

	static {
		WecharConfig.init("shared/wechar.properties");
	}
	
	/**
	 * 简单的微信请求map
	 * 
	 * @return
	 */
	public static Map getRequestMap() {
		HashMap<String,String> request = new HashMap<>();
		request.put("appid", WecharConfig.getWxAppid());
		request.put("mch_id",WecharConfig.getWxMchId());
		request.put("nonce_str",String.valueOf(System.currentTimeMillis()));
		request.put("spbill_create_ip",IPUtility.getLocalIP());
		return request;
	}
}
