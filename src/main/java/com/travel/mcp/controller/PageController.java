package com.travel.mcp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 页面控制器 — SPA 转发
 * 所有非 /api 请求转发到 Vue 3 的 index.html
 */
@Controller
public class PageController {

    @GetMapping(value = {"/", "/{path:[^\\.]*}"})
    public String forward() {
        return "forward:/index.html";
    }
}
