package org.cbioportal.pdb_annotation.scripts;

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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
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
public class PdbScriptsPipelineStarter {

	public BlastDataBase db;
	public int matches;
	public int ensembl_file_count;	

	/**
	 * Construct function
	 */
	public PdbScriptsPipelineStarter() {
		// Initiate
		this.db = new BlastDataBase(Constants.pdb_seqres_fasta_file);
		this.matches = 0;
		this.ensembl_file_count = -1;
	}	

	/**
	 * run external command by ProcessBuilder, now it only contains
	 * "makeblastdb" and "blastp", will add more in future
	 * 
	 * @param command
	 * @return Success/Failure
	 */
	public boolean run(String command) {
		if (command.equals("makeblastdb")) {
			try {
				System.out.println("[BLAST] Running makeblastdb command...");
				ProcessBuilder dbBuilder = new ProcessBuilder(makeBlastDBCommand());
				Process makeDB = dbBuilder.start();
				makeDB.waitFor();
				System.out.println("[BLAST] Command makeblastdb complete");
			} catch (Exception ee) {
				System.err.println("[BLAST] Fatal Error: Could not Successfully Run makeblastdb command");
				ee.printStackTrace();
			}
		} else if (command.equals("blastp")) {
			try {
				System.out.println("[BLAST] Running blastp command...");
				if (this.ensembl_file_count != -1) {
					for (int i = 0; i < this.ensembl_file_count; i++) {
						ProcessBuilder blastp = new ProcessBuilder(makeBlastPCommand(i));
						Process blast_standalone = blastp.start();
						blast_standalone.waitFor();
					}
				} else {
					ProcessBuilder blastp = new ProcessBuilder(makeBlastPCommand());
					Process blast_standalone = blastp.start();
					blast_standalone.waitFor();
				}
				System.out.println("[BLAST] Command blastp complete");
			} catch (Exception ee) {
				System.err.println("[BLAST] Fatal Error: Could not Successfully Run blastp command");
				ee.printStackTrace();
			}
		} else {
			System.out.println("[Shell] Error: Could not recognize Command: " + command);
		}
		return true;
	}

	/**
	 * run external command by ProcessBuilder, with redirect maker "<"
	 * 
	 * @param command
	 *            contents before "<"
	 * @param arguments
	 *            contents after ">"
	 * @param checkmultipleTag
	 *            True for split capable files, false for non-split files
	 * @return Success/Failure
	 */
	public boolean runwithRedirect(String command, String arguments, boolean checkmultipleTag) {
		if (command.equals("mysql")) {
			try {
				System.out.println("[DATABASE] Running mysql command...");

				if (checkmultipleTag && this.ensembl_file_count != -1) {
					for (int i = 0; i < this.ensembl_file_count; i++) {
						System.out.println("[DATABASE] Running mysql command on " + i + "th sql ...");
						long startTime = System.currentTimeMillis();
						ProcessBuilder mysql = new ProcessBuilder(makeDBCommand());
						mysql.redirectInput(
								ProcessBuilder.Redirect.from(new File(arguments + "." + new Integer(i).toString())));
						Process mysql_standalone = mysql.start();
						mysql_standalone.waitFor();
						long endTime = System.currentTimeMillis();
						NumberFormat formatter = new DecimalFormat("#0.000");
						System.out.println("[Shell] " + i + "th sql Execution time is "
								+ formatter.format((endTime - startTime) / 1000d) + " seconds");
					}
				} else {
					ProcessBuilder builder = new ProcessBuilder(makeDBCommand());
					builder.redirectInput(ProcessBuilder.Redirect.from(new File(arguments)));
					Process mysql_standalone = builder.start();
					mysql_standalone.waitFor();
				}
				System.out.println("[DATABASE] Command mysql complete");
			} catch (Exception ee) {
				ee.printStackTrace();
				System.err.println("[DATABASE] Fatal Error: Could not Successfully Run mysql command on " + arguments);
			}
		} else {
			System.out.println("[Shell] Error: Could not recognize Command: " + command);
		}
		return true;
	}



	
	/**
	 * Helper Function for building the makeblastdb command: makeblastdb -in
	 * Homo_sapiens.GRCh38.pep.all.fa -dbtype prot -out pdb_seqres.db
	 * 
	 * @return A List containing the commands to execute the makeblastdb
	 *         function
	 */
	private List<String> makeBlastDBCommand() {
		List<String> list = new ArrayList<String>();
		list.add(Constants.makeblastdb);
		list.add("-in");
		list.add(Constants.workspace + Constants.pdb_seqres_fasta_file);
		list.add("-dbtype");
		list.add("prot");
		list.add("-out");
		list.add(Constants.workspace + this.db.dbName);
		return list;
	}

	/**
	 * Helper Function for building the following command : 
	 * blastp -db pdb_seqres.db -query Homo_sapiens.GRCh38.pep.all.fa -word_size 11 -evalue  1e-60 -num_threads 6 -outfmt 5 -out pdb_seqres.xml
	 * 
	 * @return A List of command arguments for the processbuilder
	 */
	private List<String> makeBlastPCommand() {
		List<String> list = generateBlastCommand("");
		return list;
	}

	/**
	 * This is for the multiple inputs
	 * blastp -db pdb_seqres.db -query Homo_sapiens.GRCh38.pep.all.fa.0 -word_size 11 -evalue  1e-60 -num_threads 6 -outfmt 5 -out pdb_seqres.xml.0
	 * 
	 * @param i
	 * 			the ith file
	 * @return
	 */
	private List<String> makeBlastPCommand(int i) {
		String countStr = "." + new Integer(i).toString();
		List<String> list = generateBlastCommand(countStr);
		return list;
	}
	
	/**
	 * main body of generating blast command
	 * @param countStr
	 * @return
	 */
	private List<String> generateBlastCommand(String countStr){
		List<String> list = new ArrayList<String>();
		// Building the following process command
		list.add(Constants.blastp);
		list.add("-db");
		list.add(Constants.workspace + this.db.dbName);
		list.add("-query");
		list.add(Constants.workspace + Constants.ensembl_fasta_file + countStr);
		// list.add("-word_size");
		// list.add("11");
		list.add("-evalue");
		list.add(Constants.blast_para_evalue);
		list.add("-num_threads");
		list.add(Constants.blast_para_threads);
		list.add("-outfmt");
		list.add("5");
		list.add("-out");
		list.add(Constants.workspace + this.db.resultfileName + countStr);
		return list;	
	}

	/**
	 * mysql command
	 * 
	 * @return
	 */
	private List<String> makeDBCommand() {
		List<String> list = new ArrayList<String>();
		// Building the following process command
		list.add(Constants.mysql);
		list.add("--max_allowed_packet=1024M");
		list.add("-u");
		list.add(Constants.username);
		list.add("--password=" + Constants.password);
		list.add(Constants.db_schema);
		return list;
	}

	

	/**
	 * main function, run the commands
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		PdbScriptsPipelineStarter app = new PdbScriptsPipelineStarter();

		long startTime = System.currentTimeMillis();
		
		PdbScriptsPipelinePreprocessing preprocess= new PdbScriptsPipelinePreprocessing();
		// Step 1: choose only protein entries of all pdb
		preprocess.preprocessPDBsequences(Constants.workspace + Constants.pdb_seqres_download_file,Constants.workspace + Constants.pdb_seqres_fasta_file);

		// Step 2: preprocess ensembl files, split into small files to save the memory
		app.ensembl_file_count=preprocess.preprocessGENEsequences(Constants.workspace + Constants.ensembl_download_file,Constants.workspace + Constants.ensembl_fasta_file);
		
		// Step 3: build the database by makebalstdb
		app.run("makeblastdb");

		// Step 4: blastp ensembl genes against pdb
		// Warning: This step takes time
		app.run("blastp");

		PdbScriptsPipelineMakeSQL parseprocess = new PdbScriptsPipelineMakeSQL(app);
		// Step 5: parse results and output as input sql statments
		parseprocess.parse2sql(0);

		// Step 6: create data schema
		app.runwithRedirect("mysql", Constants.resource_dir + Constants.db_schema_script, false);

		// Step 7: import INSERT SQL statements into the database
		// Warning: This step takes time
		app.runwithRedirect("mysql", Constants.workspace + Constants.db_input_script, true);

		long endTime = System.currentTimeMillis();
		NumberFormat formatter = new DecimalFormat("#0.000");
		System.out.println("[Shell] Execution time is " + formatter.format((endTime - startTime) / 1000d) + " seconds");

	}
}
