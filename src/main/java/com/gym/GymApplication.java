package com.gym;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 健身房会员与训练管理系统
 * 启动入口
 */
@SpringBootApplication
public class GymApplication {

    public static void main(String[] args) {
        SpringApplication.run(GymApplication.class, args);
        System.out.println("================================================");
        System.out.println("  健身房会员与训练管理系统已启动");
        System.out.println("  访问地址: http://localhost:8080/");
        System.out.println("  测试账号: admin / 123456");
        System.out.println("================================================");
    }
}
