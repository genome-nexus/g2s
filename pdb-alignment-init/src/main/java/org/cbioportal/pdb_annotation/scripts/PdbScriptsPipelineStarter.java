package org.cbioportal.pdb_annotation.scripts;

import org.cbioportal.pdb_annotation.util.*;

import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.biojava.nbio.core.search.io.blast.BlastXMLParser;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.io.FastaReaderHelper;
import org.biojava.nbio.core.sequence.io.FastaWriterHelper;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.DBRef;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureIO;
import org.cbioportal.pdb_annotation.util.*;
import org.cbioportal.pdb_annotation.util.blast.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Preliminary script, includes several steps
 * Step 1: choose only protein entries of all pdb 
 * Step 2: preprocess ensembl files, split into small files to save the memory 
 * Step 3: build the database by makebalstdb 
 * Step 4: blastp ensembl genes against pdb (* Takes time) 
 * Step 5: parse results and output as input sql statments 
 * Step 6: create data schema
 * Step 7: import INSERT SQL statements into the database (* Takes time)
 * 
 * @author Juexin Wang
 *
 */
@SpringBootApplication
@PropertySource("classpath:application.properties")
public class PdbScriptsPipelineStarter {

	/**
	 * main function, run the commands
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		
		long startTime = System.currentTimeMillis();
		
		
		
		//ApplicationContext ctx = new AnnotationConfigApplicationContext(PdbConf.class);		
		//PdbScriptsPipelineRunCommand app = ctx.getBean(PdbScriptsPipelineRunCommand.class);
		
		PdbScriptsPipelineRunCommand app = new PdbScriptsPipelineRunCommand();
		
		//app.runInit();
		
		app.runUpdatePDB();
		
		

		long endTime = System.currentTimeMillis();
		NumberFormat formatter = new DecimalFormat("#0.000");
		System.out.println("[Shell] Execution time is " + formatter.format((endTime - startTime) / 1000d) + " seconds");

	}
}
