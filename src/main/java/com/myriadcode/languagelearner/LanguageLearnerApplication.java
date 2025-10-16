package com.myriadcode.languagelearner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@EntityScan
@SpringBootApplication
public class LanguageLearnerApplication {

    public static void main(String[] args) {
        SpringApplication.run(LanguageLearnerApplication.class, args);
    }

}
