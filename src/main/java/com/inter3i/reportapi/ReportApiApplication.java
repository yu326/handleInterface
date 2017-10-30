package com.inter3i.reportapi;

import javafx.application.Application;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.web.support.SpringBootServletInitializer;

//部署到Tomcat服务器时，禁用mongo默认配置
@SpringBootApplication
//		(exclude = {MongoAutoConfiguration.class,MongoDataAutoConfiguration.class})
public class ReportApiApplication extends SpringBootServletInitializer implements EmbeddedServletContainerCustomizer {

	@Override
	protected
	SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
		return builder.sources(Application.class
		);
	}

	public static void main(String[] args) {
		SpringApplication.run(ReportApiApplication.class, args);
	}

	//加上这部分，上面的必须继承才能注入
	@Override
	public void
	customize(ConfigurableEmbeddedServletContainer container) {
		container.setPort(
				15000
		);
	}


}
