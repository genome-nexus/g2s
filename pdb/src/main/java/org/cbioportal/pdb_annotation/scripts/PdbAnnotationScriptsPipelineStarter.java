package org.cbioportal.pdb_annotation.scripts;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.io.FastaReaderHelper;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.DBRef;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureIO;
import org.cbioportal.pdb_annotation.util.TextFile;
import org.cbioportal.pdb_annotation.util.Constants;

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
 * 
 * @author wangjue
 *
 */
@SpringBootApplication
public class PdbAnnotationScriptsPipelineStarter {
	
	void dealPDB(String infileName, String outfileName){
		try{	       
	       LinkedHashMap<String, ProteinSequence> a = FastaReaderHelper.readFastaProteinSequence(new File(infileName));
	       //FastaReaderHelper.readFastaDNASequence for DNA sequences
	       
	       Collection c = new ArrayList<ProteinSequence>();
	       StringBuffer sb = new StringBuffer();
	       
	       for (  Entry<String, ProteinSequence> entry : a.entrySet() ) {
	    	   String[] tmp = entry.getValue().getOriginalHeader().toString().split("\\s+");
	    	   if(tmp[1].equals("mol:protein")){
	    		   //System.out.println( entry.getValue().getOriginalHeader() + "\t" + entry.getValue().getSequenceAsString() );
	    		   c.add(entry.getValue());
	    		   sb.append(">"+entry.getValue().getOriginalHeader()+"\n"+entry.getValue().getSequenceAsString()+"\n");
	    	   }
	       }
	       
	       //standard fasta output
	       //FastaWriterHelper.writeProteinSequence(new File(outfileName), c);
	       
	       //Non-standard fasta output
	       FileWriter fw = new FileWriter(new File(outfileName));
	       fw.write(sb.toString());
	       fw.close();
	       
	       
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	
	private DataBase db;
	private String directory ="/home/wangjue/gsoc/";
	private String query;
	private String queryFileName = "Homo_sapiens.GRCh38.pep.test.fa";
	private String subjectFFN = "pdb_seqres.fasta";
	private ArrayList<Integer> index;
	private int matches;
	
	public PdbAnnotationScriptsPipelineStarter(){
		this.query = "";
		this.db = new DataBase(subjectFFN);
		this.index = new ArrayList<Integer>();
		this.matches = 0;
	}


	/**
	 * Given a collection of sequences, a set of queries are build for blasting against the subject
	 * @param queryList A linked list with each sequence to be queried
	 */
	public void setQuery(HashMap<Integer,String> queryMap)
	{	
		String queryTerms = "";
		//int count = 0;
		for( Entry<Integer, String> entry : queryMap.entrySet() )
		{
			queryTerms+="> Oligo_"+entry.getKey().toString();
			queryTerms+="\n";
			index.add(entry.getKey());
			
			// Split the string by length 80
			String [] seqSplit = entry.getValue().split("(?<=\\G.{80})");

			// Add each split string to the query term as a new line
			for (String singleLine : seqSplit){ queryTerms += singleLine + "\n";}
		}

		// Write this to file
		this.query = queryTerms;
		try {
			TextFile.write(this.directory,this.queryFileName,this.query);
		} catch (IOException e) { e.printStackTrace(); }
	}

	
	public void run(String command){
		try{
			if(command.equals("makeblastdb")){
				try{
					ProcessBuilder dbBuilder= new ProcessBuilder(makeBlastDBCommand());
					Process makeDB = dbBuilder.start(); 
					makeDB.waitFor();
				}catch (Exception ee) {
					System.err.println("[BLAST] Fatal Error: Could not Successfully Run makeblastdb command"); 
					ee.printStackTrace(); 
				}
								
			}else if(command.equals("blastp")){
				try{
					ProcessBuilder blastp = new ProcessBuilder(makeBlastPCommand());
					Process blast_standalone = blastp.start();
					blast_standalone.waitFor();					
				}
				catch (Exception ee) {
					System.err.println("[BLAST] Fatal Error: Could not Successfully Run blastp command"); 
					ee.printStackTrace(); 
				}				
			}else{
				System.out.println("[Shell] Erro: Could not recognize Command: "+command);
				
			}						
		}
		catch (Exception ee){ee.printStackTrace();}
		
	}
	
	/**
	 * Execute a Basic Local Alignment Search on the subject sequence with the query sequences 
	 * (made with the setQuery method)
	 * After calling the external process, this command will waitfor it to complete, parse and store the results
	 */
	public List<BlastResult> parseblastresults(){
		
		List<BlastResult> results = new ArrayList<BlastResult>(this.matches);
						
			try{
				String output = TextFile.read(this.directory+this.db.textFile);
				//System.out.println(output);
				results=parse(output);

			}
			catch (Exception ee) {
				System.err.println("[BLAST] Error Parsing BLAST Result");
				ee.printStackTrace();

			}
		
		return results;
	}

	/**
	 * Getter for the subject sequence that is being BLASTed against
	 * @return String with the subject sequence
	 */
	public String getSubject(){
		return this.db.name;
	}



	private List<BlastResult> parse(String output){
		String [] lines = output.split("\\n");
		ArrayList<BlastResult> results = new ArrayList<BlastResult>();
		Integer count = 0;

		for(String line: lines){

			if (line.startsWith("#")){
				if (line.contains("# Query:")) {
					count++;
					//System.out.println("count:"+count);
				}
			}
			else{
				String[] parameters = line.split("\\t");
				if (parameters.length == 12) {

					BlastResult br = new BlastResult( count-1 );
					br.qseqid = parameters[0];
					br.sseqid = parameters[1];
					br.pident = Double.parseDouble(parameters[2]);
					br.length = Integer.parseInt(parameters[3]);
					br.mismatch = Integer.parseInt(parameters[4]);
					br.gapopen = Integer.parseInt(parameters[5]);					
					br.qStart = Integer.parseInt(parameters[6]);
					br.qEnd = Integer.parseInt(parameters[7]);
					br.sStart = Integer.parseInt(parameters[8]);
					br.sEnd = Integer.parseInt(parameters[9]);
					br.evalue = Double.parseDouble(parameters[10]);
					br.bitscore = Double.parseDouble(parameters[11]);

					results.add(br);
				}
			}
		}
		this.matches = count; 
		System.out.println("[BLAST] Total Input Queries = "+this.matches);
		
		return results;
	}
	

	/**
	 * Helper Function for building the makeblastdb command:
	 * makeblastdb -in ./genome.ffn -dbtype prot  -out test.db
	 * 
	 * @return A List containing the commands to execute the makeblastdb function
	 */
	private List<String> makeBlastDBCommand(){
		List<String> list= new ArrayList<String>();

		list.add(Constants.makeblastdb);
		list.add("-in");
		list.add(this.directory+this.subjectFFN);
		list.add("-dbtype");
		list.add("prot");
		list.add("-out");
		list.add(this.directory+this.db.fileName);

		return list;
	}

	/**
	 * Helper Function for building the following command : 
	 * blastp -db ./test.db  -query ./oligo.fasta -out ./testout.txt -word_size 11 -evalue 10 -outfmt "7 ssequid ssac qstart qend sstart send qseq evalue bitscore"
	 * 
	 * @return A List of command arguments for the processbuilder
	 */
	private List<String> makeBlastPCommand(){
		List<String> list= new ArrayList<String>();

		// Building the following process command
		list.add(Constants.blastp);
		list.add("-db");
		list.add(directory+this.db.fileName);
		list.add("-query");
		list.add(directory+this.queryFileName);
		list.add("-out");
		list.add(directory+this.db.textFile);
		//list.add("-word_size");
		//list.add("11");
		list.add("-evalue");
		list.add("1e-50");
		list.add("-num_threads");
		list.add("6");
		list.add("-outfmt");
		list.add("7");
		//list.add("7 qseqid sseqid pident length mismatch gapopen qstart qend sstart send evalue bitscore");

		return list;
	}
	
	
	void generateSQLstatements(List<BlastResult> outresults, String insertsqlname){
		try{
			FileWriter fw = new FileWriter(new File(insertsqlname));
			StringBuffer sb = new StringBuffer();
			HashMap ensemblHm = new HashMap();
			HashMap pdbHm = new HashMap();
			for(BlastResult br:outresults){
				String str= "";
				if(ensemblHm.containsKey(br.getQseqid())){
					//do nothing
				}else{
					str = "INSERT INTO `ensembl_entry`(`ENSEMBL_ID`) VALUES('"+br.getQseqid()+"');\n";
					sb.append(str);
					ensemblHm.put(br.getQseqid(), "");
				}
				if(pdbHm.containsKey(br.getSseqid())){
					//do nothing
				}else{
					String[] strarray = br.getSseqid().split("_");
					str = "INSERT INTO `pdb_entry` (`PDB_NO`,`PDB_ID`,`CHAIN`) VALUES ('"+br.getSseqid()+"', '"+strarray[0]+"', '"+strarray[1]+"');\n";	
					sb.append(str);
					pdbHm.put(br.getSseqid(), "");
				}
				
				String[] strarray = br.getSseqid().split("_");
				str="INSERT INTO `pdb_ensembl_alignment` (`PDB_NO`,`PDB_ID`,`CHAIN`,`ENSEMBL_ID`,`PDB_FROM`,`PDB_TO`,`ENSEMBL_FROM`,`ENSEMBL_TO`,`EVALUE`,`BITSCORE`,`IDENTP`)VALUES ('"+br.getSseqid()+"','"+strarray[0]+"','"+strarray[1]+"','"+br.getQseqid()+"',"+br.getqStart()+","+br.getqEnd()+","+br.getsStart()+","+br.getsEnd()+",'"+br.getEvalue()+"',"+br.getBitscore()+","+br.getPident()+");\n";
				sb.append(str);
				
				//System.out.println(br.toString());	
			}
			fw.write(sb.toString());
			fw.close();
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
	
	
	
	
	private class DataBase {

		public String name;
		public String fileName;
		public String textFile;

		public DataBase(String subject) {
			this.name = subject.replaceAll("\\..*",""); 		// Remove file extension
			this.name = this.name.replaceAll(".fasta","");
			this.fileName = this.name + ".db";
			this.textFile = this.name + ".out";
		}
	}

	
	public class BlastResult{

		public Integer oligoID;
		public String qseqid;
		public String sseqid;
		public Double pident;
		public Integer length;
		public Integer mismatch;
		public Integer gapopen;
		public Integer qStart;
		public Integer qEnd;
		public Integer sStart;
		public Integer sEnd;
		public Double  evalue;
		public Double bitscore;

		public BlastResult(Integer id) { 
			this.oligoID = id;
			this.qseqid = "";
			this.sseqid = "";
			this.pident = -1.0;
			this.length=-1;
			this.mismatch=-1;
			this.gapopen=-1;
			this.qStart = -1;
			this.qEnd= -1;
			this.sStart = -1;
			this.sEnd = -1;
			this.bitscore = -1.0;
			this.evalue = -1.0;
		}
		
		
		public Integer getOligoID() {
			return oligoID;
		}


		public void setOligoID(Integer oligoID) {
			this.oligoID = oligoID;
		}


		public String getQseqid() {
			return qseqid;
		}


		public void setQseqid(String qseqid) {
			this.qseqid = qseqid;
		}


		public String getSseqid() {
			return sseqid;
		}


		public void setSseqid(String sseqid) {
			this.sseqid = sseqid;
		}


		public Double getPident() {
			return pident;
		}


		public void setPident(Double pident) {
			this.pident = pident;
		}


		public Integer getLength() {
			return length;
		}


		public void setLength(Integer length) {
			this.length = length;
		}


		public Integer getMismatch() {
			return mismatch;
		}


		public void setMismatch(Integer mismatch) {
			this.mismatch = mismatch;
		}


		public Integer getGapopen() {
			return gapopen;
		}


		public void setGapopen(Integer gapopen) {
			this.gapopen = gapopen;
		}


		public Integer getqStart() {
			return qStart;
		}


		public void setqStart(Integer qStart) {
			this.qStart = qStart;
		}


		public Integer getqEnd() {
			return qEnd;
		}


		public void setqEnd(Integer qEnd) {
			this.qEnd = qEnd;
		}


		public Integer getsStart() {
			return sStart;
		}


		public void setsStart(Integer sStart) {
			this.sStart = sStart;
		}


		public Integer getsEnd() {
			return sEnd;
		}


		public void setsEnd(Integer sEnd) {
			this.sEnd = sEnd;
		}


		public Double getEvalue() {
			return evalue;
		}


		public void setEvalue(Double evalue) {
			this.evalue = evalue;
		}


		public Double getBitscore() {
			return bitscore;
		}


		public void setBitscore(Double bitscore) {
			this.bitscore = bitscore;
		}


		@Override
		public String toString(){
			return oligoID.toString()+"\t"+qseqid+"\t"+sseqid+"\t"+pident+"\t"+length+"\t"+mismatch+"\t"+gapopen+"\t"+qStart.toString()+"\t"+
					qEnd.toString()+"\t"+sStart.toString()+"\t"+
					sEnd.toString()+"\t"+evalue.toString()+"\t"+
					bitscore.toString();
		}
		
	}
	
	
	void test(){
		try {

            Structure s = StructureIO.getStructure("4cof");
            
            for(DBRef db : s.getDBRefs()){
            	System.out.println("**");
            	System.out.println(db.getChainId());
            	System.out.println(db.getDatabase());
            	System.out.println(db.getDbAccession());
            	System.out.println(db.getDbIdCode());
            	System.out.println(db.getInsertBegin());
            	System.out.println(db.getInsertEnd());
            	System.out.println(db.getDbSeqBegin());
            	System.out.println(db.getDbSeqEnd());
            	System.out.println(db.getIdbnsBegin());
            	System.out.println(db.getIdbnsEnd());
            	System.out.println(db.getIdCode());
            	System.out.println(db.getId());
            }

            for ( Chain c : s.getChains()) {
            	
            	System.out.println(c.getChainID()+"\t"+c.getAtomLength()+"\t"+c.getAtomSequence().length()+"\t"+c.getSeqResLength());

                // only the observed residues
                System.out.println(c.getAtomSequence());
                              

                // print biological sequence
                System.out.println(c.getSeqResSequence());
            }

        } catch (Exception e) {

            e.printStackTrace();
        } 
	}
	
	
    public static void main( String[] args )
    {
        //System.out.println( "Hello World!" );
    	PdbAnnotationScriptsPipelineStarter ob = new PdbAnnotationScriptsPipelineStarter();
    	long startTime = System.currentTimeMillis();
    	//ob.test();
    	
    	
    	
    	//ob.dealPDB("/home/wangjue/gsoc/pdb_seqres.txt","/home/wangjue/gsoc/pdb_seqres.fasta");
    	
    	//ob.run("makeblastdb");
    	ob.run("blastp");
    	
    	List<BlastResult> outresults=ob.parseblastresults();
    	
    	/*
    	System.out.println("Totalhits:"+outresults.size());
    	for(int i=0;i<outresults.size();i++){
    		System.out.println(outresults.get(i).toString());	
    	}
    	*/
    	ob.generateSQLstatements(outresults, "insert.sql");
    	
    	long endTime = System.currentTimeMillis();
    	NumberFormat formatter = new DecimalFormat("#0.000");
    	System.out.print("[Shell] Execution time is " + formatter.format((endTime-startTime) / 1000d) + " seconds");
    }

}
