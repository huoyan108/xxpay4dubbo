package org.xxpay.dubbo.api.service;

import java.util.Map;

import com.hongsou.alipay.model.AlipayTradePayRequestBuilder;

/**
 * 微信当面付的接口
 * 
 * @author qiyu
 * @date 2017年12月4日 下午3:28:58
 * @version 1.0
 */
public interface WecharTradeService {

	Map tradePay(AlipayTradePayRequestBuilder builder) throws Exception;
}
