package com.adamfgcross.concurrentcomputations;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
public class ConcurrentcomputationsApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConcurrentcomputationsApplication.class, args);
	}

}
