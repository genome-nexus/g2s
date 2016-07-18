package org.cbioportal.pdb_annotation.scripts;

import org.cbioportal.pdb_annotation.util.*;

import java.io.File;
import java.io.FileWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Timer;

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
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Component;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Preliminary script, includes several steps
 * How to run this project:
Step 1. Init the Database
1. Create an empty database schema named as "pdb", username as "cbio", password as "cbio" in mysql:
	In mysql prompt,type:
	CREATE USER 'cbio'@'localhost' IDENTIFIED BY 'cbio';
	GRANT ALL PRIVILEGES ON * . * TO 'cbio'@'localhost';
	FLUSH PRIVILEGES;
	create database pdb;
2. In your code workspace, git clone https://github.com/cBioPortal/pdb-annotation.git
3. Change settings in src/main/resources/application.properties 
	(i) Change workspace to the input sequences located ${workdir}. 
	(ii)Change resource_dir to "~/pdb-annotation/pdb/src/main/resources/"  
	(iii)Change ensembl_input_interval for memory performance consideration
	(iv) * If you want to use other test ensembl sequences, please change both ensembl_download_file and ensembl_fasta_file in your workspace
4. mvn package
5. in pdb-annotation/pdb-alignment-pipeline/target/: java -jar -Xmx7000m pdb-0.1.0.jar init
 
Step 2. Check the API
1. in pdb-annotation/pdb-alignment-api/: mvn spring-boot:run
2. open your web browser:
http://localhost:8080/StructureMappingQuery?ensemblid=ENSP00000483207.2
http://localhost:8080/ProteinIdentifierRecognitionQuery?ensemblid=ENSP00000483207.2

Step 3. Weekly update
1. Using CRON to run command in pdb-annotation/pdb-alignment-pipeline/target/: java -jar -Xmx7000m pdb-0.1.0.jar update
or 
in pdb-annotation/pdb-alignment-pipeline/target/: java -jar -Xmx7000m pdb-0.1.0.jar updateweekly

Please let me know if you have questions.
 * 
 * @author Juexin Wang
 *
 */
@SpringBootApplication
@PropertySource("classpath:application.properties")
@EnableScheduling
@Configuration
@ComponentScan
@EnableAutoConfiguration
public class PdbScriptsPipelineStarter {

	/**
	 * main function, run the commands
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		/**
		 * Check arguments
		 */
		if(args.length != 1){
			System.out.println("Usage:\n"
					+ "Initiate the database"
					+ "java -jar pdb-alignment-pipeline init\n"
					+ "or\n"
					+ "Update the database weekly, user could deploy and change the settings in application.approperties\n"
					+ "java -jar pdb-alignment-pipeline updateweekly\n"
					+ "or\n"
					+ "update for update immediately , users should use CRON or other scheduling mechanisms to run the updates\n"
					+ "java -jar pdb-alignment-pipeline update\n");
			System.exit(0);
		}
				
		long startTime = System.currentTimeMillis();		
		
		/**
		 * Argument init for initiate the database 
		 */
		if(args[0].equals("init")){
			//SpringApplication app = new SpringApplication(PdbScriptsPipelineStarter.class);
			//app.run();
			PdbScriptsPipelineRunCommand app = new PdbScriptsPipelineRunCommand();
			app.runInit();
		}
		/**
		 * Argument update for update weekly, user could deploy and change the settings in application.approperties
		 */
		else if(args[0].equals("updateweekly")){
			//SpringApplication app = new SpringApplication(PdbScriptsPipelineUpdate.class);
			//app.run();
			
			ReadConfig rc = new ReadConfig();
			
	        Calendar calendar = Calendar.getInstance();
	        calendar.set(Calendar.DAY_OF_WEEK, Integer.parseInt(rc.updateDAY_OF_WEEK));
	        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(rc.updateHOUR_OF_DAY));
	        calendar.set(Calendar.MINUTE, Integer.parseInt(rc.updateMINUTE));
	        calendar.set(Calendar.SECOND, Integer.parseInt(rc.updateSECOND));
	        calendar.set(Calendar.MILLISECOND, Integer.parseInt(rc.updateMILLISECOND));

	        Timer time = new Timer(); // Instantiate Timer Object

	        //PDB is updated at Tuesday, 5 pm PDT during daylight saving time in the US, and 4 pm PST otherwise
	        //We choose running the task on Tuesday at Central Time 19:10:00 
	        time.schedule(new ScheduleUpdateTask(), calendar.getTime(), Integer.parseInt(rc.updateDELAY));			
		}
		/**
		 * Argument update for update immediately , users should use CRON or other scheduling mechanisms to run the updates 
		 */
		else if(args[0].equals("update")){
			PdbScriptsPipelineRunCommand app = new PdbScriptsPipelineRunCommand();
	        app.runUpdatePDB();			
		}
		/**
		 * If the Argument is not right
		 */
		else{
			System.out.println("The arguments should be init, updateweekly or update\n");
		}
		
		long endTime = System.currentTimeMillis();
		NumberFormat formatter = new DecimalFormat("#0.000");
		System.out.println("[Shell] All Execution time is " + formatter.format((endTime - startTime) / 1000d) + " seconds");
	}
}
