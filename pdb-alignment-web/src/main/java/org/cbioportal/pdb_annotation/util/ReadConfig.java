package org.cbioportal.pdb_annotation.util;

import java.util.*;
import org.apache.log4j.Logger;


/**
 *
 * Read application.properties by singleton design pattern
 *
 * @author Juexin Wang
 *
 */
public class ReadConfig {
    static final Logger log = Logger.getLogger(ReadConfig.class);

    private static ReadConfig rcObj;

    public static String blastp;
    public static String workspace;
    public static String uploaddir;
    public static String pdbSeqresFastaFile;
    public static String blastParaEvalue;
    public static String blastParaWordSize;
    public static String blastParaThreads;

    public static boolean isPositiveInteger(String str) {
        return str.matches("\\d+"); // match a number with positive integer.
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?"); // match a number with optional
                                                // '-' and decimal.
    }

    public static boolean isFolder(String str) {
        return str.matches("/.+/"); // match a folder start with / and end with
                                    // /
    }

    // TODO:
    // Check value of application.properties
    public void checkValue(String inStr) {

    }

    private ReadConfig() {

        try {
            Properties prop = new Properties();
            prop.load(CommandProcessUtil.class.getClassLoader().getResourceAsStream("application.properties"));

            // Set all constants
            ReadConfig.blastp = prop.getProperty("blastp");
            ReadConfig.workspace = prop.getProperty("workspace");
            ReadConfig.uploaddir = prop.getProperty("uploaddir");
            ReadConfig.pdbSeqresFastaFile = prop.getProperty("pdb_seqres_fasta_file");
            ReadConfig.blastParaEvalue = prop.getProperty("blast_para_evalue");
            ReadConfig.blastParaWordSize = prop.getProperty("blast_para_word_size");
            ReadConfig.blastParaThreads = prop.getProperty("blast_para_threads");

        } catch (Exception ex) {
            log.error("[CONFIG] Error in Reading application.properties");
            ex.printStackTrace();
        }
    }

    /**
     * Get and set Methods
     */
    
    
    public static ReadConfig getInstance() {
        if (rcObj == null) {
            rcObj = new ReadConfig();
        }
        return rcObj;
    }

    public static ReadConfig getRcObj() {
        return rcObj;
    }

    public static String getBlastp() {
        return blastp;
    }

    public static void setBlastp(String blastp) {
        ReadConfig.blastp = blastp;
    }

    public static String getWorkspace() {
        return workspace;
    }

    public static void setWorkspace(String workspace) {
        ReadConfig.workspace = workspace;
    }

    public static String getUploaddir() {
        return uploaddir;
    }

    public static void setUploaddir(String uploaddir) {
        ReadConfig.uploaddir = uploaddir;
    }

    public static String getPdbSeqresFastaFile() {
        return pdbSeqresFastaFile;
    }

    public static void setPdbSeqresFastaFile(String pdbSeqresFastaFile) {
        ReadConfig.pdbSeqresFastaFile = pdbSeqresFastaFile;
    }

    public static String getBlastParaEvalue() {
        return blastParaEvalue;
    }

    public static void setBlastParaEvalue(String blastParaEvalue) {
        ReadConfig.blastParaEvalue = blastParaEvalue;
    }

    public static String getBlastParaWordSize() {
        return blastParaWordSize;
    }

    public static void setBlastParaWordSize(String blastParaWordSize) {
        ReadConfig.blastParaWordSize = blastParaWordSize;
    }

    public static String getBlastParaThreads() {
        return blastParaThreads;
    }

    public static void setBlastParaThreads(String blastParaThreads) {
        ReadConfig.blastParaThreads = blastParaThreads;
    }

}
