package org.xxpay.dubbo.model;

import java.io.Serializable;

import com.alipay.api.DefaultAlipayClient;
import com.hongsou.config.AliConfig;

/**
 * 封装支付宝支付的默认初始化对象
 * 
 * @author qiyu
 * @date 2017年11月29日 上午10:24:57
 * @version 1.0
 */
public class AliModel extends DefaultAlipayClient implements Serializable {

	private static final long serialVersionUID = 3511836180728714812L;

	// 私有对象
	private static AliModel model;
	
	private AliModel(String serverUrl, String appId, String privateKey, String format, String charset,
			String alipayPulicKey, String signType) {
		super(serverUrl, appId, privateKey, format, charset, alipayPulicKey, signType);
	}
	
	/**
	 * 获取默认对象,获取对象之前先调用
	 * AliConfig.init("shared/ali.properties");
	 * 
	 * @return
	 */
	public static AliModel getInstance() {
		if(model !=null) {
			return model;
		}
		return new AliModel(AliConfig.getOpenApiDomain(), AliConfig.getAppid(), AliConfig.getPrivateKey(), "json", "UTF-8", AliConfig.getAlipayPublicKey(), AliConfig.getSignType());
	}

	/**
	 * 支付宝请求地址
	 */
	private String serverUrl;

	/**
	 * 平台唯一标识
	 */
	private String aliAppid;

	/**
	 * 应用私钥
	 */
	private String privateKey;

	/**
	 * 支付宝共钥
	 */
	private String publicKey;

	/**
	 * 签名类型 RSA RSA2
	 */
	private String signType;

	/**
	 * json.传送类型
	 */
	private String type = "json";

	/**
	 * 编码类型 utf-8
	 */
	private String coding = "UTF-8";
	
	/** 回调地址 */
	private String notifyUrl;
	
	public String getNotifyUrl() {
		return notifyUrl;
	}

	public void setNotifyUrl(String notifyUrl) {
		this.notifyUrl = notifyUrl;
	}

	public String getServerUrl() {
		return serverUrl;
	}

	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}

	public String getAliAppid() {
		return aliAppid;
	}

	public void setAliAppid(String aliAppid) {
		this.aliAppid = aliAppid;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getSignType() {
		return signType;
	}

	public void setSignType(String signType) {
		this.signType = signType;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getCoding() {
		return coding;
	}

	public void setCoding(String coding) {
		this.coding = coding;
	}
}
