package com.lobster.trade.service;

import com.lobster.trade.model.entity.PaymentTransaction;
import java.util.Map;

public interface WeChatPayService {

    /**
     * 提交扫码支付（Native），返回支付二维码链接
     * @param transaction 支付单
     * @param description 商品描述
     * @return code_url，二维码内容
     */
    String createNativeOrder(PaymentTransaction transaction, String description);

    /**
     * 解析微信支付回调通知
     * @param params post body 参数 map
     * @return 解析结果：paymentNo -> transactionId
     */
    Map<String, String> parseNotifyResult(Map<String, String> params);
}
