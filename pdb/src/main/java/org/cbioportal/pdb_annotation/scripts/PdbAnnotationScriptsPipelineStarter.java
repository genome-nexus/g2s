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
public class PdbAnnotationScriptsPipelineStarter {

	private BlastDataBase db;
	private int matches;
	private int ensembl_file_count;

	/**
	 * Construct function
	 */
	public PdbAnnotationScriptsPipelineStarter() {
		// Initiate
		this.db = new BlastDataBase(Constants.pdb_seqres_fasta_file);
		this.matches = 0;
		this.ensembl_file_count = -1;

	}

	/**
	 * Preprocess the PDB sequences download from PDB
	 * (ftp://ftp.rcsb.org/pub/pdb/derived_data/pdb_seqres.txt.gz) Only choose
	 * protein entries of PDB
	 * 
	 * @param infileName:
	 *            downloaded gunzip file
	 * @param outfileName:
	 *            input for makeblastdb
	 * @return Success/Failure
	 */
	public boolean preprocessPDBsequences(String infileName, String outfileName) {
		try {
			System.out.println("[Preprocessing] Preprocessing PDB sequences... ");
			LinkedHashMap<String, ProteinSequence> a = FastaReaderHelper.readFastaProteinSequence(new File(infileName));
			// FastaReaderHelper.readFastaDNASequence for DNA sequences

			StringBuffer sb = new StringBuffer();

			for (Entry<String, ProteinSequence> entry : a.entrySet()) {
				String[] tmp = entry.getValue().getOriginalHeader().toString().split("\\s+");
				if (tmp[1].equals("mol:protein")) {
					// System.out.println( entry.getValue().getOriginalHeader()
					// + "\t" + entry.getValue().getSequenceAsString() );
					sb.append(">" + entry.getValue().getOriginalHeader() + "\n" + entry.getValue().getSequenceAsString()
							+ "\n");
				}
			}
			// standard fasta output, one line has 80 coloumns
			// FastaWriterHelper.writeProteinSequence(new File(outfileName), c);

			// no-standard fasta output, one line contains all AA
			FileWriter fw = new FileWriter(new File(outfileName));
			fw.write(sb.toString());
			fw.close();
		} catch (Exception ex) {
			System.err.println("[Preprocessing] Fatal Error: Could not Successfully Preprocessing PDB sequences");
			ex.printStackTrace();
		}
		return true;
	}

	/**
	 * Preprocess the Gene sequences download from Ensembl
	 * (ftp://ftp.ensembl.org/pub/release-84/fasta/homo_sapiens/pep/Homo_sapiens.GRCh38.pep.all.fa.gz) 
	 * This function is designed to split the original FASTA file into several small files. Each small files contains
	 * Constants.ensembl_input_interval lines The purpose of doing this is
	 * saving memory in next step
	 * 
	 * @param infilename:
	 *            downloaded file
	 * @param outfilename:
	 *            processed file
	 * @return Success/Failure
	 */
	public boolean preprocessGENEsequences(String infilename, String outfilename) {
		try {
			System.out.println("[Preprocessing] Preprocessing Ensembl sequences... ");

			LinkedHashMap<String, ProteinSequence> a = FastaReaderHelper.readFastaProteinSequence(new File(infilename));
			// FastaReaderHelper.readFastaDNASequence for DNA sequences

			Collection<ProteinSequence> c = new ArrayList<ProteinSequence>();
			// line count of the original FASTA file
			int count = 0;

			// count of all generated small files
			int filecount = 0;
			for (Entry<String, ProteinSequence> entry : a.entrySet()) {
				c.add(entry.getValue());
				if (count % Constants.ensembl_input_interval == Constants.ensembl_input_interval - 1) {
					FastaWriterHelper
							.writeProteinSequence(new File(outfilename + "." + new Integer(filecount).toString()), c);
					c.clear();
					filecount++;
				}
				count++;
			}
			FastaWriterHelper.writeProteinSequence(new File(outfilename + "." + new Integer(filecount++).toString()),
					c);
			this.ensembl_file_count = filecount;
		} catch (Exception ex) {
			System.err.println("[Preprocessing] Fatal Error: Could not Successfully Preprocessing PDB sequences");
			ex.printStackTrace();
		}
		return true;
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
	 * Parse one single file of blast results to list of String, time and memory
	 * consuming for huge files
	 * 
	 * @return List<BlastResult>
	 */
	public List<BlastResult> parseblastresultsSingle() {
		List<BlastResult> results = new ArrayList<BlastResult>(this.matches);
		try {
			System.out.println("[BLAST] Read blast results from xml file...");
			File blastresults = new File(Constants.workspace + this.db.resultfileName);
			JAXBContext jc = JAXBContext.newInstance("org.cbioportal.pdb_annotation.util.blast");
			Unmarshaller u = jc.createUnmarshaller();
			u.setSchema(null);
			BlastOutput blast = (BlastOutput) u.unmarshal(blastresults);

			BlastOutputIterations iterations = blast.getBlastOutputIterations();
			int count = 1;
			for (Iteration iteration : iterations.getIteration()) {
				String querytext = iteration.getIterationQueryDef();
				IterationHits hits = iteration.getIterationHits();
				for (Hit hit : hits.getHit()) {
					BlastResult br = parseSingleAlignment(querytext, hit, count);
					results.add(br);
					count++;
				}
			}
			this.matches = count - 1;
			System.out.println("[BLAST] Total Input Queries = " + this.matches);
		} catch (Exception ee) {
			System.err.println("[BLAST] Error Parsing BLAST Result");
			ee.printStackTrace();
		}
		return results;
	}

	/**
	 * Parse XML structure into Object BlastResult
	 * 
	 * @param querytext
	 * @param hit
	 * @param count
	 * @return
	 */
	public BlastResult parseSingleAlignment(String querytext, Hit hit, int count) {
		BlastResult br = new BlastResult(count);
		br.qseqid = querytext;
		br.sseqid = hit.getHitDef().split("\\s+")[0];
		for (Hsp tmp : hit.getHitHsps().getHsp()) {
			br.ident = Double.parseDouble(tmp.getHspPositive());
			br.identp = Double.parseDouble(tmp.getHspIdentity());
			br.evalue = Double.parseDouble(tmp.getHspEvalue());
			br.bitscore = Double.parseDouble(tmp.getHspBitScore());
			br.qStart = Integer.parseInt(tmp.getHspQueryFrom());
			br.qEnd = Integer.parseInt(tmp.getHspQueryTo());
			br.sStart = Integer.parseInt(tmp.getHspHitFrom());
			br.sEnd = Integer.parseInt(tmp.getHspHitTo());
			br.ensembl_align = tmp.getHspQseq();
			br.pdb_align = tmp.getHspHseq();
			br.midline_align = tmp.getHspMidline();
		}
		return br;
	}

	/**
	 * parse Single blast XML results, output to SQL file incrementally
	 * 
	 * @return Success/Failure
	 */
	public boolean parseblastresultsSmallMem() {
		try {
			System.out.println("[BLAST] Read blast results from xml file...");

			File blastresults = new File(Constants.workspace + this.db.resultfileName);
			JAXBContext jc = JAXBContext.newInstance("org.cbioportal.pdb_annotation.util.blast");
			Unmarshaller u = jc.createUnmarshaller();
			u.setSchema(null);
			BlastOutput blast = (BlastOutput) u.unmarshal(blastresults);
			System.out.println("[BLAST] Generate HashMap...");

			HashMap ensemblHm = new HashMap();
			HashMap pdbHm = new HashMap();

			List<BlastResult> results = new ArrayList<BlastResult>();
			BlastOutputIterations iterations = blast.getBlastOutputIterations();
			System.out.println("[BLAST] Start parsing results...");
			int count = 0;
			for (Iteration iteration : iterations.getIteration()) {
				String querytext = iteration.getIterationQueryDef();
				IterationHits hits = iteration.getIterationHits();
				for (Hit hit : hits.getHit()) {
					results.add(parseSingleAlignment(querytext, hit, count));
					// Once get the criteria, output contents to the SQL file
					if (count % Constants.sql_insert_output_interval == 0) {
						genereateSQLstatementsSmallMem(results, ensemblHm, pdbHm, count,
								new File(Constants.workspace + Constants.sql_insert_file));
						results.clear();
						;
					}
					count++;
				}
			}
			// output remaining contents to the SQL file
			genereateSQLstatementsSmallMem(results, ensemblHm, pdbHm, count,
					new File(Constants.workspace + Constants.sql_insert_file));
			this.matches = count;
			System.out.println("[BLAST] Total Input Queries = " + this.matches);
		} catch (Exception ee) {
			System.err.println("[BLAST] Error Parsing BLAST Result");
			ee.printStackTrace();
		}
		return true;
	}

	/**
	 * parse multiple blast XML results, output to SQL file incrementally
	 * 
	 * @param filecount:
	 *            id of the multiple files
	 * @param ensemblHm
	 * @param pdbHm
	 * @return
	 */
	public boolean parseblastresultsSmallMem(int filecount, HashMap ensemblHm, HashMap pdbHm) {
		try {
			System.out.println("[BLAST] Read blast results from " + filecount + "th xml file...");
			File blastresults = new File(Constants.workspace + this.db.resultfileName + "." + filecount);
			JAXBContext jc = JAXBContext.newInstance("org.cbioportal.pdb_annotation.util.blast");
			Unmarshaller u = jc.createUnmarshaller();
			u.setSchema(null);
			BlastOutput blast = (BlastOutput) u.unmarshal(blastresults);

			List<BlastResult> results = new ArrayList<BlastResult>();
			BlastOutputIterations iterations = blast.getBlastOutputIterations();
			System.out.println("[BLAST] Start parsing results...");
			int count = 0;

			File outputfile;
			// Check whether multiple files existed
			if (this.ensembl_file_count != -1) {
				outputfile = new File(Constants.workspace + Constants.sql_insert_file + "." + filecount);
			} else {
				outputfile = new File(Constants.workspace + Constants.sql_insert_file);
			}

			for (Iteration iteration : iterations.getIteration()) {
				String querytext = iteration.getIterationQueryDef();
				IterationHits hits = iteration.getIterationHits();
				for (Hit hit : hits.getHit()) {
					results.add(parseSingleAlignment(querytext, hit, count));
					if (count % Constants.sql_insert_output_interval == 0) {
						// Once get the criteria, output contents to the SQL file
						genereateSQLstatementsSmallMem(results, ensemblHm, pdbHm, count, outputfile);
						results.clear();
					}
					count++;
				}
			}
			// output remaining contents to the SQL file
			genereateSQLstatementsSmallMem(results, ensemblHm, pdbHm, count, outputfile);
			this.matches = this.matches + count;
			System.out.println("[BLAST] Input Queries after parsing " + filecount + "th xml : " + this.matches);
		} catch (Exception ee) {
			System.err.println("[BLAST] Error Parsing BLAST Result");
			ee.printStackTrace();
		}
		return true;
	}

	/**
	 * Parse list of String blast results to input SQL statements, time and
	 * memory consuming for huge files
	 * 
	 * @param outresults
	 *            List<BlastResult>
	 * @return Success/Failure
	 */
	boolean generateSQLstatementsSingle(List<BlastResult> outresults) {
		try {
			System.out.println("[SHELL] Start Write insert.sql File...");
			FileWriter fw = new FileWriter(new File(Constants.workspace + Constants.sql_insert_file));
			StringBuffer sb = new StringBuffer();

			// HashMap ensemblHm and pdbHm are designed to avoid duplication of
			// primary keys in SQL
			// if we already have the entry, do nothing; otherwise generate the
			// SQL and add the keys into the HashMap
			HashMap ensemblHm = new HashMap();
			HashMap pdbHm = new HashMap();
			for (BlastResult br : outresults) {
				String str = "";
				if (ensemblHm.containsKey(br.getQseqid())) {
					// do nothing
				} else {
					String[] strarrayQ = br.getQseqid().split("\\s+");
					str = "INSERT INTO `ensembl_entry`(`ENSEMBL_ID`,`ENSEMBL_GENE`,`ENSEMBL_TRANSCRIPT`) VALUES('"
							+ strarrayQ[0] + "', '" + strarrayQ[3].split(":")[1] + "', '" + strarrayQ[4].split(":")[1]
							+ "');\n";
					sb.append(str);
					ensemblHm.put(br.getQseqid(), "");
				}
				if (pdbHm.containsKey(br.getSseqid())) {
					// do nothing
				} else {
					String[] strarrayS = br.getSseqid().split("_");
					str = "INSERT INTO `pdb_entry` (`PDB_NO`,`PDB_ID`,`CHAIN`) VALUES ('" + br.getSseqid() + "', '"
							+ strarrayS[0] + "', '" + strarrayS[1] + "');\n";
					sb.append(str);
					pdbHm.put(br.getSseqid(), "");
				}
				String[] strarrayQ = br.getQseqid().split("\\s+");
				String[] strarrayS = br.getSseqid().split("_");
				str = "INSERT INTO `pdb_ensembl_alignment` (`PDB_NO`,`PDB_ID`,`CHAIN`,`ENSEMBL_ID`,`PDB_FROM`,`PDB_TO`,`ENSEMBL_FROM`,`ENSEMBL_TO`,`EVALUE`,`BITSCORE`,`IDENTITY`,`IDENTP`,`ENSEMBL_ALIGN`,`PDB_ALIGN`,`MIDLINE_ALIGN`)VALUES ('"
						+ br.getSseqid() + "','" + strarrayS[0] + "','" + strarrayS[1] + "','" + strarrayQ[0] + "',"
						+ br.getqStart() + "," + br.getqEnd() + "," + br.getsStart() + "," + br.getsEnd() + ",'"
						+ br.getEvalue() + "'," + br.getBitscore() + "," + br.getIdent() + "," + br.getIdentp() + ",'"
						+ br.getEnsembl_align() + "','" + br.getPdb_align() + "','" + br.getMidline_align() + "');\n";
				sb.append(str);
			}
			fw.write(sb.toString());
			fw.close();
			System.out.println("[SHELL] Write insert.sql Done");
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return true;
	}

	/**
	 * Parse multiple list of String blast results to multiple input SQL
	 * statements
	 * 
	 * @param List<BlastResult>
	 *            results
	 * @param ensemblHm
	 * @param pdbHm
	 * @param count
	 * @param outputfile
	 * @return
	 */
	public boolean genereateSQLstatementsSmallMem(List<BlastResult> results, HashMap ensemblHm, HashMap pdbHm,
			int count, File outputfile) {
		try {
			System.out.println("[SHELL] Start Write insert.sql File from Alignment " + count + "...");
			FileUtils fu = new FileUtils();
			// check, if starts, make sure it is empty
			if (count == 0) {
				if (outputfile.exists()) {
					outputfile.delete();
				}
			}
			List<String> outputlist = new ArrayList<String>();
			for (BlastResult br : results) {
				String str = "";
				if (ensemblHm.containsKey(br.getQseqid())) {
					// do nothing
				} else {
					String[] strarrayQ = br.getQseqid().split("\\s+");
					str = "INSERT INTO `ensembl_entry`(`ENSEMBL_ID`,`ENSEMBL_GENE`,`ENSEMBL_TRANSCRIPT`) VALUES('"
							+ strarrayQ[0] + "', '" + strarrayQ[3].split(":")[1] + "', '" + strarrayQ[4].split(":")[1]
							+ "');\n";
					outputlist.add(str);
					ensemblHm.put(br.getQseqid(), "");
				}
				if (pdbHm.containsKey(br.getSseqid())) {
					// do nothing
				} else {
					String[] strarrayS = br.getSseqid().split("_");
					str = "INSERT INTO `pdb_entry` (`PDB_NO`,`PDB_ID`,`CHAIN`) VALUES ('" + br.getSseqid() + "', '"
							+ strarrayS[0] + "', '" + strarrayS[1] + "');\n";
					outputlist.add(str);
					pdbHm.put(br.getSseqid(), "");
				}

				String[] strarrayQ = br.getQseqid().split("\\s+");
				String[] strarrayS = br.getSseqid().split("_");
				str = "INSERT INTO `pdb_ensembl_alignment` (`PDB_NO`,`PDB_ID`,`CHAIN`,`ENSEMBL_ID`,`PDB_FROM`,`PDB_TO`,`ENSEMBL_FROM`,`ENSEMBL_TO`,`EVALUE`,`BITSCORE`,`IDENTITY`,`IDENTP`,`ENSEMBL_ALIGN`,`PDB_ALIGN`,`MIDLINE_ALIGN`)VALUES ('"
						+ br.getSseqid() + "','" + strarrayS[0] + "','" + strarrayS[1] + "','" + strarrayQ[0] + "',"
						+ br.getqStart() + "," + br.getqEnd() + "," + br.getsStart() + "," + br.getsEnd() + ",'"
						+ br.getEvalue() + "'," + br.getBitscore() + "," + br.getIdent() + "," + br.getIdentp() + ",'"
						+ br.getEnsembl_align() + "','" + br.getPdb_align() + "','" + br.getMidline_align() + "');\n";
				outputlist.add(str);
			}

			fu.writeLines(outputfile, outputlist, "", true);
			outputlist = null;

		} catch (Exception ex) {
			ex.printStackTrace();
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
	 * Helper Function for building the following command : blastp -db
	 * pdb_seqres.db -query Homo_sapiens.GRCh38.pep.all.fa -word_size 11 -evalue
	 * 1e-60 -num_threads 6 -outfmt 5 -out pdb_seqres.xml"
	 * 
	 * @return A List of command arguments for the processbuilder
	 */
	private List<String> makeBlastPCommand() {
		List<String> list = new ArrayList<String>();
		// Building the following process command
		list.add(Constants.blastp);
		list.add("-db");
		list.add(Constants.workspace + this.db.dbName);
		list.add("-query");
		list.add(Constants.workspace + Constants.ensembl_fasta_file);
		// list.add("-word_size");
		// list.add("11");
		list.add("-evalue");
		list.add(Constants.blast_para_evalue);
		list.add("-num_threads");
		list.add(Constants.blast_para_threads);
		list.add("-outfmt");
		list.add("5");
		list.add("-out");
		list.add(Constants.workspace + this.db.resultfileName);
		return list;
	}

	/**
	 * This is for the multiple inputs
	 * 
	 * @param i
	 * @return
	 */
	private List<String> makeBlastPCommand(int i) {
		List<String> list = new ArrayList<String>();
		// Building the following process command
		list.add(Constants.blastp);
		list.add("-db");
		list.add(Constants.workspace + this.db.dbName);
		list.add("-query");
		list.add(Constants.workspace + Constants.ensembl_fasta_file + "." + new Integer(i).toString());
		// list.add("-word_size");
		// list.add("11");
		list.add("-evalue");
		list.add(Constants.blast_para_evalue);
		list.add("-num_threads");
		list.add(Constants.blast_para_threads);
		list.add("-outfmt");
		list.add("5");
		list.add("-out");
		list.add(Constants.workspace + this.db.resultfileName + "." + new Integer(i).toString());
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
	 * parse XML blast results to INSERT SQL file
	 * 
	 * @param choose
	 *            0 for mem efficiency, 1 for disk efficiency
	 * @return
	 */
	public boolean parse2sql(int choose) {
		switch (choose) {
		case 0:
			// multiple input, multiple sql generated incrementally
			if (this.ensembl_file_count == -1) {
				parseblastresultsSmallMem();
			} else {
				HashMap ensemblHm = new HashMap();
				HashMap pdbHm = new HashMap();
				for (int i = 0; i < this.ensembl_file_count; i++) {
					parseblastresultsSmallMem(i, ensemblHm, pdbHm);
				}
			}
			break;
		case 1:
			// test for small datasets: single input, single sql generated in
			// one time
			List<BlastResult> outresults = parseblastresultsSingle();
			for (int i = 0; i <= outresults.size(); i++) {
				System.out.println(outresults.get(i).toString());
			}
			generateSQLstatementsSingle(outresults);
			break;

		default:
			break;
		}
		return true;
	}

	/**
	 * main function, run the commands
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		PdbAnnotationScriptsPipelineStarter app = new PdbAnnotationScriptsPipelineStarter();

		long startTime = System.currentTimeMillis();

		// Step 1: choose only protein entries of all pdb
		app.preprocessPDBsequences(Constants.workspace + Constants.pdb_seqres_download_file,
				Constants.workspace + Constants.pdb_seqres_fasta_file);

		// Step 2: preprocess ensembl files, split into small files to save the
		// memory
		app.preprocessGENEsequences(Constants.workspace + Constants.ensembl_download_file,
				Constants.workspace + Constants.ensembl_fasta_file);

		// Step 3: build the database by makebalstdb
		app.run("makeblastdb");

		// Step 4: blastp ensembl genes against pdb
		// Warning: This step takes time
		app.run("blastp");

		// Step 5: parse results and output as input sql statments
		app.parse2sql(0);

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
