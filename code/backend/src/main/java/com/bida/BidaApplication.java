package com.bida;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BidaApplication {

    public static void main(String[] args) {
        SpringApplication.run(BidaApplication.class, args);
    }
}
