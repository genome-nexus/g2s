package org.cbioportal.pdb_annotation.scripts;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.cbioportal.pdb_annotation.util.CommandProcessUtil;
import org.cbioportal.pdb_annotation.util.FTPClientUtil;
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
    private int seqFileCount;
    private boolean updateTag;

    /**
     * Constructor
     */
    public PdbScriptsPipelineRunCommand() {
        this.matches = 0;
        this.seqFileCount = -1;
        this.updateTag = false;
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

    public int getSeqFileCount() {
        return seqFileCount;
    }

    public void setSeqFileCount(int seqFileCount) {
        this.seqFileCount = seqFileCount;
    }

    public boolean isUpdateTag() {
        return updateTag;
    }

    public void setUpdateTag(boolean updateTag) {
        this.updateTag = updateTag;
    }

    /**
     * main steps of init pipeline
     */
    public void runInit() {
        
        this.db = new BlastDataBase(ReadConfig.pdbSeqresFastaFile);
        PdbScriptsPipelinePreprocessing preprocess = new PdbScriptsPipelinePreprocessing();
        CommandProcessUtil cu = new CommandProcessUtil();
        ArrayList<String> paralist = new ArrayList<String>();
        
        
        // Step 1
        // Read Sequences from cloned whole PDB, need at least 24G free spaces
        // and at least 12 hours
        log.info("********************[STEP 1]********************");
        log.info("Download PDB and parse to sequences");
        log.info(
                "[Download] A cloned copy of whole PDB will be downloaded and parse to sequences, unziped and parsing to get the PDB sequences");
        PdbSequenceUtil pu = new PdbSequenceUtil();
        pu.initSequencefromFolder("/home/wangjue/gsoc/pdb_all/pdb",ReadConfig.workspace
         + ReadConfig.pdbSeqresDownloadFile);
        // pu.initSequencefromFolder("/home/wangjue/gsoc/testpdb/test",ReadConfig.workspace
        // + ReadConfig.pdbSeqresDownloadFile);
        //pu.initSequencefromAll(ReadConfig.pdbRepo, ReadConfig.workspace + ReadConfig.pdbSeqresDownloadFile);
        
        
        // Step 2:
        log.info("********************[STEP 2]********************");
        log.info(
                "[Processing] Preprocess PDB sequence and sequence files");
        // Select only PDB files of proteins, parse PDB files to sequences
        preprocess.preprocessPDBsequences(ReadConfig.workspace + ReadConfig.pdbSeqresDownloadFile,
                ReadConfig.workspace + ReadConfig.pdbSeqresFastaFile);
        
        
        log.info("********************[STEP 3]********************");
        log.info("[Download] Download and unzip Ensembl, Uniprot and Isoform");
        paralist = new ArrayList<String>();
        paralist.add(ReadConfig.ensemblWholeSource);
        paralist.add(ReadConfig.workspace
                + ReadConfig.ensemblWholeSource.substring(ReadConfig.ensemblWholeSource.lastIndexOf("/") + 1));
        cu.runCommand("wget", paralist);

        paralist = new ArrayList<String>();
        paralist.add(ReadConfig.workspace
                + ReadConfig.ensemblWholeSource.substring(ReadConfig.ensemblWholeSource.lastIndexOf("/") + 1));
        paralist.add(ReadConfig.workspace + ReadConfig.ensemblDownloadFile);
        cu.runCommand("gunzip", paralist);

        FTPClientUtil fc = new FTPClientUtil();
        fc.downloadFilefromFTP(ReadConfig.swissprotWholeSource, ReadConfig.workspace
                + ReadConfig.swissprotWholeSource.substring(ReadConfig.swissprotWholeSource.lastIndexOf("/") + 1));

        paralist = new ArrayList<String>();
        paralist.add(ReadConfig.workspace
                + ReadConfig.swissprotWholeSource.substring(ReadConfig.swissprotWholeSource.lastIndexOf("/") + 1));
        paralist.add(ReadConfig.workspace + ReadConfig.swissprotDownloadFile);
        cu.runCommand("gunzip", paralist);

        // TrTembl, it is huge and needs to be careful
        // Normally we do not encourage use this

        // paralist = new ArrayList<String>();
        // paralist.add(ReadConfig.tremblWholeSource);
        // paralist.add(ReadConfig.workspace +
        // ReadConfig.tremblWholeSource.substring(ReadConfig.tremblWholeSource.lastIndexOf("/")
        // + 1));
        // cu.runCommand("wgetftp", paralist);

        // paralist = new ArrayList<String>();
        // paralist.add(ReadConfig.workspace +
        // ReadConfig.tremblWholeSource.substring(ReadConfig.tremblWholeSource.lastIndexOf("/")
        // + 1));
        // paralist.add(ReadConfig.workspace + ReadConfig.tremblDownloadFile);
        // cu.runCommand("gunzip", paralist);

        fc.downloadFilefromFTP(ReadConfig.isoformWholeSource, ReadConfig.workspace
                + ReadConfig.isoformWholeSource.substring(ReadConfig.isoformWholeSource.lastIndexOf("/") + 1));

        paralist = new ArrayList<String>();
        paralist.add(ReadConfig.workspace
                + ReadConfig.isoformWholeSource.substring(ReadConfig.isoformWholeSource.lastIndexOf("/") + 1));
        paralist.add(ReadConfig.workspace + ReadConfig.isoformDownloadFile);
        cu.runCommand("gunzip", paralist);
        
        
        // Step 4:
        log.info("********************[STEP 4]********************");
        log.info(
                "[Processing] Incorprate ensembl, swissprot, trembl and isoform togethe");        
        // This step takes memory, then split into small files to save the running memory
        HashMap<String, String> uniqSeqHm = new HashMap<String, String>();
        uniqSeqHm = preprocess.preprocessUniqSeq(ReadConfig.workspace + ReadConfig.ensemblDownloadFile, uniqSeqHm);
        uniqSeqHm = preprocess.preprocessUniqSeq(ReadConfig.workspace + ReadConfig.swissprotDownloadFile, uniqSeqHm);
        // uniqSeqHm = preprocess.preprocessUniqSeq(ReadConfig.workspace +
        // ReadConfig.tremblDownloadFile,uniqSeqHm);
        uniqSeqHm = preprocess.preprocessUniqSeq(ReadConfig.workspace + ReadConfig.isoformDownloadFile, uniqSeqHm);

        this.seqFileCount = preprocess.preprocessGENEsequences(uniqSeqHm,
                ReadConfig.workspace + ReadConfig.seqFastaFile);

        
        // Step 5:
        log.info("********************[STEP 5]********************");
        log.info("[PrepareBlast] Build the database by makeblastdb");
        paralist = new ArrayList<String>();
        paralist.add(ReadConfig.workspace + ReadConfig.pdbSeqresFastaFile);
        paralist.add(ReadConfig.workspace + this.db.dbName);
        cu.runCommand("makeblastdb", paralist);

        
        // Step 6:
        log.info("********************[STEP 6]********************");
        log.info("[Blast] blastp ensembl genes against pdb (Warning: This step takes time)");
        if (this.seqFileCount != -1) {
            for (int i = 0; i < this.seqFileCount; i++) {
                paralist = new ArrayList<String>();
                paralist.add(ReadConfig.workspace + ReadConfig.seqFastaFile + "." + new Integer(i).toString());
                paralist.add(ReadConfig.workspace + this.db.resultfileName + "." + new Integer(i).toString());
                paralist.add(ReadConfig.workspace + this.db.dbName);
                cu.runCommand("blastp", paralist);
            }
        } else {
            paralist = new ArrayList<String>();
            paralist.add(ReadConfig.workspace + ReadConfig.seqFastaFile);
            paralist.add(ReadConfig.workspace + this.db.resultfileName);
            paralist.add(ReadConfig.workspace + this.db.dbName);
            cu.runCommand("blastp", paralist);
        }
        
        PdbScriptsPipelineMakeSQL parseprocess = new PdbScriptsPipelineMakeSQL(this);
        
        
        // Step 7:
        log.info("********************[STEP 7]********************");
        log.info("[PrepareSQL] Parse results and output as input sql statments");
        parseprocess.parse2sql(false, ReadConfig.workspace, this.seqFileCount);

        
        // Step 8:
        log.info("********************[STEP 8]********************");
        log.info("[SQL] Create data schema");
        paralist = new ArrayList<String>();
        paralist.add(ReadConfig.resourceDir + ReadConfig.dbNameScript);
        cu.runCommand("mysql", paralist);
        

        // Step 9:
        log.info("********************[STEP 9]********************");
        log.info("[SQL] Import gene sequence SQL statements into the database");
        paralist = new ArrayList<String>();
        paralist.add(ReadConfig.workspace + ReadConfig.insertSequenceSQL);
        cu.runCommand("mysql", paralist);
        

        // Step 10:
        log.info("********************[STEP 10]********************");
        log.info("[SQL] Import INSERT SQL statements into the database (Warning: This step takes time)");
        if (this.seqFileCount != -1) {
            for (int i = 0; i < this.seqFileCount; i++) {
                paralist = new ArrayList<String>();
                paralist.add(ReadConfig.workspace + ReadConfig.sqlInsertFile + "." + new Integer(i).toString());
                cu.runCommand("mysql", paralist);
            }
        } else {
            paralist = new ArrayList<String>();
            paralist.add(ReadConfig.workspace + ReadConfig.sqlInsertFile);
            cu.runCommand("mysql", paralist);
        }

        // Step 11:
        log.info("********************[STEP 11]********************");
        log.info("[FileSystem] Clean Up");
        /*
         * if(ReadConfig.saveSpaceTag.equals("true")){ log.info(
         * "[PIPELINE] Start cleaning up in filesystem"); paralist = new
         * ArrayList<String>();
         * paralist.add(ReadConfig.workspace+ReadConfig.sqlEnsemblSQL);
         * cu.runCommand("gzip", paralist);
         * 
         * if (this.ensemblFileCount != -1) { for (int i = 0; i <
         * this.ensemblFileCount; i++) { paralist = new ArrayList<String>();
         * paralist.add(ReadConfig.workspace + ReadConfig.sqlInsertFile + "." +
         * new Integer(i).toString()); cu.runCommand("gzip", paralist); paralist
         * = new ArrayList<String>(); paralist.add(ReadConfig.workspace +
         * this.db.resultfileName + "." + new Integer(i).toString());
         * cu.runCommand("rm", paralist); } } else { paralist = new
         * ArrayList<String>(); paralist.add(ReadConfig.workspace +
         * ReadConfig.sqlInsertFile); cu.runCommand("gzip", paralist); paralist
         * = new ArrayList<String>(); paralist.add(ReadConfig.workspace +
         * this.db.resultfileName ); cu.runCommand("rm", paralist); }
         * 
         * paralist = new ArrayList<String>(); paralist.add(ReadConfig.pdbRepo);
         * cu.runCommand("rm", paralist); }
         */

    }

    /**
     * main steps of update pipeline
     */
    public void runUpdatePDB() {
        CommandProcessUtil cu = new CommandProcessUtil();
        this.db = new BlastDataBase(ReadConfig.pdbSeqresDownloadFile);
        this.setUpdateTag(true);
        PdbScriptsPipelinePreprocessing preprocess = new PdbScriptsPipelinePreprocessing();
        PdbScriptsPipelineMakeSQL parseprocess = new PdbScriptsPipelineMakeSQL(this);
        this.seqFileCount = Integer.parseInt(ReadConfig.updateSeqFastaFileNum);
        
        

        // Step 1: Set dateVersion of updating and create a folder as YYYYMMDD
        // under the main folder
        String dateVersion = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        String currentDir = ReadConfig.workspace + dateVersion + "/";

        
        // Step 2: Download and prepare new, obsolete and modified PDB in weekly
        // update from PDB
        List<String> listOld = preprocess.prepareUpdatePDBFile(currentDir, ReadConfig.pdbSeqresDownloadFile,
                ReadConfig.delPDB);

        /*
        preprocess.preprocessPDBsequencesUpdate(currentDir + ReadConfig.pdbSeqresDownloadFile,
                currentDir + ReadConfig.pdbSeqresFastaFile);

        // Step 3: Create new blast alignments in new and modified PDB
        ArrayList<String> paralist = new ArrayList<String>();
        paralist.add(currentDir + ReadConfig.pdbSeqresFastaFile);
        paralist.add(currentDir + this.db.dbName);
        cu.runCommand("makeblastdb", paralist);

        // Step 4: blastp ensembl genes against pdb; Use splited FASTA results 
        if (this.seqFileCount != -1) {
            for (int i = 0; i < this.seqFileCount; i++) {
                paralist = new ArrayList<String>();
                paralist.add(ReadConfig.workspace + ReadConfig.seqFastaFile + "." + new Integer(i).toString());
                paralist.add(currentDir + this.db.resultfileName + "." + new Integer(i).toString());
                paralist.add(currentDir + this.db.dbName);
                cu.runCommand("blastp", paralist);
            }
        } else {
            paralist = new ArrayList<String>();
            paralist.add(ReadConfig.workspace + ReadConfig.seqFastaFile);
            paralist.add(currentDir + this.db.resultfileName);
            paralist.add(currentDir + this.db.dbName);
            cu.runCommand("blastp", paralist);
        }

        // Step 4: Obsolete: blastp ensembl genes against pdb; Use one input, drawback is too huge xml results
        // The problem is too huge results for one blast results file
        // paralist = new ArrayList<String>();
        // paralist.add(ReadConfig.workspace + ReadConfig.seqFastaFile);
        // paralist.add(currentDir + this.db.resultfileName);
        // paralist.add(currentDir + this.db.dbName);
        // cu.runCommand("blastp", paralist);

        // Step 5: Insert delete SQL of obsolete and modified alignments
        parseprocess.generateDeleteSql(currentDir, listOld);
        paralist = new ArrayList<String>();
        paralist.add(currentDir + ReadConfig.sqlDeleteFile);
        cu.runCommand("mysql", paralist);

        
        // Step 6: Create and insert SQL statements of new and modified alignments; Use splited FASTA results
        parseprocess.parse2sql(false, currentDir, this.seqFileCount);
        */
        
        ArrayList<String> paralist = new ArrayList<String>();
        if (this.seqFileCount != -1) {
            for (int i = 0; i < this.seqFileCount; i++) {
                paralist = new ArrayList<String>();
                paralist.add(currentDir + ReadConfig.sqlInsertFile + "." + new Integer(i).toString());
                cu.runCommand("mysql", paralist);
            }
        } else {
            paralist = new ArrayList<String>();
            paralist.add(currentDir + ReadConfig.sqlInsertFile);
            cu.runCommand("mysql", paralist);
        }

        // Step 6: Obsolete: Create and insert SQL statements of new and
        // modified alignments; Use one input, drawback is too huge xml results
        // The problem is too huge results for one blast results file
        // parseprocess.parse2sql(true, currentDir);
        // paralist = new ArrayList<String>();
        // paralist.add(currentDir + ReadConfig.sqlInsertFile);
        // cu.runCommand("mysql", paralist);

        // Step 7: After update all the new alignments,
        // Create complete PDB sequences for de novo sequence blast
        preprocess.denovoPreprocessPDBsequencesUpdate(dateVersion, listOld, currentDir + ReadConfig.pdbSeqresFastaFile,
                currentDir + ReadConfig.pdbSeqresFastaFile);

        paralist = new ArrayList<String>();
        paralist.add(ReadConfig.workspace + ReadConfig.pdbSeqresFastaFile);
        paralist.add(ReadConfig.workspace + this.db.dbName);
        cu.runCommand("makeblastdb", paralist);
        
        
        // Step 8: Create release tags
        // Change messages.properties in web module
        paralist = new ArrayList<String>();
        paralist.add(ReadConfig.resourceDir + ReadConfig.releaseTag);
        paralist.add(currentDir + ReadConfig.releaseTagResult);
        cu.runCommand("releaseTag", paralist);
        
        preprocess.releasTagUpdate(currentDir + ReadConfig.releaseTagResult, ReadConfig.updateWebProperties);

        
        // Step 9: Clean up

        if (ReadConfig.saveSpaceTag.equals("true")) {
            log.info("[PIPELINE] Start cleaning up in filesystem");

            if (this.seqFileCount != -1) {

                for (int i = 0; i < this.seqFileCount; i++) {
                    paralist = new ArrayList<String>();
                    paralist.add(currentDir + this.db.resultfileName + "." + new Integer(i).toString());
                    cu.runCommand("rm", paralist);
                }

            }

            if (this.seqFileCount != -1) {
                for (int i = 0; i < this.seqFileCount; i++) {
                    paralist = new ArrayList<String>();
                    paralist.add(currentDir + ReadConfig.sqlInsertFile + "." + new Integer(i).toString());
                    cu.runCommand("gzip", paralist);
                }

            } else {
                paralist = new ArrayList<String>();
                paralist.add(currentDir + ReadConfig.sqlInsertFile);
                cu.runCommand("gzip", paralist);

            }

            paralist = new ArrayList<String>();
            paralist.add(currentDir + ReadConfig.sqlDeleteFile);
            cu.runCommand("gzip", paralist);

            paralist = new ArrayList<String>();
            paralist.add(currentDir + this.db.dbphr);
            cu.runCommand("rm", paralist);

            paralist = new ArrayList<String>();
            paralist.add(currentDir + this.db.dbpin);
            cu.runCommand("rm", paralist);

            paralist = new ArrayList<String>();
            paralist.add(currentDir + this.db.dbpsq);
            cu.runCommand("rm", paralist);

            paralist = new ArrayList<String>();
            paralist.add(currentDir + this.db.resultfileName);
            cu.runCommand("rm", paralist);

            // raw, delete one, zip one
            paralist = new ArrayList<String>();
            paralist.add(currentDir + ReadConfig.pdbSeqresDownloadFile);
            cu.runCommand("rm", paralist);

            paralist = new ArrayList<String>();
            paralist.add(currentDir + ReadConfig.pdbSeqresFastaFile);
            cu.runCommand("gzip", paralist);
        }
        

    }
}
