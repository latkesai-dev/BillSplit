package com.billsplit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BillSplitApplication {
    public static void main(String[] args) {
        SpringApplication.run(BillSplitApplication.class, args);
    }
}
