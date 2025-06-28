package com.semicolon.africa.tapprbackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TapprBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(TapprBackendApplication.class, args);
    }

}
