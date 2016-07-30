package org.cbioportal.pdb_annotation.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Main SpringBoot Application
 *
 * @author Juexin Wang
 *
 */
@SpringBootApplication
@EnableSwagger2
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public Docket annotationApi() {
        // default swagger definition file location: <root>/v2/api-docs?group=pdb_annotation
        // default swagger UI location: <root>/swagger-ui.html
        return new Docket(DocumentationType.SWAGGER_2)
            .groupName("pdb_annotation")
            .apiInfo(annotationApiInfo())
            .select()
            .paths(PathSelectors.regex("/pdb_annotation.*"))
            .build();
    }

    private ApiInfo annotationApiInfo() {
        return new ApiInfoBuilder()
            .title("PDB Annotation API")
            .description("PDB Annotation API")
            //.termsOfServiceUrl("http://terms-of-service-url")
            .contact("CMO, MSKCC")
            .license("GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
            .licenseUrl("https://github.com/cBioPortal/pdb-annotation/blob/master/LICENSE")
            .version("2.0")
            .build();
    }
}
