package com.brevity.gmall.passport;

import com.brevity.gmall.passport.config.JwtUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@SpringBootTest
@RunWith(SpringRunner.class)
public class GmallPassportWebApplicationTests {

    @Test
    public void contextLoads() {
    }

    @Test
    public void JWTTest() {
        String key = "brevity";
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", "101");
        map.put("name", "root");
        String salt = "192.168.116.1";
        String token = JwtUtil.encode(key, map, salt);
        System.out.println("token：" + token);
        System.out.print("解密：");
        Map<String, Object> maps = JwtUtil.decode(token, key, salt);
        System.out.println(maps);
    }
}
