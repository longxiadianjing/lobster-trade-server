package com.lobster.trade.controller.admin;

import com.lobster.trade.common.Result;
import com.lobster.trade.model.entity.TradeOrder;
import com.lobster.trade.model.entity.User;
import com.lobster.trade.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/admin/export")
@RequiredArgsConstructor
public class ExportController {

    private final AdminService adminService;

    @GetMapping("/orders")
    public void exportOrders(HttpServletResponse response,
                              @RequestParam(required = false) String status,
                              @RequestParam(required = false) String tradeType,
                              @RequestParam(required = false) String keyword) throws IOException {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<TradeOrder> page =
                adminService.listOrders(keyword, status, tradeType, 1, 10000);
        List<TradeOrder> orders = page.getRecords();

        response.setContentType("text/csv;charset=GBK");
        response.setHeader("Content-Disposition",
                "attachment; filename=orders_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv");

        OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), "GBK");
        writer.write("\uFEFF"); // BOM for Excel
        writer.write("订单ID,订单号,类型,商品标题,卖家ID,买家ID,订单金额,托管金额,状态,支付状态,创建时间\n");

        for (TradeOrder o : orders) {
            writer.write(String.format("%d,%s,%s,%s,%d,%d,%s,%s,%s,%s,%s\n",
                    o.getId(),
                    escapeCsv(o.getOrderNo()),
                    escapeCsv(o.getTradeType()),
                    escapeCsv(o.getProductTitle()),
                    o.getSellerId() != null ? o.getSellerId() : 0,
                    o.getBuyerId() != null ? o.getBuyerId() : 0,
                    o.getOrderAmount() != null ? o.getOrderAmount().toString() : "",
                    o.getEscrowAmount() != null ? o.getEscrowAmount().toString() : "",
                    escapeCsv(o.getStatus()),
                    escapeCsv(o.getPaymentStatus() != null ? o.getPaymentStatus().toString() : ""),
                    escapeCsv(o.getCreateTime() != null ? o.getCreateTime().toString() : "")
            ));
        }
        writer.flush();
        writer.close();
    }

    @GetMapping("/users")
    public void exportUsers(HttpServletResponse response,
                             @RequestParam(required = false) String keyword,
                             @RequestParam(required = false) String status) throws IOException {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<User> page =
                adminService.listUsers(keyword, status, 1, 10000);
        List<User> users = page.getRecords();

        response.setContentType("text/csv;charset=GBK");
        response.setHeader("Content-Disposition",
                "attachment; filename=users_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv");

        OutputStreamWriter writer = new OutputStreamWriter(response.getOutputStream(), "GBK");
        writer.write("\uFEFF");
        writer.write("用户ID,用户名,昵称,手机号,邮箱,用户等级,余额,累计交易额,交易次数,实名状态,账号状态,注册时间\n");

        for (User u : users) {
            writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s,%d,%s,%s,%s\n",
                    u.getId(),
                    escapeCsv(u.getUsername()),
                    escapeCsv(u.getNickname()),
                    escapeCsv(u.getPhone()),
                    escapeCsv(u.getEmail()),
                    u.getUserLevel() != null ? u.getUserLevel().toString() : "",
                    u.getBalance() != null ? u.getBalance().toString() : "",
                    u.getTotalTradeAmount() != null ? u.getTotalTradeAmount().toString() : "",
                    u.getTotalTradeCount() != null ? u.getTotalTradeCount() : 0,
                    u.getRealNameStatus() != null ? u.getRealNameStatus().toString() : "",
                    u.getStatus() != null ? u.getStatus().toString() : "",
                    escapeCsv(u.getCreateTime() != null ? u.getCreateTime().toString() : "")
            ));
        }
        writer.flush();
        writer.close();
    }

    private String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}