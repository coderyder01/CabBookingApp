package com.example.rides.dispatch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.rides")
public class DispatchServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DispatchServiceApplication.class, args);
    }
}
