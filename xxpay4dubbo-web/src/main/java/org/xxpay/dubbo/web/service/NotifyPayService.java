package org.xxpay.dubbo.web.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.xxpay.common.util.JsonUtil;
import org.xxpay.dubbo.api.service.INotifyPayService;

import com.alibaba.dubbo.config.annotation.Reference;

/**
 * @author: dingzhiwei
 * @date: 17/9/10
 * @description:
 */
@Service
public class NotifyPayService {

	@Reference(version = "1.0.0", timeout = 10000, retries = 0)
	private INotifyPayService notifyPayServiceImpl;


    public String doAliPayNotify(Map params) {
        Map<String,Object> paramMap = new HashMap<>();
        paramMap.put("params", params);
        String jsonParam = JsonUtil.object2Json(paramMap);
      
        Map<String, Object> result = notifyPayServiceImpl.doAliPayNotify(jsonParam);
      
        return "";
    }

   

}
