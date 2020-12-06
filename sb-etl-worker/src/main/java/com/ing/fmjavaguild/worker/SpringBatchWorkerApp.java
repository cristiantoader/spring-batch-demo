package com.ing.fmjavaguild.worker;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableBatchProcessing
public class SpringBatchWorkerApp {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchWorkerApp.class, args);
    }
}
