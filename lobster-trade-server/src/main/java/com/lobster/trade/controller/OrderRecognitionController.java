package com.lobster.trade.controller;

import com.lobster.trade.common.Result;
import com.lobster.trade.model.entity.GameCategory;
import com.lobster.trade.model.request.OrderRecognizeRequest;
import com.lobster.trade.model.response.OrderRecognizeVO;
import com.lobster.trade.service.GameService;
import com.lobster.trade.mapper.TradeOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 手动录单 - 订单文本智能识别
 * 接收格式灵活的文本，自动解析出订单关键字段
 */
@RestController
@RequestMapping("/api/admin/order")
@RequiredArgsConstructor
public class OrderRecognitionController {

    private final GameService gameService;
    private final TradeOrderMapper orderMapper;

    /**
     * POST /api/admin/order/recognize
     * 识别文本中的订单字段
     */
    @PostMapping("/recognize")
    public Result<OrderRecognizeVO> recognize(@RequestBody OrderRecognizeRequest req) {
        String text = req.getText();
        OrderRecognizeVO vo = parseOrderText(text);
        return Result.success(vo);
    }

    private OrderRecognizeVO parseOrderText(String text) {
        OrderRecognizeVO vo = new OrderRecognizeVO();
        vo.setOriginalText(text);

        String[] lines = text.split("\n");
        vo.setLines(lines);

        OrderRecognizeVO.RecognizedFields fields = new OrderRecognizeVO.RecognizedFields();
        Map<String, OrderRecognizeVO.FieldStatus> statusMap = new HashMap<>();
        List<String> unrecognized = new ArrayList<>();
        int recognizedCount = 0;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            if (line.isEmpty()) continue;

            // 尝试找 "键: 值" 格式
            String[] parts = line.split("[:：]", 2);
            if (parts.length < 2) {
                // 尝试整行解析
                unrecognized.add(line);
                continue;
            }

            String key = parts[0].trim().toLowerCase();
            String value = parts[1].trim();
            OrderRecognizeVO.FieldStatus fs = new OrderRecognizeVO.FieldStatus();
            fs.setRaw(value);

            boolean matched = true;

            switch (key) {
                case "订单号":
                case "订单编号":
                case "orderno":
                case "order":
                    fields.setOrderNo(value);
                    fs.setParsed(value);
                    fs.setRecognized(true);
                    recognizedCount++;
                    break;

                case "订单内容":
                case "内容":
                case "商品":
                case "订单商品":
                    fields.setProductTitle(value);
                    fs.setParsed(value);
                    fs.setRecognized(true);
                    recognizedCount++;
                    // 尝试自动识别交易类型
                    if (containsBoostKeyword(value)) {
                        fields.setTradeType("boost");
                    } else if (containsGoodsKeyword(value)) {
                        fields.setTradeType("goods");
                    } else {
                        fields.setTradeType("goods");
                    }
                    break;

                case "服务端口":
                case "端口":
                case "区服":
                case "服":
                case "server":
                case "区":
                    String normalizedServer = normalizeServer(value);
                    fields.setServer(normalizedServer);
                    fs.setParsed(normalizedServer);
                    fs.setRecognized(true);
                    recognizedCount++;
                    // 识别游戏区
                    fields.setGameZone(value);
                    break;

                case "角色名字":
                case "角色名":
                case "角色":
                case "名字":
                case "角色名称":
                case "角色id":
                case "role":
                    fields.setRoleName(value);
                    fs.setParsed(value);
                    fs.setRecognized(true);
                    recognizedCount++;
                    break;

                case "几格保险":
                case "保险":
                case "保险箱":
                case "保险柜":
                case "safe":
                case "box":
                    fields.setSafeBox(value);
                    fs.setParsed(value);
                    fs.setRecognized(true);
                    recognizedCount++;
                    break;

                case "段位":
                case "段位多少":
                case "段":
                case "rank":
                case "目标段位":
                case "起始段位":
                    String normalizedRank = normalizeRank(value);
                    fields.setRank(normalizedRank);
                    fs.setParsed(normalizedRank);
                    fs.setRecognized(true);
                    recognizedCount++;
                    break;

                case "联系方式":
                case "联系":
                case "联系方":
                case "微信":
                case "QQ":
                case "qq":
                case "wechat":
                case "电话":
                case "手机":
                case "手机号":
                case "contact":
                    String contactVal = normalizeContact(value);
                    fields.setOrderSource(contactVal);
                    fs.setParsed(contactVal);
                    fs.setRecognized(true);
                    recognizedCount++;
                    break;

                case "订单来源":
                case "来源":
                case "渠道":
                case "source":
                    String sourceVal = normalizeSource(value);
                    fields.setOrderSource(sourceVal);
                    fs.setParsed(sourceVal);
                    fs.setRecognized(true);
                    recognizedCount++;
                    break;

                case "游戏":
                case "游戏名":
                case "game":
                    String gameResult = recognizeGame(value);
                    fs.setParsed(gameResult);
                    fs.setRecognized(gameResult != null);
                    if (gameResult != null) recognizedCount++;
                    break;

                default:
                    // 尝试验证任意行是否包含关键内容
                    String autoParsed = autoParseField(key, value);
                    if (autoParsed != null) {
                        fs.setParsed(autoParsed);
                        fs.setRecognized(true);
                        recognizedCount++;
                    } else {
                        fs.setRecognized(false);
                        fs.setTip("未识别字段: " + key);
                        unrecognized.add(line);
                        matched = false;
                    }
                    break;
            }

            statusMap.put(key, fs);
        }

        // 自动识别游戏
        String detectedGame = detectGame(text);
        if (detectedGame != null) {
            fields.setGameName(detectedGame);
            fields.setGameId(getGameId(detectedGame));
        }

        // 计算置信度
        double rate = lines.length > 0 ? (double) recognizedCount / lines.length : 0;
        if (rate >= 0.7) fields.setConfidence("high");
        else if (rate >= 0.4) fields.setConfidence("medium");
        else fields.setConfidence("low");

        fields.setUnrecognizedLines(unrecognized.toArray(new String[0]));
        vo.setRecognized(fields);
        vo.setFieldStatus(statusMap);

        return vo;
    }

    // 辅助方法

    private boolean containsBoostKeyword(String text) {
        String t = text.toLowerCase();
        return t.contains("代练") || t.contains("段位") || t.contains("排位")
                || t.contains("上分") || t.contains("代肝") || t.contains("等级")
                || t.contains("通行证") || t.contains("任务") || t.contains("护航")
                || t.contains("陪玩") || t.contains("撞车");
    }

    private boolean containsGoodsKeyword(String text) {
        String t = text.toLowerCase();
        return t.contains("币") || t.contains("金") || t.contains("点券")
                || t.contains("道具") || t.contains("装备") || t.contains("皮肤")
                || t.contains("水晶") || t.contains("碎片") || t.contains("足球")
                || t.contains("机甲") || t.contains("哈夫");
    }

    private String normalizeServer(String value) {
        String v = value.trim().toUpperCase();
        // 烽火/全面战场/矮墙服务
        if (v.contains("Q") || v.contains("q")) return "Q服(烽火)";
        if (v.contains("R") || v.contains("r")) return "R服(战场)";
        if (v.contains("H") || v.contains("h")) return "H服(烽火)";
        // 直接识别
        if (v.equals("Q") || v.equals("Q服") || v.equals("Q(烽火)")) return "Q服(烽火)";
        if (v.equals("R") || v.equals("R服") || v.equals("R(战场)")) return "R服(战场)";
        if (v.equals("H") || v.equals("H服") || v.equals("H(烽火)")) return "H服(烽火)";
        return v;
    }

    private String normalizeRank(String value) {
        String v = value.trim();
        // 三角洲行动段位
        if (v.contains("青铜") || v.contains("无不入") || v.contains("新兵"))
            return "新兵/青铜";
        if (v.contains("白银") || v.contains("列兵"))
            return "白银/列兵";
        if (v.contains("黄金") || v.contains("军士") || v.contains("下士") || v.contains("中士"))
            return "黄金/军士";
        if (v.contains("铂金") || v.contains("上士") || v.contains("士官"))
            return "铂金/士官";
        if (v.contains("钻石") || v.contains("尉官") || v.contains("少尉") || v.contains("中尉"))
            return "钻石/尉官";
        if (v.contains("皇冠") || v.contains("校官") || v.contains("少校") || v.contains("中校"))
            return "皇冠/校官";
        if (v.contains("宗师") || v.contains("将官") || v.contains("少将") || v.contains("中将"))
            return "宗师/将官";
        if (v.contains("战神") || v.contains("兵王") || v.contains("元帅"))
            return "战神/兵王";
        return v;
    }

    private String normalizeContact(String value) {
        String v = value.trim();
        // 手机号优先
        if (v.matches("1[3-9]\\d{9}")) return "手机:" + v;
        // 微信
        if (v.matches("wx[\\da-zA-Z]{6,}") || v.matches("wechat[\\da-zA-Z]{6,}")) return v.toUpperCase();
        // QQ号（5-10位数字，以非1开头）
        if (v.matches("\\d{5,10}") && !v.startsWith("1")) return "QQ:" + v;
        return v;
    }

    private String normalizeSource(String value) {
        String v = value.trim();
        // 标准化平台名称
        if (v.contains("淘宝") || v.equals("tb") || v.equals("Taobao")) return "淘宝";
        if (v.contains("拼多多") || v.equals("pdd") || v.equals("拼")) return "拼多多";
        if (v.contains("抖音") || v.contains("抖店") || v.contains("抖音小店")) return "抖店";
        if (v.contains("闲鱼") || v.equals("xy")) return "闲鱼";
        if (v.contains("快手") || v.equals("ks")) return "快手";
        // 其他直接返回（可能是个人名字）
        return v;
    }

    private String detectGame(String text) {
        String t = text.toLowerCase();
        if (t.contains("三角洲") || t.contains("delta") || t.contains("df")
                || t.contains("炫彩足球") || t.contains("巨兽机甲")
                || t.contains("哈夫币") || t.contains("烽火") || t.contains("战场")) {
            return "三角洲行动";
        }
        if (t.contains("王者") || t.contains("农药") || t.contains("lol") || t.contains("王者荣耀")) {
            return "王者荣耀";
        }
        if (t.contains("原神")) return "原神";
        if (t.contains("暗区") || t.contains("突围")) return "暗区突围";
        return null;
    }

    private String recognizeGame(String value) {
        String v = value.trim();
        String detected = detectGame(v);
        return detected;
    }

    /**
     * POST /api/admin/order/create
     * 根据识别结果（或手动填写）创建订单
     */
    @PostMapping("/create")
    public Result<Map<String, Object>> createOrder(@RequestBody Map<String, Object> params) {
        String orderNo = (String) params.get("orderNo");
        String productTitle = (String) params.get("productTitle");
        String tradeType = (String) params.get("tradeType");
        Long gameId = params.get("gameId") != null ? ((Number) params.get("gameId")).longValue() : null;
        String gameZone = (String) params.get("gameZone");
        String roleName = (String) params.get("roleName");
        String safeBox = (String) params.get("safeBox");
        String rank = (String) params.get("rank");
        Long sellerId = params.get("sellerId") != null ? ((Number) params.get("sellerId")).longValue() : null;
        Object amountObj = params.get("orderAmount");
        java.math.BigDecimal orderAmount = amountObj != null ? new java.math.BigDecimal(amountObj.toString()) : java.math.BigDecimal.ZERO;
        String remark = (String) params.get("remark");
        String contact = (String) params.get("contact");
        String orderSource = (String) params.get("orderSource");

        if (orderNo == null || orderNo.isBlank()) {
            return Result.error("订单号不能为空");
        }
        if (sellerId == null) {
            return Result.error("卖家ID不能为空");
        }

        com.lobster.trade.model.entity.TradeOrder order = new com.lobster.trade.model.entity.TradeOrder();
        order.setOrderNo(orderNo);
        order.setProductTitle(productTitle != null ? productTitle : "");
        order.setTradeType(tradeType != null ? tradeType : "goods");
        order.setGameId(gameId);
        order.setSellerId(sellerId);
        order.setBuyerId(sellerId); // 手动录单时卖家即买家（自买自卖）或设为占位值
        order.setOrderAmount(orderAmount);
        order.setStatus("pending_pay"); // 默认待付款
        order.setEscrowStatus(1); // 未托管
        order.setPaymentStatus(0);
        order.setCreateTime(java.time.LocalDateTime.now());
        order.setUpdateTime(java.time.LocalDateTime.now());
        order.setIsDeleted(0);

        // 附加要求存到 boostRequirement
        StringBuilder reqSb = new StringBuilder();
        if (roleName != null && !roleName.isBlank()) reqSb.append("角色：").append(roleName).append("；");
        if (safeBox != null && !safeBox.isBlank()) reqSb.append("保险柜：").append(safeBox).append("；");
        if (rank != null && !rank.isBlank()) reqSb.append("段位：").append(rank).append("；");
        if (contact != null && !contact.isBlank()) reqSb.append("联系方式：").append(contact).append("；");
        if (orderSource != null && !orderSource.isBlank()) reqSb.append("订单来源：").append(orderSource).append("；");
        if (remark != null && !remark.isBlank()) reqSb.append("备注：").append(remark);
        if (reqSb.length() > 0) order.setBoostRequirement(reqSb.toString());

        try {
            orderMapper.insert(order);
        } catch (Exception e) {
            throw new RuntimeException("订单创建失败：" + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("orderId", order.getId());
        result.put("orderNo", order.getOrderNo());
        return Result.success(result);
    }

    private Long getGameId(String gameName) {
        if ("三角洲行动".equals(gameName)) return 1L;
        if ("王者荣耀".equals(gameName)) return 2L;
        if ("原神".equals(gameName)) return 3L;
        if ("暗区突围".equals(gameName)) return 4L;
        return null;
    }

    private String autoParseField(String key, String value) {
        // 尝试自动识别无标签字段
        String v = value.trim();
        if (v.matches("\\d{10,}") || v.matches(".*-.*\\d{10,}")) {
            return v; // 订单号
        }
        if (v.matches("\\d{1,2}")) {
            return v; // 保险格数
        }
        return null;
    }
}
