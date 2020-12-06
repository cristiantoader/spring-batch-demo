package com.ing.fmjavaguild.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.PropertySource;

@SpringBootApplication
@EnableBatchProcessing
@PropertySource("classpath:./secret.properties")
public class SpringBatchEtlCsvImporterApp {

    public static void main(String[] args) {
        SpringApplication.run(SpringBatchEtlCsvImporterApp.class, args);
    }
}
