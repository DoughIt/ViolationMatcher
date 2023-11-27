package cn.edu.fudan.violationtracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Jerry Zhang <zhangjian16@fudan.edu.cn>
 * @date 2023/11/27 14:07
 */
@SpringBootApplication

public class ViolationMatchApplication {
    public ViolationMatchApplication() {
    }

    public static void main(String[] args) {
        SpringApplication.run(ViolationMatchApplication.class, args);
    }
}
