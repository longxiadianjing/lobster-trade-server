package com.lobster.trade.model.request;

import lombok.Data;

/**
 * 手动录单 - 文本识别请求
 * 接收粘贴的订单文本，自动识别关键字段
 */
@Data
public class OrderRecognizeRequest {

    /**
     * 原始文本，可以包含换行、空格，格式灵活
     * 例如：
     * 订单号：260423-482449877411343
     * 订单内容：炫彩足球加巨兽机甲
     * 服务端口：Q
     * 角色名字：向来运气好
     * 几格保险：9
     * 段位多少：钻石
     */
    private String text;
}
