package com.tianyoukeji.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {
	@Bean
	public Docket createRestApi() {
		return new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(new ApiInfoBuilder().title("天邮科技平台后台开发接口").description("平台后台").version("1.0")
						.contact(new Contact("李扬", "https://github.com/readme916", "1290144599@qq.com"))
						.license("The Apache License").build())
				.select().apis(RequestHandlerSelectors.basePackage("com.tianyoukeji.platform.controller"))
				.paths(PathSelectors.any()).build();
	}
}
