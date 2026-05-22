package com.example.rides.surge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.rides")
public class SurgeServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(SurgeServiceApplication.class, args);
    }
}
