package org.cbioportal.pdb_annotation.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.FileUtils;
import org.cbioportal.pdb_annotation.util.ReadConfig;
import org.cbioportal.pdb_annotation.util.blast.BlastDataBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;


@Component
@EnableConfigurationProperties
@PropertySource("classpath:application.properties")
public class PdbScriptsPipelineRunCommand {
	
	public BlastDataBase db;
	public int matches;
	public int ensembl_file_count;
	ReadConfig rc;
	public String dataVesrsion;


	/**
	 * Construct function
	 */
	public PdbScriptsPipelineRunCommand() {		
		// Initiate		
		ReadConfig rc = new ReadConfig();
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
	public boolean run(String command, String currentDir) {
		if (command.equals("makeblastdb")) {
			try {
				System.out.println("[BLAST] Running makeblastdb command...");
				ProcessBuilder dbBuilder = new ProcessBuilder(makeBlastDBCommand(currentDir));
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
						ProcessBuilder blastp = new ProcessBuilder(makeBlastPCommand(currentDir, i));
						Process blast_standalone = blastp.start();
						blast_standalone.waitFor();
					}
				} else {
					ProcessBuilder blastp = new ProcessBuilder(makeBlastPCommand(currentDir));
					Process blast_standalone = blastp.start();
					blast_standalone.waitFor();
				}
				System.out.println("[BLAST] Command blastp complete");
			} catch (Exception ee) {
				System.err.println("[BLAST] Fatal Error: Could not Successfully Run blastp command");
				ee.printStackTrace();
			}
		}  
		else {
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
	public boolean runwithRedirectFrom(String command, String arguments, boolean checkmultipleTag) {
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

	
	public boolean runwithRedirectTo(String command, String inputname, String outputname){
		if(command.equals("gunzip")){
			try{
				ProcessBuilder builder = new ProcessBuilder(makeGunzipCommand(inputname));
				builder.redirectOutput(ProcessBuilder.Redirect.to(new File(outputname)));
				Process mysql_standalone = builder.start();
				mysql_standalone.waitFor();
			}catch(Exception ee){
				ee.printStackTrace();
				System.err.println("[SHELL] Fatal Error: Could not Successfully Run gunzip command on "+inputname+" to " + outputname);
			}
			
		}
		return true;
	}
	
	private List<String> makeGunzipCommand(String inputname){
		List<String> list = new ArrayList<String>();
		list.add("gunzip");
		list.add("-c");
		list.add("-d");
		list.add(inputname);
		return list;
	}


	
	/**
	 * Helper Function for building the makeblastdb command: makeblastdb -in
	 * Homo_sapiens.GRCh38.pep.all.fa -dbtype prot -out pdb_seqres.db
	 * 
	 * @return A List containing the commands to execute the makeblastdb
	 *         function
	 */
	private List<String> makeBlastDBCommand(String currentDir) {
		List<String> list = new ArrayList<String>();
		list.add(rc.makeblastdb);
		list.add("-in");
		list.add(currentDir +rc.pdb_seqres_fasta_file);
		list.add("-dbtype");
		list.add("prot");
		list.add("-out");
		list.add(currentDir + this.db.dbName);
		return list;
	}

	/**
	 * Helper Function for building the following command : 
	 * blastp -db pdb_seqres.db -query Homo_sapiens.GRCh38.pep.all.fa -word_size 11 -evalue  1e-60 -num_threads 6 -outfmt 5 -out pdb_seqres.xml
	 * 
	 * @return A List of command arguments for the processbuilder
	 */
	private List<String> makeBlastPCommand(String currentDir) {
		List<String> list = generateBlastCommand(currentDir,"");
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
	private List<String> makeBlastPCommand(String currentDir, int i) {
		String countStr = "." + new Integer(i).toString();
		List<String> list = generateBlastCommand(currentDir, countStr);
		return list;
	}
	
	/**
	 * main body of generating blast command
	 * @param countStr
	 * @return
	 */
	private List<String> generateBlastCommand(String currentDir, String countStr){
		List<String> list = new ArrayList<String>();
		// Building the following process command
		list.add(rc.blastp);
		list.add("-db");
		list.add(currentDir + this.db.dbName);
		list.add("-query");
		list.add(rc.workspace + rc.ensembl_fasta_file + countStr);
		// list.add("-word_size");
		// list.add("11");
		list.add("-evalue");
		list.add(rc.blast_para_evalue);
		list.add("-num_threads");
		list.add(rc.blast_para_threads);
		list.add("-outfmt");
		list.add("5");
		list.add("-out");
		list.add(currentDir + this.db.resultfileName + countStr);
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
		list.add(rc.mysql);
		list.add("--max_allowed_packet=1024M");
		list.add("-u");
		list.add(rc.username);
		list.add("--password=" + rc.password);
		list.add(rc.db_schema);
		return list;
	}
	
	private List<String> makeDownloadCommand(String urlFilename, String localFilename) {
		List<String> list = new ArrayList<String>();
		// Building the following process command
		list.add("wget");
		list.add("-O");
		list.add(localFilename);
		list.add(urlFilename);
		return list;
	}
	
	
	public boolean downloadfile(String urlFilename, String localFilename) {
			try {
				System.out.println("[SHELL] Download file "+urlFilename+" ...");
				ProcessBuilder dbBuilder = new ProcessBuilder(makeDownloadCommand(urlFilename,localFilename ));
				Process makeDB = dbBuilder.start();
				makeDB.waitFor();
				System.out.println("[SHELL] "+urlFilename+" completed");
			} catch (Exception ee) {
				System.err.println("[SHELL] Fatal Error: Could not Successfully download files");
				ee.printStackTrace();
			}	
		return true;
	}
	
	public List<String> readFTPfile2List(String urlStr){
		List<String> list = new ArrayList();
		try{
			URL url = new URL(urlStr);
	        URLConnection con = url.openConnection();
	        BufferedReader in = new BufferedReader(new InputStreamReader(
	                                    con.getInputStream()));
	        String inputLine;
	        while ((inputLine = in.readLine()) != null) 
	        	list.add(inputLine);
	        in.close();
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return list;
	}
	
	public String readFTPfile2Str(String urlStr){
		String str ="";
		try{
			URL url = new URL(urlStr);
	        URLConnection con = url.openConnection();
	        BufferedReader in = new BufferedReader(new InputStreamReader(
	                                    con.getInputStream()));
	        String inputLine;
	        while ((inputLine = in.readLine()) != null) 
	        	str=str+inputLine+"\n";
	        in.close();
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return str;
	}
	
	public List<String> prepareUpdatePDBFile(String currentDir, String updateTxt, String delPDB){
		List<String> listOld=new ArrayList<String>();
		try{
			System.out.println("[SHELL] Weekly Update: Create deleted list");
			
			
			FileUtils.forceMkdir(new File(currentDir));
			
			String addFileName = currentDir+updateTxt;
			File addFastaFile = new File(addFileName);
			
			String delFileName = currentDir+delPDB;
			
			List listAdd = readFTPfile2List("ftp://ftp.pdbj.org/pub/pdb/data/status/latest/added.pdb"); 
						
			List listMod = readFTPfile2List("ftp://ftp.pdbj.org/pub/pdb/data/status/latest/modified.pdb");
						
			List listObs = readFTPfile2List("ftp://ftp.pdbj.org/pub/pdb/data/status/latest/obsolete.pdb");
			
			List<String> listNew = new ArrayList<String>(listAdd);
			listNew.addAll(listMod);
			
			listOld = new ArrayList<String>(listMod);
			listOld.addAll(listObs);
			
			String listNewCont = "";
			for(String pdbName:listNew){
				listNewCont = listNewCont + readFTPfile2Str("http://www.rcsb.org/pdb/files/fasta.txt?structureIdList="+pdbName);
			}
						
			FileUtils.writeStringToFile(addFastaFile, listNewCont);			
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return listOld;
	}
	
	
	
	
	public void runInit(){
		this.db = new BlastDataBase(rc.pdb_seqres_fasta_file);
		PdbScriptsPipelinePreprocessing preprocess= new PdbScriptsPipelinePreprocessing(rc.ensembl_input_interval);
		
		// Step 1: Download essential PDB and Essential tools 
		downloadfile(rc.pdbwholeSource,rc.workspace +rc.pdbwholeSource.substring(rc.pdbwholeSource.lastIndexOf("/")+1));
		//runwithRedirectTo("gunzip", rc.workspace +rc.pdbwholeSource.substring(rc.pdbwholeSource.lastIndexOf("/")+1), rc.workspace+rc.pdb_seqres_download_file);
		
		downloadfile(rc.ensemblwholeSource,rc.workspace +rc.ensemblwholeSource.substring(rc.ensemblwholeSource.lastIndexOf("/")+1));
		//runwithRedirectTo("gunzip", rc.workspace +rc.ensemblwholeSource.substring(rc.ensemblwholeSource.lastIndexOf("/")+1), rc.workspace+rc.ensembl_download_file);
		
		// Step 1: choose only protein entries of all pdb
		preprocess.preprocessPDBsequences(rc.workspace + rc.pdb_seqres_download_file,rc.workspace + rc.pdb_seqres_fasta_file);

		// Step 2: preprocess ensembl files, split into small files to save the memory
		ensembl_file_count=preprocess.preprocessGENEsequences(rc.workspace + rc.ensembl_download_file,rc.workspace + rc.ensembl_fasta_file);
		
		// Step 3: build the database by makebalstdb
		run("makeblastdb",rc.workspace);

		// Step 4: blastp ensembl genes against pdb
		// Warning: This step takes time
		run("blastp",rc.workspace);

		PdbScriptsPipelineMakeSQL parseprocess = new PdbScriptsPipelineMakeSQL(this, rc);
		// Step 5: parse results and output as input sql statments
		parseprocess.parse2sql(0, rc.workspace);

		// Step 6: create data schema
		runwithRedirectFrom("mysql", rc.resource_dir + rc.db_schema_script, false);

		// Step 7: import INSERT SQL statements into the database
		// Warning: This step takes time
		runwithRedirectFrom("mysql", rc.workspace + rc.sql_insert_file, true);
	}
	
	
	public void runUpdatePDB(){
		String dataVersion = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
		this.dataVesrsion = dataVersion;
		this.db = new BlastDataBase(rc.pdb_seqres_download_file);
		
		String currentDir = rc.workspace+this.dataVesrsion+"/";
		PdbScriptsPipelinePreprocessing preprocess= new PdbScriptsPipelinePreprocessing();
		
		List<String> listOld = prepareUpdatePDBFile(currentDir,rc.pdb_seqres_download_file, rc.delPDB);
		
		preprocess.preprocessPDBsequencesUpdate(currentDir+rc.pdb_seqres_download_file, currentDir+rc.pdb_seqres_fasta_file);
		
		run("makeblastdb", currentDir);

		run("blastp",  currentDir);
		
		PdbScriptsPipelineMakeSQL parseprocess = new PdbScriptsPipelineMakeSQL(this, rc);
		
		parseprocess.parse2sql(1, currentDir);
		
		runwithRedirectFrom("mysql", currentDir + rc.sql_insert_file, false);
		
		
		//delete old
		
		parseprocess.generateDeleteSql(currentDir, listOld);
		runwithRedirectFrom("mysql", currentDir + rc.sql_delete_file, false);
		
		
	}

}
