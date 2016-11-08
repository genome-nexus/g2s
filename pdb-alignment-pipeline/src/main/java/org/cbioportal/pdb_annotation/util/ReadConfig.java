package org.cbioportal.pdb_annotation.util;

import java.util.*;

import org.apache.log4j.Logger;
import org.cbioportal.pdb_annotation.scripts.PdbScriptsPipelineStarter;

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

    public static String makeblastdb;
    public static String blastp;
    public static String workspace;
    public static String resourceDir;
    public static String tmpdir;
    public static String pdbRepo;
    public static String pdbSegMinLengthMulti;
    public static String pdbSegMinLengthSingle;
    public static String pdbSegGapThreshold;
    public static String pdbSeqresDownloadFile;
    public static String pdbSeqresFastaFile;
    public static String ensemblDownloadFile;        
    public static String swissprotDownloadFile;
    public static String tremblDownloadFile;
    public static String isoformDownloadFile; 
    public static String seqFastaFile;
    public static String sqlInsertFile;
    public static String sqlDeleteFile;
    public static String blastParaEvalue;
    public static String blastParaWordSize;
    public static String blastParaThreads;
    public static String ensemblInputInterval;
    public static String sqlInsertOutputInterval;
    public static String mysql;
    public static String username;
    public static String password;
    public static String dbName;
    public static String dbNameScript;
    public static String pdbWholeSource;
    public static String ensemblWholeSource;    
    public static String swissprotWholeSource;
    public static String tremblWholeSource;
    public static String isoformWholeSource;    
    public static String updateTxt;
    public static String updateFasta;
    public static String delPDB;
    public static String updateDAY;
    public static String updateHOUR;
    public static String updateMINUTE;
    public static String updateSECOND;
    public static String updateMILLISECOND;
    public static String updateDELAY;
    public static String insertSequenceSQL;
    public static String updateAdded;
    public static String updateModified;
    public static String updateObsolete;
    public static String pdbFastaService;
    public static String mysqlMaxAllowedPacket;
    public static String saveSpaceTag;

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
            prop.load(PdbScriptsPipelineStarter.class.getClassLoader().getResourceAsStream("application.properties"));

            // Set all constants
            ReadConfig.makeblastdb = prop.getProperty("makeblastdb");
            ReadConfig.blastp = prop.getProperty("blastp");
            ReadConfig.workspace = prop.getProperty("workspace");
            ReadConfig.resourceDir = prop.getProperty("resource_dir");
            ReadConfig.tmpdir = prop.getProperty("tmpdir");
            ReadConfig.pdbRepo = prop.getProperty("pdbRepo");
            ReadConfig.pdbSegMinLengthMulti = prop.getProperty("pdb.seg.minLength.multi");
            ReadConfig.pdbSegMinLengthSingle = prop.getProperty("pdb.seg.minLength.single");
            ReadConfig.pdbSegGapThreshold = prop.getProperty("pdb.seg.gapThreshold");
            ReadConfig.pdbSeqresDownloadFile = prop.getProperty("pdb_seqres_download_file");
            ReadConfig.pdbSeqresFastaFile = prop.getProperty("pdb_seqres_fasta_file");
            ReadConfig.ensemblDownloadFile = prop.getProperty("ensembl_download_file");                       
            ReadConfig.swissprotDownloadFile = prop.getProperty("swissprot_download_file");
            ReadConfig.tremblDownloadFile = prop.getProperty("trembl_download_file");
            ReadConfig.isoformDownloadFile = prop.getProperty("isoform_download_file");
            ReadConfig.seqFastaFile = prop.getProperty("seq_fasta_file"); 
            ReadConfig.sqlInsertFile = prop.getProperty("sql_insert_file");
            ReadConfig.sqlDeleteFile = prop.getProperty("sql_delete_file");
            ReadConfig.blastParaEvalue = prop.getProperty("blast_para_evalue");
            ReadConfig.blastParaWordSize = prop.getProperty("blast_para_word_size");
            ReadConfig.blastParaThreads = prop.getProperty("blast_para_threads");
            ReadConfig.ensemblInputInterval = prop.getProperty("ensembl_input_interval");
            ReadConfig.sqlInsertOutputInterval = prop.getProperty("sql_insert_output_interval");
            ReadConfig.mysql = prop.getProperty("mysql");
            ReadConfig.username = prop.getProperty("username");
            ReadConfig.password = prop.getProperty("password");
            ReadConfig.dbName = prop.getProperty("db_name");
            ReadConfig.dbNameScript = prop.getProperty("db_name_script");
            ReadConfig.pdbWholeSource = prop.getProperty("pdb.wholeSource");
            ReadConfig.ensemblWholeSource = prop.getProperty("ensembl.wholeSource");            
            ReadConfig.swissprotWholeSource = prop.getProperty("swissprot.wholeSource");
            ReadConfig.tremblWholeSource = prop.getProperty("trembl.wholeSource");
            ReadConfig.isoformWholeSource = prop.getProperty("isoform.wholeSource");
            ReadConfig.updateTxt = prop.getProperty("update.updateTxt");
            ReadConfig.updateFasta = prop.getProperty("update.updateFasta");
            ReadConfig.delPDB = prop.getProperty("update.delPDB");
            ReadConfig.updateDAY = prop.getProperty("update.DAY_OF_WEEK");
            ReadConfig.updateHOUR = prop.getProperty("update.HOUR_OF_DAY");
            ReadConfig.updateMINUTE = prop.getProperty("update.MINUTE");
            ReadConfig.updateSECOND = prop.getProperty("update.SECOND");
            ReadConfig.updateMILLISECOND = prop.getProperty("update.MILLISECOND");
            ReadConfig.updateDELAY = prop.getProperty("update.DELAY");
            ReadConfig.insertSequenceSQL = prop.getProperty("insert_sequence_SQL");
            ReadConfig.updateAdded = prop.getProperty("update.added");
            ReadConfig.updateModified = prop.getProperty("update.modified");
            ReadConfig.updateObsolete = prop.getProperty("update.obsolete");
            ReadConfig.pdbFastaService = prop.getProperty("pdb.fastaService");
            ReadConfig.mysqlMaxAllowedPacket = prop.getProperty("mysql_max_allowed_packet");
            ReadConfig.saveSpaceTag = prop.getProperty("saveSpaceTag");

        } catch (Exception ex) {
            log.error("[CONFIG] Error in Reading application.properties");
            ex.printStackTrace();
        }
    }

    /**
     * Get Methods
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

    public static String getSwissprotDownloadFile() {
        return swissprotDownloadFile;
    }

    public static String getTremblDownloadFile() {
        return tremblDownloadFile;
    }

    public static String getIsoformDownloadFile() {
        return isoformDownloadFile;
    }

    public static String getSeqFastaFile() {
        return seqFastaFile;
    }

    public static String getSwissprotWholeSource() {
        return swissprotWholeSource;
    }

    public static String getTremblWholeSource() {
        return tremblWholeSource;
    }

    public static String getIsoformWholeSource() {
        return isoformWholeSource;
    }

    public static String getMakeblastdb() {
        return makeblastdb;
    }

    public static String getBlastp() {
        return blastp;
    }

    public static String getWorkspace() {
        return workspace;
    }

    public static String getResourceDir() {
        return resourceDir;
    }

    public static String getTmpdir() {
        return tmpdir;
    }

    public static String getPdbRepo() {
        return pdbRepo;
    }

    public static String getPdbSegMinLengthMulti() {
        return pdbSegMinLengthMulti;
    }

    public static void setPdbSegMinLengthMulti(String pdbSegMinLengthMulti) {
        ReadConfig.pdbSegMinLengthMulti = pdbSegMinLengthMulti;
    }

    public static String getPdbSegMinLengthSingle() {
        return pdbSegMinLengthSingle;
    }

    public static void setPdbSegMinLengthSingle(String pdbSegMinLengthSingle) {
        ReadConfig.pdbSegMinLengthSingle = pdbSegMinLengthSingle;
    }

    public static String getPdbSegGapThreshold() {
        return pdbSegGapThreshold;
    }

    public static void setPdbSegGapThreshold(String pdbSegGapThreshold) {
        ReadConfig.pdbSegGapThreshold = pdbSegGapThreshold;
    }

    public static String getPdbSeqresDownloadFile() {
        return pdbSeqresDownloadFile;
    }

    public static String getPdbSeqresFastaFile() {
        return pdbSeqresFastaFile;
    }

    public static String getEnsemblDownloadFile() {
        return ensemblDownloadFile;
    }

    public static String getSqlInsertFile() {
        return sqlInsertFile;
    }

    public static String getSqlDeleteFile() {
        return sqlDeleteFile;
    }

    public static String getBlastParaEvalue() {
        return blastParaEvalue;
    }

    public static String getBlastParaWordSize() {
        return blastParaWordSize;
    }

    public static String getBlastParaThreads() {
        return blastParaThreads;
    }

    public static String getEnsemblInputInterval() {
        return ensemblInputInterval;
    }

    public static String getSqlInsertOutputInterval() {
        return sqlInsertOutputInterval;
    }

    public static String getMysql() {
        return mysql;
    }

    public static String getUsername() {
        return username;
    }

    public static String getPassword() {
        return password;
    }

    public static String getDbName() {
        return dbName;
    }

    public static String getDbNameScript() {
        return dbNameScript;
    }

    public static String getPdbWholeSource() {
        return pdbWholeSource;
    }

    public static String getEnsemblWholeSource() {
        return ensemblWholeSource;
    }

    public static String getUpdateTxt() {
        return updateTxt;
    }

    public static String getUpdateFasta() {
        return updateFasta;
    }

    public static String getDelPDB() {
        return delPDB;
    }

    public static String getUpdateDAY() {
        return updateDAY;
    }

    public static String getUpdateHOUR() {
        return updateHOUR;
    }

    public static String getUpdateMINUTE() {
        return updateMINUTE;
    }

    public static String getUpdateSECOND() {
        return updateSECOND;
    }

    public static String getUpdateMILLISECOND() {
        return updateMILLISECOND;
    }

    public static String getUpdateDELAY() {
        return updateDELAY;
    }

    public static String getInsertSequenceSQL() {
        return insertSequenceSQL;
    }

    public static String getUpdateAdded() {
        return updateAdded;
    }

    public static String getUpdateModified() {
        return updateModified;
    }

    public static String getUpdateObsolete() {
        return updateObsolete;
    }

    public static String getPdbFastaService() {
        return pdbFastaService;
    }

    public static String getMysqlMaxAllowedPacket() {
        return mysqlMaxAllowedPacket;
    }

    public static String getSaveSpaceTag() {
        return saveSpaceTag;
    }

    /**
     * Set Methods
     */
    public static void setRcObj(ReadConfig rcObj) {
        ReadConfig.rcObj = rcObj;
    }
    

    public static void setSwissprotDownloadFile(String swissprotDownloadFile) {
        ReadConfig.swissprotDownloadFile = swissprotDownloadFile;
    }

    public static void setTremblDownloadFile(String tremblDownloadFile) {
        ReadConfig.tremblDownloadFile = tremblDownloadFile;
    }

    public static void setIsoformDownloadFile(String isoformDownloadFile) {
        ReadConfig.isoformDownloadFile = isoformDownloadFile;
    }

    public static void setSeqFastaFile(String seqFastaFile) {
        ReadConfig.seqFastaFile = seqFastaFile;
    }

    public static void setSwissprotWholeSource(String swissprotWholeSource) {
        ReadConfig.swissprotWholeSource = swissprotWholeSource;
    }

    public static void setTremblWholeSource(String tremblWholeSource) {
        ReadConfig.tremblWholeSource = tremblWholeSource;
    }

    public static void setIsoformWholeSource(String isoformWholeSource) {
        ReadConfig.isoformWholeSource = isoformWholeSource;
    }

    public static void setMakeblastdb(String makeblastdb) {
        ReadConfig.makeblastdb = makeblastdb;
    }

    public static void setBlastp(String blastp) {
        ReadConfig.blastp = blastp;
    }

    public static void setWorkspace(String workspace) {
        ReadConfig.workspace = workspace;
    }

    public static void setResourceDir(String resourceDir) {
        ReadConfig.resourceDir = resourceDir;
    }

    public static void setTmpdir(String tmpdir) {
        ReadConfig.tmpdir = tmpdir;
    }

    public static void setPdbRepo(String pdbRepo) {
        ReadConfig.pdbRepo = pdbRepo;
    }

    public static void setPdbSeqresDownloadFile(String pdbSeqresDownloadFile) {
        ReadConfig.pdbSeqresDownloadFile = pdbSeqresDownloadFile;
    }

    public static void setPdbSeqresFastaFile(String pdbSeqresFastaFile) {
        ReadConfig.pdbSeqresFastaFile = pdbSeqresFastaFile;
    }

    public static void setEnsemblDownloadFile(String ensemblDownloadFile) {
        ReadConfig.ensemblDownloadFile = ensemblDownloadFile;
    }

    public static void setSqlInsertFile(String sqlInsertFile) {
        ReadConfig.sqlInsertFile = sqlInsertFile;
    }

    public static void setSqlDeleteFile(String sqlDeleteFile) {
        ReadConfig.sqlDeleteFile = sqlDeleteFile;
    }

    public static void setBlastParaEvalue(String blastParaEvalue) {
        ReadConfig.blastParaEvalue = blastParaEvalue;
    }

    public static void setBlastParaWordSize(String blastParaWordSize) {
        ReadConfig.blastParaWordSize = blastParaWordSize;
    }

    public static void setBlastParaThreads(String blastParaThreads) {
        ReadConfig.blastParaThreads = blastParaThreads;
    }

    public static void setEnsemblInputInterval(String ensemblInputInterval) {
        ReadConfig.ensemblInputInterval = ensemblInputInterval;
    }

    public static void setSqlInsertOutputInterval(String sqlInsertOutputInterval) {
        ReadConfig.sqlInsertOutputInterval = sqlInsertOutputInterval;
    }

    public static void setMysql(String mysql) {
        ReadConfig.mysql = mysql;
    }

    public static void setUsername(String username) {
        ReadConfig.username = username;
    }

    public static void setPassword(String password) {
        ReadConfig.password = password;
    }

    public static void setDbName(String dbName) {
        ReadConfig.dbName = dbName;
    }

    public static void setDbNameScript(String dbNameScript) {
        ReadConfig.dbNameScript = dbNameScript;
    }

    public static void setPdbWholeSource(String pdbWholeSource) {
        ReadConfig.pdbWholeSource = pdbWholeSource;
    }

    public static void setEnsemblWholeSource(String ensemblWholeSource) {
        ReadConfig.ensemblWholeSource = ensemblWholeSource;
    }

    public static void setUpdateTxt(String updateTxt) {
        ReadConfig.updateTxt = updateTxt;
    }

    public static void setUpdateFasta(String updateFasta) {
        ReadConfig.updateFasta = updateFasta;
    }

    public static void setDelPDB(String delPDB) {
        ReadConfig.delPDB = delPDB;
    }

    public static void setUpdateDAY(String updateDAY) {
        ReadConfig.updateDAY = updateDAY;
    }

    public static void setUpdateHOUR(String updateHOUR) {
        ReadConfig.updateHOUR = updateHOUR;
    }

    public static void setUpdateMINUTE(String updateMINUTE) {
        ReadConfig.updateMINUTE = updateMINUTE;
    }

    public static void setUpdateSECOND(String updateSECOND) {
        ReadConfig.updateSECOND = updateSECOND;
    }

    public static void setUpdateMILLISECOND(String updateMILLISECOND) {
        ReadConfig.updateMILLISECOND = updateMILLISECOND;
    }

    public static void setUpdateDELAY(String updateDELAY) {
        ReadConfig.updateDELAY = updateDELAY;
    }

    public static void setInsertSequenceSQL(String insertSequenceSQL) {
        ReadConfig.insertSequenceSQL = insertSequenceSQL;
    }

    public static void setUpdateAdded(String updateAdded) {
        ReadConfig.updateAdded = updateAdded;
    }

    public static void setUpdateModified(String updateModified) {
        ReadConfig.updateModified = updateModified;
    }

    public static void setUpdateObsolete(String updateObsolete) {
        ReadConfig.updateObsolete = updateObsolete;
    }

    public static void setPdbFastaService(String pdbFastaService) {
        ReadConfig.pdbFastaService = pdbFastaService;
    }

    public static void setMysqlMaxAllowedPacket(String mysqlMaxAllowedPacket) {
        ReadConfig.mysqlMaxAllowedPacket = mysqlMaxAllowedPacket;
    }

    public static void setSaveSpaceTag(String saveSpaceTag) {
        ReadConfig.saveSpaceTag = saveSpaceTag;
    }

}
