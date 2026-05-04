package com.lobster.trade.model.response;

import lombok.Data;
import java.util.Map;

/**
 * 订单文本识别结果
 */
@Data
public class OrderRecognizeVO {

    /** 原始文本 */
    private String originalText;

    /** 识别结果 */
    private RecognizedFields recognized;

    /** 原始文本行（用于展示） */
    private String[] lines;

    /** 原始文本行对应的解析状态 */
    private Map<String, FieldStatus> fieldStatus;

    @Data
    public static class RecognizedFields {
        private String orderNo;       // 订单号
        private String productTitle;  // 商品标题/订单内容
        private String server;        // 服务端口（Q/R/H等）
        private String gameZone;      // 游戏区服
        private String roleName;      // 角色名字
        private String safeBox;       // 几格保险
        private String rank;         // 段位
        private String orderSource;   // 订单来源（淘宝/拼多多/抖店/个人）
        private String contact;       // 联系方式
        private String tradeType;     // 交易类型 boost/goods/accompany/escort
        private Long gameId;          // 识别的游戏ID
        private String gameName;      // 识别的游戏名称
        private String confidence;    // 识别置信度 high/medium/low
        private String[] unrecognizedLines; // 未能识别的行
    }

    @Data
    public static class FieldStatus {
        private String raw;          // 原始值
        private String parsed;        // 解析后值
        private boolean recognized;   // 是否识别成功
        private String tip;           // 提示信息
    }
}
