package com.epsilon.auto.poc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.epsilon.auto.poc.rest"})
public class AwsS3NotifyApplication {

	public static void main(String[] args) {
		SpringApplication.run(AwsS3NotifyApplication.class, args);
	}

}
