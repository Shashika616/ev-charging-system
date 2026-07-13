package com.evcharging.gridservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class GridServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(GridServiceApplication.class, args);
	}

}
