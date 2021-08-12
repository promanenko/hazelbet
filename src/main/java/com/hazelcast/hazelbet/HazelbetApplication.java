package com.hazelcast.hazelbet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class HazelbetApplication {

    public static void main(String[] args) {
        SpringApplication.run(HazelbetApplication.class, args);
    }

}
