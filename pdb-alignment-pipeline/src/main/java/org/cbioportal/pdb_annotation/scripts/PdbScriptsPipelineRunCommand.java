package org.cbioportal.pdb_annotation.scripts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;
import org.cbioportal.pdb_annotation.util.CommandProcessUtil;
import org.cbioportal.pdb_annotation.util.PdbSequenceUtil;
import org.cbioportal.pdb_annotation.util.ReadConfig;
import org.cbioportal.pdb_annotation.util.blast.BlastDataBase;

/**
 * Shell-based command running
 *
 * @author Juexin Wang
 *
 */

public class PdbScriptsPipelineRunCommand {
	final static Logger log = Logger.getLogger(PdbScriptsPipelineRunCommand.class);
    private BlastDataBase db;
    private int matches;
    private int ensemblFileCount;

    /**
     * Constructor
     */
    public PdbScriptsPipelineRunCommand() {
        this.matches = 0;
        this.ensemblFileCount = -1;
    }
    
    

    public BlastDataBase getDb() {
		return db;
	}



	public void setDb(BlastDataBase db) {
		this.db = db;
	}



	public int getMatches() {
		return matches;
	}



	public void setMatches(int matches) {
		this.matches = matches;
	}



	public int getEnsemblFileCount() {
		return ensemblFileCount;
	}



	public void setEnsemblFileCount(int ensemblFileCount) {
		this.ensemblFileCount = ensemblFileCount;
	}



	/**
     * main steps of init pipeline
     */
    public void runInit() {   	
    	
        this.db = new BlastDataBase(ReadConfig.pdbSeqresFastaFile);              
        PdbScriptsPipelinePreprocessing preprocess = new PdbScriptsPipelinePreprocessing(); 
        CommandProcessUtil cu = new CommandProcessUtil();
        ArrayList<String> paralist = new ArrayList<String>();
        
        // Step 1: Download essential PDB and Ensembl
        // Two strategies were defined here, users could choose one of them from setting usePdbSeqLocalTag in Application.properties
        // usePdbSeqLocalTag is "true": Read Sequences from cloned whole PDB, need at least 22G free spaces and at least 12 hours, accurate
        // usePdbSeqLocalTag is not "true": Read Sequences from PDB precaculated file, efficient
        if(ReadConfig.usePdbSeqLocalTag.equals("true")){
        	log.info("[PDB] usePdbSeqLocalTag is set as true, a cloned copy of whole PDB will be downloaded, unziped and parsing to get the PDB sequences");
        	PdbSequenceUtil pu = new PdbSequenceUtil();		
        	pu.initSequencefromAll(ReadConfig.pdbRepo,ReadConfig.workspace + ReadConfig.pdbSeqresDownloadFile);
        }else{
        	log.info("[PDB] usePdbSeqLocalTag is not set as true, the PDB sequence is directly downloaded ");
        	paralist = new ArrayList<String>();
            paralist.add(ReadConfig.pdbWholeSource);
            paralist.add(ReadConfig.workspace + ReadConfig.pdbWholeSource.substring(ReadConfig.pdbWholeSource.lastIndexOf("/") + 1));
        	cu.runCommand("wget", paralist);
        	
        	paralist = new ArrayList<String>();
            paralist.add(ReadConfig.workspace + ReadConfig.pdbWholeSource.substring(ReadConfig.pdbWholeSource.lastIndexOf("/") + 1));
            paralist.add(ReadConfig.workspace + ReadConfig.pdbSeqresDownloadFile);
        	cu.runCommand("gunzip", paralist);           
        }
        paralist = new ArrayList<String>();
        paralist.add(ReadConfig.ensemblWholeSource);
        paralist.add(ReadConfig.workspace + ReadConfig.ensemblWholeSource.substring(ReadConfig.ensemblWholeSource.lastIndexOf("/") + 1));
        cu.runCommand("wget", paralist);
        
        paralist = new ArrayList<String>();
        paralist.add(ReadConfig.workspace + ReadConfig.ensemblWholeSource.substring(ReadConfig.ensemblWholeSource.lastIndexOf("/") + 1));
        paralist.add(ReadConfig.workspace + ReadConfig.ensemblDownloadFile);
        cu.runCommand("gunzip", paralist);       
        
        // Step 2: choose only protein entries of all pdb
        preprocess.preprocessPDBsequences(ReadConfig.workspace + ReadConfig.pdbSeqresDownloadFile, ReadConfig.workspace + ReadConfig.pdbSeqresFastaFile);
       
        // Step 3: preprocess ensembl files, split into small files to save the running memory
        this.ensemblFileCount = preprocess.preprocessGENEsequences(ReadConfig.workspace + ReadConfig.ensemblDownloadFile, ReadConfig.workspace + ReadConfig.ensemblFastaFile);
        
        // Step 4: build the database by makeblastdb
        paralist = new ArrayList<String>();
        paralist.add(ReadConfig.workspace + ReadConfig.pdbSeqresFastaFile);
        paralist.add(ReadConfig.workspace + this.db.dbName);       
        cu.runCommand("makeblastdb", paralist);
        
        // Step 5: blastp ensembl genes against pdb (Warning: This step takes time)
        if (this.ensemblFileCount != -1) {
            for (int i = 0; i < this.ensemblFileCount; i++) {
            	paralist = new ArrayList<String>();
            	paralist.add(ReadConfig.workspace + ReadConfig.ensemblFastaFile + "." + new Integer(i).toString());
            	paralist.add(ReadConfig.workspace + this.db.resultfileName + "." + new Integer(i).toString());
            	paralist.add(ReadConfig.workspace + this.db.dbName);
                cu.runCommand("blastp", paralist);
            }
        } else {
        	paralist = new ArrayList<String>();
        	paralist.add(ReadConfig.workspace + ReadConfig.ensemblFastaFile);
        	paralist.add(ReadConfig.workspace + this.db.resultfileName);
        	paralist.add(ReadConfig.workspace + this.db.dbName);
            cu.runCommand("blastp", paralist);           
        }
        PdbScriptsPipelineMakeSQL parseprocess = new PdbScriptsPipelineMakeSQL(this);
        
        // Step 6: parse results and output as input sql statments
        parseprocess.parse2sql(false, ReadConfig.workspace);
        
        // Step 7: create data schema
        paralist = new ArrayList<String>();
        paralist.add(ReadConfig.resourceDir + ReadConfig.dbNameScript);      
        cu.runCommand("mysql", paralist);

        // Step 8: import ensembl SQL statements into the database
        paralist = new ArrayList<String>();
        paralist.add(ReadConfig.workspace + ReadConfig.sqlEnsemblSQL);      
        cu.runCommand("mysql", paralist);
        
        // Step 9: import INSERT SQL statements into the database (Warning: This step takes time)
        if (this.ensemblFileCount != -1) {
            for (int i = 0; i < this.ensemblFileCount; i++) {
            	paralist = new ArrayList<String>();
                paralist.add(ReadConfig.workspace + ReadConfig.sqlInsertFile + "." + new Integer(i).toString()); 
            	cu.runCommand("mysql", paralist);
            }
        } else {
        	paralist = new ArrayList<String>();
            paralist.add(ReadConfig.workspace + ReadConfig.sqlInsertFile); 
        	cu.runCommand("mysql", paralist);
        }
        
        // Step 10: Clean up
        if(ReadConfig.saveSpaceTag.equals("true")){
        	paralist = new ArrayList<String>();
            paralist.add(ReadConfig.workspace+ReadConfig.sqlEnsemblSQL);
    		cu.runCommand("gzip", paralist);
    		
    		if (this.ensemblFileCount != -1) {
                for (int i = 0; i < this.ensemblFileCount; i++) {
                	paralist = new ArrayList<String>();
                    paralist.add(ReadConfig.workspace + ReadConfig.sqlInsertFile + "." + new Integer(i).toString()); 
                	cu.runCommand("gzip", paralist);
                	paralist = new ArrayList<String>();
                	paralist.add(ReadConfig.workspace + this.db.resultfileName + "." + new Integer(i).toString());
                	cu.runCommand("rm", paralist);
                }
            } else {
            	paralist = new ArrayList<String>();
                paralist.add(ReadConfig.workspace + ReadConfig.sqlInsertFile); 
            	cu.runCommand("gzip", paralist);
            	paralist = new ArrayList<String>();
            	paralist.add(ReadConfig.workspace + this.db.resultfileName );
            	cu.runCommand("rm", paralist);
            }
    		
    		/*
    		paralist = new ArrayList<String>();
        	paralist.add(ReadConfig.pdbRepo);
        	cu.runCommand("rm", paralist);
    		*/
        }
        
    }

    /**
     * main steps of update pipeline
     */
    public void runUpdatePDB() {
    	CommandProcessUtil cu = new CommandProcessUtil();
    	this.db = new BlastDataBase(ReadConfig.pdbSeqresDownloadFile);
    	 PdbScriptsPipelinePreprocessing preprocess = new PdbScriptsPipelinePreprocessing();
    	 PdbScriptsPipelineMakeSQL parseprocess = new PdbScriptsPipelineMakeSQL(this);
    	
    	// Step 1: Set dateVersion of updating and create a folder as YYYYMMDD under the main folder
        String dateVersion = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());        
        String currentDir = ReadConfig.workspace + dateVersion + "/";
        
        // Step 2: Download and prepare new, obsolete and modified PDB in weekly update from PDB
        List<String> listOld = preprocess.prepareUpdatePDBFile(currentDir, ReadConfig.pdbSeqresDownloadFile, ReadConfig.delPDB);
        preprocess.preprocessPDBsequencesUpdate(currentDir + ReadConfig.pdbSeqresDownloadFile, currentDir + ReadConfig.pdbSeqresFastaFile);      
        
        // Step 3: Create new blast alignments in new and modified PDB
        ArrayList<String> paralist = new ArrayList<String>();       
        paralist.add(currentDir + ReadConfig.pdbSeqresFastaFile);
        paralist.add(currentDir + this.db.dbName);       
        cu.runCommand("makeblastdb", paralist);
        
        // Step 4: blastp ensembl genes against pdb
        /*
        if (this.ensemblFileCount != -1) {
            for (int i = 0; i < this.ensemblFileCount; i++) {
            	paralist = new ArrayList<String>();
            	paralist.add(currentDir + ReadConfig.ensemblFastaFile + "." + new Integer(i).toString());
            	paralist.add(currentDir + this.db.resultfileName + "." + new Integer(i).toString());
            	paralist.add(currentDir);
                cu.runCommand("blastp", paralist);
            }
        } else {
        */
        	paralist = new ArrayList<String>();
        	paralist.add(ReadConfig.workspace + ReadConfig.ensemblFastaFile);
        	paralist.add(currentDir + this.db.resultfileName);
        	paralist.add(currentDir + this.db.dbName);
            cu.runCommand("blastp", paralist);           
        
        
        // Step 5: Insert delete SQL of obsolete and modified alignments
        parseprocess.generateDeleteSql(currentDir, listOld);       
        paralist = new ArrayList<String>();
        paralist.add(currentDir + ReadConfig.sqlDeleteFile);      
        cu.runCommand("mysql", paralist);
        
        // Step 6: Create and insert SQL statements of new and modified alignments       
        parseprocess.parse2sql(true, currentDir);       
        paralist = new ArrayList<String>();
        paralist.add(currentDir + ReadConfig.sqlInsertFile);      
        cu.runCommand("mysql", paralist);
        
        // Step 7: Clean up
        if(ReadConfig.saveSpaceTag.equals("true")){
        	paralist = new ArrayList<String>();
            paralist.add(currentDir+ReadConfig.sqlInsertFile);
    		cu.runCommand("gzip", paralist);
    		    		
            paralist = new ArrayList<String>();
            paralist.add(currentDir + ReadConfig.sqlDeleteFile); 
            cu.runCommand("gzip", paralist);
            	
            paralist = new ArrayList<String>();	
            paralist.add(currentDir + this.db.resultfileName );
            cu.runCommand("rm", paralist);
    		
        }
 
    }
}
