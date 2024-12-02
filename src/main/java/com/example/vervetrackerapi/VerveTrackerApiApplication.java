package com.example.vervetrackerapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VerveTrackerApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(VerveTrackerApiApplication.class, args);
    }

}
