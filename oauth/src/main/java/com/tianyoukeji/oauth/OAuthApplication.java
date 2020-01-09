package com.tianyoukeji.oauth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = {"com.tianyoukeji.base.entity"})
@EntityScan(basePackages= {"com.tianyoukeji.base.entity"})
@EnableJpaAuditing
public class OAuthApplication {
	protected final static Logger logger = LoggerFactory.getLogger(OAuthApplication.class);

	public static void main(String[] args) {

		SpringApplication.run(OAuthApplication.class, args);

	}
}
