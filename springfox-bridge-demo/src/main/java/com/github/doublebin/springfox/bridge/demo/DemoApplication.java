package com.github.doublebin.springfox.bridge.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        new SpringApplicationBuilder(DemoApplication.class).web(true).run(args);
    }

    //@Bean
    public Docket createRestApi(@Value("${swagger.pathSelectors.regex:/.*}") String regex)
    {
        return new Docket(DocumentationType.SWAGGER_2).groupName("Needle service")
            .genericModelSubstitutes(DeferredResult.class)
            .useDefaultResponseMessages(false)
            .forCodeGeneration(true)
            .pathMapping("/")
            .select()
            .paths(PathSelectors.regex(regex))
            // .apis(RequestHandlerSelectors.basePackage("org.starryspace.microservice.controller"))

            .build()
            .apiInfo(apiInfo());
    }

    private ApiInfo apiInfo()
    {
        return new ApiInfoBuilder().title("Restful apis for needle service.")
            .description("Needle service apis")
            .termsOfServiceUrl("http://doublebin.site/")
            .contact("lubinbin")
            .version("1.0")
            .build();
    }
}
