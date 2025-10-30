package com.campus.campussharebackend;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class BCryptTest {
    public static void main(String[] args) {
        // 明文密码（你想设置的密码，如"123456"）
        String rawPassword = "123456";

        // 加密
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String encodedPassword = encoder.encode(rawPassword);

        // 输出加密后的字符串（复制到数据库）
        System.out.println("加密后的密码：" + encodedPassword);
    }
}