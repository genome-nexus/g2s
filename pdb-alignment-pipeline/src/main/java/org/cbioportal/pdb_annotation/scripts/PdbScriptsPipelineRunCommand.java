package org.cbioportal.pdb_annotation.scripts;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.cbioportal.pdb_annotation.util.FTPClientUtil;
import org.cbioportal.pdb_annotation.util.ReadConfig;
import org.cbioportal.pdb_annotation.util.blast.BlastDataBase;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

/**
 * Shell-based command running
 *
 * @author wangjue
 *
 */
@Service
@Component
@EnableConfigurationProperties
@PropertySource("classpath:application.properties")
public class PdbScriptsPipelineRunCommand {
    BlastDataBase db;
    int matches;
    int ensemblFileCount;
    private String dataVersion;

    /**
     * Constructor
     */
    public PdbScriptsPipelineRunCommand() {
        this.matches = 0;
        this.ensemblFileCount = -1;
    }
    
    /**
     * output process Errors from process
     * 
     * @param process
     */
    private void outputProcessError(Process process, int shellReturnCode){
    	try{
    		if(shellReturnCode != 0){
    			InputStream error = process.getErrorStream();
        		for (int i = 0; i < error.available(); i++) {
        			System.out.println("[Process] Error: "+error.read());
        		}			
    		}   		
    	}
        catch(Exception ex){
        	ex.printStackTrace();
        }
    	
    }

    /**
     * run external command by ProcessBuilder, now it only contains
     * "makeblastdb" and "blastp", will add more in future
     *
     * @param shell command
     * @param currentDir
     * @return shellReturnCode, 0 for normal results
     */
    private int run(String command, String currentDir) {
    	int shellReturnCode = 0;
        if (command.equals("makeblastdb")) {
            try {
                System.out.println("[BLAST] Running makeblastdb command...");
                ProcessBuilder dbBuilder = new ProcessBuilder(makeBlastDBCommand(currentDir));
                Process makeDB = dbBuilder.start();
                makeDB.waitFor();
                shellReturnCode=makeDB.exitValue();
                outputProcessError(makeDB, shellReturnCode);
                System.out.println("[BLAST] Command makeblastdb complete");
            } catch (Exception ee) {
                System.err.println("[BLAST] Fatal Error: Could not Successfully Run makeblastdb command");
                ee.printStackTrace();
            }
        } else if (command.equals("blastp")) {
            try {
                System.out.println("[BLAST] Running blastp command...");
                if (this.ensemblFileCount != -1) {
                    for (int i = 0; i < this.ensemblFileCount; i++) {
                        ProcessBuilder blastp = new ProcessBuilder(makeBlastPCommand(currentDir, i));
                        Process blast_standalone = blastp.start();
                        blast_standalone.waitFor();
                        if(blast_standalone.exitValue()!=0){
                        	System.out.println("[BLAST] Running blastp went wrong on "+i+"th input!");
                        	outputProcessError(blast_standalone, shellReturnCode);
                        	shellReturnCode=blast_standalone.exitValue();
                        }
                    }
                } else {
                    ProcessBuilder blastp = new ProcessBuilder(makeBlastPCommand(currentDir));
                    Process blast_standalone = blastp.start();
                    blast_standalone.waitFor();
                    shellReturnCode=blast_standalone.exitValue();
                    outputProcessError(blast_standalone, shellReturnCode);
                }
                System.out.println("[BLAST] Command blastp complete");
            } catch (Exception ee) {
                System.err.println("[BLAST] Fatal Error: Could not Successfully Run blastp command");
                ee.printStackTrace();
            }
        } else {
            System.out.println("[Shell] Error: Could not recognize Command: " + command);
        }
        return shellReturnCode;
    }

    /**
     * run external command by ProcessBuilder, with redirect maker "<"
     * Now the command now is mysql
     *
     * @param command
     *            contents before "<"
     * @param arguments
     *            contents after  "<"
     * @param checkmultipleTag
     *            True for split capable files, false for non-split files
     * @return Success/Failure
     */
    private int runwithRedirectFrom(String command, String arguments, boolean checkmultipleTag) {
    	int shellReturnCode = 0;
        if (command.equals("mysql")) {
            try {
                System.out.println("[DATABASE] Running mysql command...");
                if (checkmultipleTag && this.ensemblFileCount != -1) {
                    for (int i = 0; i < this.ensemblFileCount; i++) {
                        System.out.println("[DATABASE] Running mysql command on " + i + "th sql ...");
                        long startTime = System.currentTimeMillis();
                        ProcessBuilder mysql = new ProcessBuilder(makeDBCommand());
                        mysql.redirectInput(ProcessBuilder.Redirect.from(new File(arguments + "." + new Integer(i).toString())));
                        Process mysql_standalone = mysql.start();
                        mysql_standalone.waitFor();
                        if(mysql_standalone.exitValue()!=0){
                        	System.out.println("[BLAST] Running mysql went wrong on "+i+"th input!");
                        	outputProcessError(mysql_standalone, shellReturnCode);
                        	shellReturnCode=mysql_standalone.exitValue();
                        }
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
                    shellReturnCode=mysql_standalone.exitValue();
                    outputProcessError(mysql_standalone, shellReturnCode);
                }
                System.out.println("[DATABASE] Command mysql complete");
            } catch (Exception ee) {
                ee.printStackTrace();
                System.err.println("[DATABASE] Fatal Error: Could not Successfully Run mysql command on " + arguments);
            }
        } else {
            System.out.println("[Shell] Error: Could not recognize Command: " + command);
        }
        return shellReturnCode;
    }

    /**
     * run external command by ProcessBuilder, with redirect maker ">"
     * Now the command is gunzip
     *
     * @param command
     *            contents before ">"
     * @param arguments
     *            contents after  ">"
     * @param checkmultipleTag
     *            True for split capable files, false for non-split files
     * @return Success/Failure
     */
    private int runwithRedirectTo(String command, String inputname, String outputname) {
        int shellReturnCode=0;
    	if (command.equals("gunzip")) {
            if (!inputname.endsWith(".gz")) {
                return 0;
            }
            try {
                ProcessBuilder builder = new ProcessBuilder(makeGunzipCommand(inputname));
                builder.redirectOutput(ProcessBuilder.Redirect.to(new File(outputname)));
                Process mysql_standalone = builder.start();
                mysql_standalone.waitFor();
                shellReturnCode=mysql_standalone.exitValue();
                outputProcessError(mysql_standalone, shellReturnCode);
            } catch(Exception ee) {
                ee.printStackTrace();
                System.err.println("[SHELL] Fatal Error: Could not Successfully Run gunzip command on " + inputname + " to " + outputname);
            }
        }
        return shellReturnCode;
    }

    /**
     * generate gunzip command
     *
     * @param inputname
     * @return
     */
    private List<String> makeGunzipCommand(String inputname) {
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
        list.add(ReadConfig.makeblastdb);
        list.add("-in");
        list.add(currentDir + ReadConfig.pdbSeqresFastaFile);
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
        List<String> list = generateBlastCommand(currentDir, "");
        return list;
    }

    /**
     * This is for the multiple inputs
     * blastp -db pdb_seqres.db -query Homo_sapiens.GRCh38.pep.all.fa.0 -word_size 11 -evalue  1e-60 -num_threads 6 -outfmt 5 -out pdb_seqres.xml.0
     *
     * @param i
     *             the ith file
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
    private List<String> generateBlastCommand(String currentDir, String countStr) {
        List<String> list = new ArrayList<String>();
        list.add(ReadConfig.blastp);
        list.add("-db");
        list.add(currentDir + this.db.dbName);
        list.add("-query");
        list.add(ReadConfig.workspace + ReadConfig.ensemblFastaFile + countStr);
        list.add("-word_size");
        list.add(ReadConfig.blastParaWordSize);
        list.add("-evalue");
        list.add(ReadConfig.blastParaEvalue);
        list.add("-num_threads");
        list.add(ReadConfig.blastParaThreads);
        list.add("-outfmt");
        list.add("5");
        list.add("-out");
        list.add(currentDir + this.db.resultfileName + countStr);
        return list;
    }

    /**
     * generate mysql command
     *
     * @return
     */
    private List<String> makeDBCommand() {
        List<String> list = new ArrayList<String>();
        list.add(ReadConfig.mysql);
        list.add("--max_allowed_packet="+ReadConfig.mysqlMaxAllowedPacket);
        list.add("-u");
        list.add(ReadConfig.username);
        list.add("--password=" + ReadConfig.password);
        list.add(ReadConfig.dbName);
        return list;
    }

    /**
     * generate wget command
     *
     * @param urlFilename
     * @param localFilename
     * @return
     */
    private List<String> makeDownloadCommand(String urlFilename, String localFilename) {
        List<String> list = new ArrayList<String>();
        list.add("wget");
        list.add("-O");
        list.add(localFilename);
        list.add(urlFilename);
        return list;
    }

    /**
     * download files from urlFilename from Internet to localFilename of the system
     *
     * @param urlFilename
     * @param localFilename
     * @return
     */
    private int downloadfile(String urlFilename, String localFilename) {
    	int shellReturnCode=0;
            try {
                System.out.println("[SHELL] Download file " + urlFilename + " ...");
                ProcessBuilder dbBuilder = new ProcessBuilder(makeDownloadCommand(urlFilename, localFilename));
                Process makeDB = dbBuilder.start();
                makeDB.waitFor();
                shellReturnCode=makeDB.exitValue();
                outputProcessError(makeDB, shellReturnCode);
                System.out.println("[SHELL] " + urlFilename + " completed");
            } catch (Exception ee) {
                System.err.println("[SHELL] Fatal Error: Could not Successfully download files");
                ee.printStackTrace();
            }
        return shellReturnCode;
    }

    /**
     * read FTP file to list
     *
     * @param urlStr
     * @return
     */
    private List<String> readFTPfile2List(String urlStr) {
        List<String> list = new ArrayList();
        try {
            URL url = new URL(urlStr);
            URLConnection con = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                list.add(inputLine);
            }
            in.close();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * read FTP file to String
     * @param urlStr
     * @return
     */
    private String readFTPfile2Str(String urlStr) {
        String str = "";
        try {
            URL url = new URL(urlStr);
            URLConnection con = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                str = str + inputLine + "\n";
            }
            in.close();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return str;
    }

    /**
     * prepare weekly updated PDB files
     *
     * @param currentDir
     * @param updateTxt
     * @param delPDB
     * @return
     */
    private List<String> prepareUpdatePDBFile(String currentDir, String updateTxt, String delPDB) {
        List<String> listOld = new ArrayList<String>();
        try {
            System.out.println("[SHELL] Weekly Update: Create deleted list");
            FileUtils.forceMkdir(new File(currentDir));
            String addFileName = currentDir + updateTxt;
            File addFastaFile = new File(addFileName);
            String delFileName = currentDir + delPDB;
            List listAdd = readFTPfile2List(ReadConfig.updateAdded);
            List listMod = readFTPfile2List(ReadConfig.updateModified);
            List listObs = readFTPfile2List(ReadConfig.updateObsolete);
            List<String> listNew = new ArrayList<String>(listAdd);
            listNew.addAll(listMod);
            listOld = new ArrayList<String>(listMod);
            listOld.addAll(listObs);
            String listNewCont = "";
            for(String pdbName:listNew) {
                listNewCont = listNewCont + readFTPfile2Str(ReadConfig.pdbFastaService + pdbName);
            }
            FileUtils.writeStringToFile(addFastaFile, listNewCont);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return listOld;
    }

    /**
     * main steps of init pipeline
     */
    public void runInit() { 
        this.db = new BlastDataBase(ReadConfig.pdbSeqresFastaFile);              
        PdbScriptsPipelinePreprocessing preprocess = new PdbScriptsPipelinePreprocessing();         
        // Step 1: Download essential PDB and Essential tools
        //FTP test
        /*
        FTPClientUtil fu = new FTPClientUtil();
        fu.downloadFromFTP("ftp.wwpdb.org", "pub/pdb/data/status/latest/added.pdb", "added.pdb");
        */
        downloadfile(ReadConfig.pdbWholeSource, ReadConfig.workspace + ReadConfig.pdbWholeSource.substring(ReadConfig.pdbWholeSource.lastIndexOf("/") + 1));
        runwithRedirectTo("gunzip", ReadConfig.workspace + ReadConfig.pdbWholeSource.substring(ReadConfig.pdbWholeSource.lastIndexOf("/") + 1), ReadConfig.workspace + ReadConfig.pdbSeqresDownloadFile);
        downloadfile(ReadConfig.ensemblWholeSource, ReadConfig.workspace + ReadConfig.ensemblWholeSource.substring(ReadConfig.ensemblWholeSource.lastIndexOf("/") + 1));
        runwithRedirectTo("gunzip", ReadConfig.workspace + ReadConfig.ensemblWholeSource.substring(ReadConfig.ensemblWholeSource.lastIndexOf("/") + 1), ReadConfig.workspace + ReadConfig.ensemblDownloadFile);       
        // Step 2: choose only protein entries of all pdb
        preprocess.preprocessPDBsequences(ReadConfig.workspace + ReadConfig.pdbSeqresDownloadFile, ReadConfig.workspace + ReadConfig.pdbSeqresFastaFile);
        // Step 3: preprocess ensembl files, split into small files to save the memory
        ensemblFileCount = preprocess.preprocessGENEsequences(ReadConfig.workspace + ReadConfig.ensemblDownloadFile, ReadConfig.workspace + ReadConfig.ensemblFastaFile);
        // Step 4: build the database by makebalstdb
        run("makeblastdb", ReadConfig.workspace);
        // Step 5: blastp ensembl genes against pdb (Warning: This step takes time)
        run("blastp", ReadConfig.workspace);
        PdbScriptsPipelineMakeSQL parseprocess = new PdbScriptsPipelineMakeSQL(this);
        // Step 6: parse results and output as input sql statments
        parseprocess.parse2sql(0, ReadConfig.workspace);
        // Step 7: create data schema
        runwithRedirectFrom("mysql", ReadConfig.resourceDir + ReadConfig.dbNameScript, false);
        // Step 8: import ensembl SQL statements into the database
        runwithRedirectFrom("mysql", ReadConfig.workspace + ReadConfig.sqlEnsemblSQL, false);
        // Step 9: import INSERT SQL statements into the database (Warning: This step takes time)
        runwithRedirectFrom("mysql", ReadConfig.workspace + ReadConfig.sqlInsertFile, true);
    }

    /**
     * main steps of update pipeline
     */
    public void runUpdatePDB() {
        String dataVersion = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        this.dataVersion = dataVersion;
        this.db = new BlastDataBase(ReadConfig.pdbSeqresDownloadFile);
        String currentDir = ReadConfig.workspace + this.dataVersion + "/";
        PdbScriptsPipelinePreprocessing preprocess = new PdbScriptsPipelinePreprocessing();
        List<String> listOld = prepareUpdatePDBFile(currentDir, ReadConfig.pdbSeqresDownloadFile, ReadConfig.delPDB);
        preprocess.preprocessPDBsequencesUpdate(currentDir + ReadConfig.pdbSeqresDownloadFile, currentDir + ReadConfig.pdbSeqresFastaFile);
        run("makeblastdb", currentDir);
        run("blastp",  currentDir);
        PdbScriptsPipelineMakeSQL parseprocess = new PdbScriptsPipelineMakeSQL(this);
        parseprocess.parse2sql(1, currentDir);
        runwithRedirectFrom("mysql", currentDir + ReadConfig.sqlInsertFile, false);
        //delete old
        parseprocess.generateDeleteSql(currentDir, listOld);
        runwithRedirectFrom("mysql", currentDir + ReadConfig.sqlDeleteFile, false);
    }
}
