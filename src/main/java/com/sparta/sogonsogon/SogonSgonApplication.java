package com.sparta.sogonsogon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class SogonSgonApplication {

    public static void main(String[] args) {
        SpringApplication.run(SogonSgonApplication.class, args);
    }

}
