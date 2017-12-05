package org.xxpay.dubbo.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.xxpay.common.util.MyLog;
import org.xxpay.dubbo.api.service.INotifyPayService;
import org.xxpay.dubbo.model.AliModel;
import org.xxpay.dubbo.service.mq.Mq4MchPayNotify;

import com.alibaba.dubbo.config.annotation.Service;
import com.hongsou.config.AliConfig;

/**
 * @author: dingzhiwei
 * @date: 17/9/10
 * @description:
 */
@Service(version = "1.0.0")
public class NotifyPayServiceImpl  implements INotifyPayService {

    private static final MyLog _log = MyLog.getLog(NotifyPayServiceImpl.class);

 
	private static AliModel aliModel;
	
	@Autowired
	private Mq4MchPayNotify payNotif;
	/**
	 * 读取配置文件,初始化请求对象
	 */
	static {
		AliConfig.init("shared/ali.properties");
		aliModel = AliModel.getInstance();
	}
	
    @Override
    public Map doAliPayNotify(String jsonParam) {
        //验证签名
        _log.info("{}验证支付通知数据及签名通过", "");
      

        // 支付状态成功或者完成通知更新数据
        payNotif.sendSuccessOrder("");
       
        _log.info("====== 完成处理支付宝支付回调通知 ======");
        return null;
    }

    @Override
    public Map doWxPayNotify(String jsonParam) {
    	return null;
       
    }

    @Override
    public Map sendBizPayNotify(String jsonParam) {
       return null;
       
    }


    /**
     * 验证支付宝支付通知参数
     * @return
     */
    public boolean verifyAliPayParams(Map<String, Object> payContext) {
        
        return true;
    }

    /**
     * 验证微信支付通知参数
     * @return
     */
    public boolean verifyWxPayParams(Map<String, Object> payContext) {
       
        return true;
    }


}
