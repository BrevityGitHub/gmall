package com.brevity.gmall.thymeleaf.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;

@Controller
public class IndexController {

    @RequestMapping("index")
    public String index(HttpServletRequest request, HttpSession session) {
        // 保存一个名称
        request.setAttribute("name", "天后");
        ArrayList<String> list = new ArrayList<>();
        list.add("爱因斯坦");
        list.add("麦哲伦");
        list.add("牛顿");
        list.add("图灵");
        request.setAttribute("strList", list);
        request.setAttribute("age", 18);
        session.setAttribute("address", "北京");
        // 解析样式
        request.setAttribute("green", "<span style=color:green>baby</span>");
        return "index";
    }
}
