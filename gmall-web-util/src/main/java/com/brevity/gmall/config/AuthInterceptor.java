package com.brevity.gmall.config;

import com.alibaba.fastjson.JSON;
import com.brevity.gmall.constant.WebConst;
import com.brevity.gmall.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.Map;

// springMVC的四大组件：核心控制器(DispatcherServlet)、适配器(HandlerAdapter)、映射器(@RequestMapping())、视图解析器
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {

    // 在进入控制器之前此方法必然会执行
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 获取token存入cookie中，只有登录的时候才能得到token
        String token = request.getParameter("newToken");
        if (token != null) {
            // 将token放入cookie中
            CookieUtil.setCookie(request, response, "token", token, WebConst.COOKIE_MAXAGE, false);
        }

        // 当登录以后用户访问其它业务的时候，此时有token
        if (token == null) {
            // token可能在cookie中
            token = CookieUtil.getCookieValue(request, "token", false);
        }

        // 从token中得到用户昵称
        if (token != null) {
            Map map = getUserMapByToken(token);
            String nickName = (String) map.get("nickName");
            // 保存用户昵称
            request.setAttribute("nickName", nickName);
        }

        // 获取方法上的注解
        HandlerMethod handlerMethod = (HandlerMethod) handler;
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
        // 说明方法上有这个注解
        if (methodAnnotation != null) {
            String salt = request.getHeader("X-forwarded-for");
            // 调用认证方法
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&salt=" + salt);
            if ("success".equals(result)) {
                // 已经登录了
                Map map = getUserMapByToken(token);
                String userId = (String) map.get("userId");
                request.setAttribute("userId", userId);
                return true;
            } else {
                // 注解中autoRedirect为true，必须登录,没有登录重定向到登录页面
                if (methodAnnotation.autoRedirect()) {
                    // 获取到被拦截的请求的url
                    String requestURL = request.getRequestURL().toString();
                    // 编码处理
                    String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
                    response.sendRedirect(WebConst.LOGIN_ADDRESS + "?originUrl=" + encodeURL);
                    return false;
                }
            }
        }
        return true;
    }

    // 解密token
    public Map getUserMapByToken(String token) {
        // 获取token的私有部分
        String tokenUserInfo = StringUtils.substringBetween(token, ".");
        // 使用base64解码
        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] bytes = base64UrlCodec.decode(tokenUserInfo);
        // 把字节数组变为字符串
        String strJson = new String(bytes);
        // 把字符串变为map
        return JSON.parseObject(strJson, Map.class);
    }

    // 进入拦截器之后返回视图之前执行
    public void postHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)
            throws Exception {
    }

    // 最后一步执行(相当于finally的作用)
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {
    }
}
