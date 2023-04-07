package com.kongtoon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class WebComicsApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebComicsApplication.class, args);
	}

}
