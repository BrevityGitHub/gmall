package com.brevity.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.brevity.gmall.bean.UserInfo;
import com.brevity.gmall.passport.config.JwtUtil;
import com.brevity.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {
    @Reference
    private UserService userService;

    // 获取配置文件的key
    @Value("${token.key}")
    private String key;

    @RequestMapping("index")
    public String index(HttpServletRequest request) {
        // index.html中隐藏域的originUrl的作用：记录从哪里点击的登录
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl", originUrl);
        return "index";  // index为视图名
    }

    @ResponseBody
    @RequestMapping("login")
    public String login(UserInfo userInfo, HttpServletRequest request) {
        UserInfo info = userService.login(userInfo);
        if (info != null) {
            // 登录成功返回token
            String salt = request.getHeader("X-forwarded-for");
            HashMap<String, Object> map = new HashMap<>();
            map.put("userId", info.getId());
            map.put("nickName", info.getNickName());
            String token = JwtUtil.encode(key, map, salt);
            return token;
        } else {
            return "fail";
        }
    }

    /**
     * @return 解密token得到userId
     */
    @ResponseBody
    @RequestMapping("verify")
    public String verify(HttpServletRequest request) {
        // 获取浏览器url中的token和salt
        String token = request.getParameter("token");
        String salt = request.getParameter("salt");
        Map<String, Object> map = JwtUtil.decode(token, key, salt);
        if (map != null && map.size() > 0) {
            // 获取userId
            String userId = (String) map.get("userId");
            UserInfo userInfo = userService.verify(userId);
            if (userInfo != null) {
                return "success";
            } else {
                return "fail";
            }
        }
        return "fail";
    }
}
