package com.lobster.trade.controller;

import com.lobster.trade.mapper.AdminMapper;
import com.lobster.trade.model.entity.Admin;
import com.lobster.trade.util.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
public class DebugController {

    private final AdminMapper adminMapper;

    @GetMapping("/admin/{username}")
    public Map<String, Object> getAdmin(@PathVariable String username) {
        Admin admin = adminMapper.selectByUsername(username);
        if (admin == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", "admin not found");
            err.put("username", username);
            return err;
        }
        String rawPwd = username.equals("admin") ? "admin123" : "operator123";
        boolean matches = PasswordEncoder.matches(rawPwd, admin.getPassword());
        Map<String, Object> result = new HashMap<>();
        result.put("id", admin.getId());
        result.put("username", admin.getUsername());
        result.put("passwordHash", admin.getPassword());
        result.put("passwordLength", admin.getPassword() != null ? admin.getPassword().length() : 0);
        result.put("testPassword", rawPwd);
        result.put("matchesResult", matches);
        return result;
    }
}