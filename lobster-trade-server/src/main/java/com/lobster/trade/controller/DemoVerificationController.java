package com.lobster.trade.controller;

import com.lobster.trade.service.RealNameVerifyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/api/real-name")
@RequiredArgsConstructor
public class DemoVerificationController {

    private final RealNameVerifyService verifyService;

    /**
     * DEMO演示页面（无阿里云AK时跳转此页面）
     * GET /api/real-name/demo-page?token=xxx
     */
    @GetMapping(value = "/demo-page", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public String demoPage(@RequestParam String token) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"zh\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <title>实名认证 - 龙虾道具交易平台</title>\n" +
                "  <style>\n" +
                "    body { font-family: 'PingFang SC', 'Microsoft YaHei', sans-serif; background: #f5f7fa; margin: 0; padding: 0; }\n" +
                "    .container { max-width: 500px; margin: 80px auto; background: #fff; border-radius: 16px; padding: 40px; box-shadow: 0 4px 24px rgba(0,0,0,0.08); text-align: center; }\n" +
                "    .icon { font-size: 64px; margin-bottom: 16px; }\n" +
                "    h2 { margin: 0 0 8px; color: #333; }\n" +
                "    p { color: #888; font-size: 14px; margin: 0 0 32px; }\n" +
                "    .info { background: #f8f8ff; border-radius: 8px; padding: 16px; margin-bottom: 24px; text-align: left; font-size: 13px; color: #555; }\n" +
                "    .info-item { margin: 6px 0; }\n" +
                "    .btns { display: flex; gap: 12px; justify-content: center; }\n" +
                "    button { padding: 12px 32px; border: none; border-radius: 8px; font-size: 15px; cursor: pointer; transition: all 0.2s; }\n" +
                "    .btn-pass { background: #67c23a; color: #fff; }\n" +
                "    .btn-pass:hover { background: #529b2e; }\n" +
                "    .btn-fail { background: #f56c6c; color: #fff; }\n" +
                "    .btn-fail:hover { background: #e64242; }\n" +
                "    .btn-skip { background: #e8e8e8; color: #888; }\n" +
                "    .btn-skip:hover { background: #dcdcdc; }\n" +
                "    .tip { font-size: 11px; color: #bbb; margin-top: 20px; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"container\">\n" +
                "    <div class=\"icon\">🎭</div>\n" +
                "    <h2>实名认证（演示模式）</h2>\n" +
                "    <p>当前为 Demo 演示，无需真实身份信息</p>\n" +
                "    <div class=\"info\">\n" +
                "      <div class=\"info-item\"><b>Token:</b> <span id=\"token\"></span></div>\n" +
                "      <div class=\"info-item\"><b>认证方式:</b> 阿里云实人认证（Demo）</div>\n" +
                "      <div class=\"info-item\"><b>说明:</b> 点击下方按钮模拟认证结果</div>\n" +
                "    </div>\n" +
                "    <div class=\"btns\">\n" +
                "      <button class=\"btn-pass\" onclick=\"submitResult(2000)\">✅ 通过认证</button>\n" +
                "      <button class=\"btn-fail\" onclick=\"submitResult(4000)\">❌ 认证失败</button>\n" +
                "    </div>\n" +
                "    <div class=\"btns\" style=\"margin-top:10px;\">\n" +
                "      <button class=\"btn-skip\" onclick=\"window.close()\">取消</button>\n" +
                "    </div>\n" +
                "    <p class=\"tip\">演示模式：实际接阿里云时需跳转支付宝/微信进行人脸核身</p>\n" +
                "  </div>\n" +
                "  <script>\n" +
                "    document.getElementById('token').textContent = window.location.search.split('token=')[1] || '';\n" +
                "    function submitResult(statusCode) {\n" +
                "      fetch('/api/real-name/demo-submit?token=' + encodeURIComponent('" + "\" + token + \"" + "') + '&statusCode=' + statusCode, {\n" +
                "        method: 'POST',\n" +
                "        headers: { 'Content-Type': 'application/json' },\n" +
                "        body: JSON.stringify({ certifyId: '" + "\" + token + \"" + "', verifyToken: '" + "\" + token + \"" + "', statusCode: statusCode })\n" +
                "      }).then(r => r.json()).then(data => {\n" +
                "        if (data.code === 0 || data.code === '0') {\n" +
                "          alert(statusCode === 2000 ? '✅ 认证通过！可以关闭此页面了。' : '❌ 认证失败，请重试。');\n" +
                "          window.close();\n" +
                "        } else {\n" +
                "          alert('操作失败: ' + data.message);\n" +
                "        }\n" +
                "      }).catch(e => { alert('网络错误'); console.error(e); });\n" +
                "    }\n" +
                "  </script>\n" +
                "</body>\n" +
                "</html>";
    }

    /**
     * DEMO认证结果提交（模拟阿里云回调）
     * POST /api/real-name/demo-submit
     */
    @PostMapping("/demo-submit")
    @ResponseBody
    public Map<String, Object> demoSubmit(
            @RequestParam String token,
            @RequestParam Integer statusCode,
            @RequestBody Map<String, Object> body) {

        Map<String, Object> callbackBody = new HashMap<>();
        callbackBody.put("verifyToken", token);
        callbackBody.put("certifyId", token);
        callbackBody.put("statusCode", statusCode);

        verifyService.handleAliyunCallback(callbackBody);

        Map<String, Object> result = new HashMap<>();
        result.put("code", 0);
        result.put("message", "ok");
        return result;
    }
}