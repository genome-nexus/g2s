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
    public static String blastParaThreads;
    public static String gnApiEnsemblUrl;
    public static String gnApiGnUrl;

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
            ReadConfig.blastp = prop.getProperty("blastp").trim();
            ReadConfig.workspace = prop.getProperty("workspace").trim();
            ReadConfig.uploaddir = prop.getProperty("uploaddir").trim();
            ReadConfig.pdbSeqresFastaFile = prop.getProperty("pdb_seqres_fasta_file").trim();
            ReadConfig.blastParaThreads = prop.getProperty("blast_para_threads").trim();
            ReadConfig.gnApiEnsemblUrl = prop.getProperty("gn.api.ensembl.url").trim();
            ReadConfig.gnApiGnUrl = prop.getProperty("gn.api.genomenexus.url").trim();

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

    public static String getBlastParaThreads() {
        return blastParaThreads;
    }

    public static void setBlastParaThreads(String blastParaThreads) {
        ReadConfig.blastParaThreads = blastParaThreads;
    }
    
    public static String getGnApiEnsemblUrl() {
        return gnApiEnsemblUrl;
    }

    public static void setGnApiEnsemblUrl(String gnApiEnsemblUrl) {
        ReadConfig.gnApiEnsemblUrl = gnApiEnsemblUrl;
    }

    public static String getGnApiGnUrl() {
        return gnApiGnUrl;
    }

    public static void setGnApiGnUrl(String gnApiGnUrl) {
        ReadConfig.gnApiGnUrl = gnApiGnUrl;
    }

    
}
