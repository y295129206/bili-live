package com.bililive.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.bililive"})
@MapperScan("com.bililive.mapper")
@EnableTransactionManagement
@EnableScheduling
public class BililiveAdminRunApplication {
    public static void main(String[] args) {
        SpringApplication.run(BililiveAdminRunApplication.class, args);
    }
}
