package org.cbioportal.pdb_annotation.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;


import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 
 * @author Juexin Wang
 *
 */
@SpringBootApplication
@SpringBootConfiguration
@EnableSwagger2
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public Docket annotationApi() {
        // default swagger definition file location:
        // <root>/v2/api-docs?group=pdb_annotation
        // default swagger UI location: <root>/swagger-ui.html
        return new Docket(DocumentationType.SWAGGER_2).groupName("api").apiInfo(annotationApiInfo()).select()
                .paths(PathSelectors.regex("/api.*")).build();
    }

    private ApiInfo annotationApiInfo() {
        /*
        return new ApiInfoBuilder().title("G2S API").description(
                "A Genome to Strucure (G2S) API Supports Automated Mapping and Annotating Genomic Variants in 3D Protein Structures. Supports Inputs from Human Genome Position, Uniprot and Human Ensembl Names")
                // .termsOfServiceUrl("http://terms-of-service-url")
                .termsOfServiceUrl("http://g2s.genomenexus.org")
                .contact("CMO, MSKCC").license("GNU AFFERO GENERAL PUBLIC LICENSE Version 3")
                .licenseUrl("https://github.com/cBioPortal/pdb-annotation/blob/master/LICENSE").version("2.0").build();
                */
        ApiInfo apiInfo = new ApiInfo(
                "G2S web API",
                "A Genome to Strucure (G2S) API Supports Automated Mapping and Annotating Genomic Variants in 3D Protein Structures. Supports Inputs from Human Genome Position, Uniprot and Human Ensembl Names.",
                "1.0 (beta)",
                "g2s.genomenexus.org",
                new Contact("G2S", "http://g2s.genomenexus.org", "wangjue@missouri.edu"),
                "License",
                "https://github.com/cBioPortal/cbioportal/blob/master/LICENSE");
        return apiInfo;
    }

}
