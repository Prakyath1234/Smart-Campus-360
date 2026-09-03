package com.smartcampus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

import java.util.Arrays;

@SpringBootApplication
public class SmartCampusApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SmartCampusApplication.class);

    @Autowired
    private Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(SmartCampusApplication.class, args);
    }

    @Override
    public void run(String... args) {
        logger.info("=================== DATABASE STARTUP DIAGNOSTICS ===================");
        logger.info("Active Spring Profiles : {}", Arrays.toString(environment.getActiveProfiles()));
        logger.info("DB_HOST               : {}", mask(environment.getProperty("DB_HOST")));
        logger.info("DB_PORT               : {}", environment.getProperty("DB_PORT"));
        logger.info("DB_NAME               : {}", environment.getProperty("DB_NAME"));
        logger.info("DB_USERNAME           : {}", mask(environment.getProperty("DB_USERNAME")));
        logger.info("DB_SSL                : {}", environment.getProperty("DB_SSL"));
        logger.info("====================================================================");
    }

    private String mask(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "<not set>";
        }
        if (value.length() <= 6) {
            return "***";
        }
        return value.substring(0, 4) + "***" + value.substring(value.length() - 4);
    }
}
