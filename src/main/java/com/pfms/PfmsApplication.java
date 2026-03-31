package com.pfms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Personal Finance Management System.
 * Enables scheduling for budget alerts and notifications.
 */
@SpringBootApplication
@EnableScheduling
public class PfmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PfmsApplication.class, args);
    }
}
