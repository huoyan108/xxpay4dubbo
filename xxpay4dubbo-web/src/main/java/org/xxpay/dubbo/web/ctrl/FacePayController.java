package org.xxpay.dubbo.web.ctrl;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import org.xxpay.common.util.PayUtils;
import org.xxpay.dubbo.api.service.AlipayTradeService;
import org.xxpay.dubbo.api.service.WecharTradeService;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hongsou.alipay.model.AlipayTradePayRequestBuilder;
import com.hongsou.config.Constants;
import com.hongsou.trade.result.AlipayF2FPayResult;

/**
 * 扫码支付
 * 
 * @author qiyu
 * @date 2017年11月29日 下午4:24:33
 * @version 1.0
 */
@RestController
@RequestMapping("/pay")
public class FacePayController {

	private static Log log = LogFactory.getLog(FacePayController.class);

	@Reference(version = "1.0.0", timeout = 10000, retries = 0)
	private AlipayTradeService alipayTradeImpl;

	@Reference(version = "1.0.0", timeout = 10000, retries = 0)
	private WecharTradeService WecharTradeImpl;

	@RequestMapping("/tabbyPay")
	public Map<String, String> tabbyPay(AlipayTradePayRequestBuilder pojo, ModelAndView model) {
		// 正式环境是18位,测试环境下有17位修改了这个方法.
		if (PayUtils.estimate(pojo.getAuthCode()) == 1) {
			try {
				AlipayF2FPayResult result = alipayTradeImpl.tradePay(pojo);
				switch (result.getTradeStatus()) {
				case SUCCESS:
					log.info("支付宝支付成功: )");
					return null;
				case FAILED:
					log.error("支付宝支付失败!!!");
					break;
				case UNKNOWN:
					log.error("系统异常，订单状态未知!!!");
					break;
				case PAYING:
					log.info("用户支付中");
					break;
				default:
					log.error("不支持的交易状态，交易返回异常!!!");
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			/** 微信条码支付 */
		} else if (PayUtils.estimate(pojo.getAuthCode()) == 2) {

			try {
				Map resultState = WecharTradeImpl.tradePay(pojo);
				if(Constants.SUCCESS.equals(PayUtils.getResult(resultState))) {
					log.info("支付成功");
				}else if(Constants.FAILED.equals(PayUtils.getResult(resultState))) {
					log.info("支付失败");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
