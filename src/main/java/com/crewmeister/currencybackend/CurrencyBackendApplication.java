package com.crewmeister.currencybackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CurrencyBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(CurrencyBackendApplication.class, args);
    }

}
