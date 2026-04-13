package com.bililive.web;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages = {"com.bililive"})
@MapperScan("com.bililive.mapper")
@EnableTransactionManagement
@EnableScheduling
public class BililiveWebRunApplication {
    public static void main(String[] args) {
        SpringApplication.run(BililiveWebRunApplication.class, args);
    }
}
