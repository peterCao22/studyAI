package com.marcopolo.hima01;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.marcopolo.hima01"})
public class Hima01Application {

    public static void main(String[] args) {
        SpringApplication.run(Hima01Application.class, args);
    }

}
