package com.tianyoukeji.org;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import com.liyang.jpa.smart.query.annotation.EnableJpaSmartQuery;


@SpringBootApplication
@EnableJpaAuditing
@EnableJpaSmartQuery
@EnableJpaRepositories(basePackages = {"com.tianyoukeji.parent.entity"})
@EntityScan(basePackages= {"com.tianyoukeji.parent.entity"})
public class OrgApplication {
	protected final static Logger logger = LoggerFactory.getLogger(OrgApplication.class);
	public static void main(String[] args) {
		SpringApplication.run(OrgApplication.class, args);
	}
}
