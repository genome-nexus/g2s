package org.cbioportal.pdb_annotation.scripts;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.io.FastaReaderHelper;
import org.cbioportal.pdb_annotation.util.FTPClientUtil;
import org.cbioportal.pdb_annotation.util.PdbSequenceUtil;
import org.cbioportal.pdb_annotation.util.ReadConfig;

/**
 * Preprocessing the input PDB, Ensembl and Uniprot files, both in init and
 * update pipeline
 *
 * @author Juexin Wang
 *
 */

public class PdbScriptsPipelinePreprocessing {
    final static Logger log = Logger.getLogger(PdbScriptsPipelinePreprocessing.class);
    public int seqFileCount;
    public int seqInputInterval;

    public PdbScriptsPipelinePreprocessing() {
        this.seqInputInterval = Integer.parseInt(ReadConfig.ensemblInputInterval);
        this.seqFileCount = -1;
    }

    public int getSeq_file_count() {
        return seqFileCount;
    }

    public void setSeq_file_count(int seqFileCount) {
        this.seqFileCount = seqFileCount;
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
     */
    public void preprocessPDBsequences(String infileName, String outfileName) {
        try {
            log.info("[Preprocessing] Preprocessing PDB sequences... ");
            LinkedHashMap<String, ProteinSequence> a = FastaReaderHelper.readFastaProteinSequence(new File(infileName));
            StringBuffer sb = new StringBuffer();
            for (Entry<String, ProteinSequence> entry : a.entrySet()) {
                String[] tmp = entry.getValue().getOriginalHeader().toString().split("\\s+");
                if (tmp[1].equals("mol:protein")) {
                    sb.append(">" + entry.getValue().getOriginalHeader() + "\n" + entry.getValue().getSequenceAsString()
                            + "\n");
                }
            }
            // one line contains all AA
            FileWriter fw = new FileWriter(new File(outfileName));
            fw.write(sb.toString());
            fw.close();
            log.info("[Preprocessing] PDB sequences Ready ... ");
        } catch (Exception ex) {
            log.error("[Preprocessing] Fatal Error: Could not Successfully Preprocessing PDB sequences");
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * preprocess PDB sequence update for single file
     * 
     * @param infileName
     * @param outfileName
     */
    public void preprocessPDBsequencesUpdate(String infileName, String outfileName) {
        try {
            log.info("[Preprocessing] Preprocessing PDB sequences... ");
            LinkedHashMap<String, ProteinSequence> a = FastaReaderHelper.readFastaProteinSequence(new File(infileName));
            StringBuffer sb = new StringBuffer();
            for (Entry<String, ProteinSequence> entry : a.entrySet()) {
                String[] tmp = entry.getValue().getOriginalHeader().toString().split("\\|");
                String outstr = tmp[0].replaceAll(":", "_");
                sb.append(">" + outstr + "\n" + entry.getValue().getSequenceAsString() + "\n");
            }
            // one line contains all AA
            FileWriter fw = new FileWriter(new File(outfileName));
            fw.write(sb.toString());
            fw.close();
        } catch (Exception ex) {
            log.error("[Preprocessing] Fatal Error: Could not Successfully Preprocessing PDB sequences");
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Preprocess the Gene sequences download from Ensembl, uniprot This
     * function is designed to split the original FASTA file into several small
     * files. Each small files contains Constants.ensembl_input_interval lines
     * The purpose of doing this is saving memory in next step
     * 
     * @param uniqSeqHm
     *            HashMap <key,value> >1;ensembl;uniprot\nseq\n
     * @param outfilename
     * @return
     */
    public int preprocessGENEsequences(HashMap<String, String> uniqSeqHm, String outfilename) {
        // count of all generated small files
        int filecount = 0;
        try {
            log.info("[Preprocessing] Preprocessing gene sequences... ");
            List<String> list = new ArrayList<String>();

            Collection<String> c = new ArrayList<String>();
            // line count of the original FASTA file, Start from 1
            int count = 1;
            for (Entry<String, String> entry : uniqSeqHm.entrySet()) {
                c.add(">" + count + ";" + entry.getValue() + "\n" + entry.getKey());
                list.add(count + ";" + entry.getValue());
                if (count % this.seqInputInterval == this.seqInputInterval - 1) {
                    FileUtils.writeLines(new File(outfilename + "." + new Integer(filecount).toString()), c);
                    FileUtils.writeLines(new File(outfilename), c, true);
                    c.clear();
                    filecount++;
                }
                count++;
            }
            if (c.size() != 0) {
                FileUtils.writeLines(new File(outfilename + "." + new Integer(filecount++).toString()), c);
                FileUtils.writeLines(new File(outfilename), c, true);
            }
            setSeq_file_count(filecount);
            generateSeqSQLTmpFile(list);
        } catch (Exception ex) {
            log.error("[Preprocessing] Fatal Error: Could not Successfully Preprocessing gene sequences");
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
        return filecount;
    }

    /**
     * Update! New! Preprocess the Gene sequences download from Ensembl, uniprot
     * This function is designed to split the original FASTA file into several
     * small files. Each small files contains Constants.ensembl_input_interval
     * lines The purpose of doing this is saving memory in next step
     * 
     * @param infilename
     * @param outfilename
     * @return
     */
    public int preprocessGENEsequences(String infilename, String outfilename) {
        // count of all generated small files
        int filecount = 0;
        try {
            log.info("[Preprocessing] Preprocessing gene sequences... ");

            LinkedHashMap<String, ProteinSequence> originalHm = FastaReaderHelper
                    .readFastaProteinSequence(new File(infilename));
            HashMap<String, String> outHm = new HashMap<String, String>();
            for (Entry<String, ProteinSequence> entry : originalHm.entrySet()) {
                if (outHm.containsKey(entry.getValue().getSequenceAsString())) {
                    String tmpStr = outHm.get(entry.getValue().getSequenceAsString());
                    tmpStr = tmpStr + ";" + getUniqueSeqID(entry.getKey());
                    outHm.put(entry.getValue().getSequenceAsString(), tmpStr);
                } else {
                    outHm.put(entry.getValue().getSequenceAsString(), getUniqueSeqID(entry.getKey()));
                }
            }

            List<String> list = new ArrayList<String>();

            Collection<String> c = new ArrayList<String>();
            // line count of the original FASTA file, Start from 1
            int count = 1;
            for (Entry<String, String> entry : outHm.entrySet()) {
                c.add(">" + count + ";" + entry.getValue() + "\n" + entry.getKey());
                list.add(count + ";" + entry.getValue());
                if (count % this.seqInputInterval == this.seqInputInterval - 1) {
                    FileUtils.writeLines(new File(outfilename + "." + new Integer(filecount).toString()), c);
                    FileUtils.writeLines(new File(outfilename), c, true);
                    c.clear();
                    filecount++;
                }
                count++;
            }
            if (c.size() != 0) {
                FileUtils.writeLines(new File(outfilename + "." + new Integer(filecount++).toString()), c);
                FileUtils.writeLines(new File(outfilename), c, true);
            }
            setSeq_file_count(filecount);
            generateSeqSQLTmpFile(list);
        } catch (Exception ex) {
            log.error("[Preprocessing] Fatal Error: Could not Successfully Preprocessing gene sequences");
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
        return filecount;
    }

    /**
     * parsing fasta names: ENSEMBL: return ID,gene,transcript UNIPROT:
     * uniprotID
     * 
     * @param inputStr
     * @return
     */
    String getUniqueSeqID(String inputStr) {
        // ENSEMBL
        if (inputStr.startsWith("ENSP") && inputStr.trim().split("\\s+").length > 3) {
            String tmpArray[] = inputStr.trim().split("\\s+");
            return tmpArray[0] + " " + tmpArray[3].split("gene:")[1] + " " + tmpArray[4].split("transcript:")[1];
        }
        // UNIPROT
        else if (inputStr.length() >= 6 && inputStr.length() <= 12) {
            String tmpArray[] = inputStr.trim().split("-");
            if (tmpArray.length == 2) {
                return tmpArray[0] + "_" + tmpArray[1];
            } else {
                return inputStr + "_1";
            }
        }
        // Others, to continue
        else {
            log.error("Sequence ID Parsing Format error in: " + inputStr);
            return "";
        }
    }

    /**
     * deal with redundancy,combine the name together, split with ";"
     * 
     * @param infilename
     * @param outHm
     * @return
     */
    HashMap<String, String> preprocessUniqSeq(String infilename, HashMap<String, String> outHm) {
        try {
            LinkedHashMap<String, ProteinSequence> originalHm = FastaReaderHelper
                    .readFastaProteinSequence(new File(infilename));

            for (Entry<String, ProteinSequence> entry : originalHm.entrySet()) {
                if (outHm.containsKey(entry.getValue().getSequenceAsString())) {
                    String tmpStr = outHm.get(entry.getValue().getSequenceAsString());
                    tmpStr = tmpStr + ";" + getUniqueSeqID(entry.getKey());
                    outHm.put(entry.getValue().getSequenceAsString(), tmpStr);
                } else {
                    outHm.put(entry.getValue().getSequenceAsString(), getUniqueSeqID(entry.getKey()));
                }
            }
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
        return outHm;
    }

    /**
     * Generate TmpSeqSQLFile Fasta headers of ensembl and uniprot genes for
     * generating SQL insert file
     *
     * @param list
     */
    public void generateSeqSQLTmpFile(List<String> list) {
        List<String> outputlist = new ArrayList<String>();
        try {
            // Add transaction
            outputlist.add("SET autocommit = 0;");
            outputlist.add("start transaction;");
            for (String str : list) {
                String[] strarrayQ = str.split(";");
                outputlist.add("INSERT IGNORE INTO `seq_entry`(`SEQ_ID`) VALUES('" + strarrayQ[0] + "');");
                for (int i = 1; i < strarrayQ.length; i++) {
                    if (strarrayQ[i].split("\\s+").length == 3) {// ensembl
                        String[] strarrayQQ = strarrayQ[i].split("\\s+");
                        outputlist
                                .add("INSERT IGNORE INTO `ensembl_entry`(`ENSEMBL_ID`,`ENSEMBL_GENE`,`ENSEMBL_TRANSCRIPT`,`SEQ_ID`) VALUES('"
                                        + strarrayQQ[0] + "', '" + strarrayQQ[1] + "', '" + strarrayQQ[2] + "', '"
                                        + strarrayQ[0] + "');");
                    } else {// uniprot
                        String[] strarrayQQ = strarrayQ[i].split("_");
                        outputlist
                                .add("INSERT IGNORE INTO `uniprot_entry`(`UNIPROT_ID_ISO`,`UNIPROT_ID`,`NAME`,`ISOFORM`,`SEQ_ID`) VALUES('"
                                        + strarrayQ[i] + "', '" + strarrayQQ[0] + "', '" + strarrayQQ[1] + "', '"
                                        + strarrayQ[0] + "');");
                    }
                }
            }
            outputlist.add("commit;");
            // Write File named as insertSequenceSQL in application.properties
            FileUtils.writeLines(new File(ReadConfig.workspace + ReadConfig.insertSequenceSQL), outputlist);
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * prepare weekly updated PDB files
     *
     * @param currentDir
     * @param updateTxt
     * @param delPDB
     * @return
     */
    public List<String> prepareUpdatePDBFile(String currentDir, String updateTxt, String delPDB) {
        List<String> listOld = new ArrayList<String>();
        FTPClientUtil fcu = new FTPClientUtil();
        try {
            log.info("[PIPELINE] Weekly Update: Create deleted list");
            FileUtils.forceMkdir(new File(currentDir));
            String addFileName = currentDir + updateTxt;
            File addFastaFile = new File(addFileName);
            String delFileName = currentDir + delPDB;
            List<String> listAdd = fcu.readFTPfile2List(ReadConfig.updateAdded);
            List<String> listMod = fcu.readFTPfile2List(ReadConfig.updateModified);
            List<String> listObs = fcu.readFTPfile2List(ReadConfig.updateObsolete);
            List<String> listNew = new ArrayList<String>(listAdd);
            listNew.addAll(listMod);
            listOld = new ArrayList<String>(listMod);
            listOld.addAll(listObs);
            String listNewCont = "";
            PdbSequenceUtil pu = new PdbSequenceUtil();
            for (String pdbName : listNew) {
                // listNewCont = listNewCont +
                // fcu.readFTPfile2Str(ReadConfig.pdbFastaService + pdbName);
                // System.out.println(pdbName);
                listNewCont = listNewCont + pu.readPDB2Results(pdbName);
            }
            FileUtils.writeStringToFile(addFastaFile, listNewCont);
            log.info("Write PDB sequences to" + addFastaFile + "...Done");
        } catch (Exception ex) {
            log.error("[SHELL] Error in fetching weekly updates: " + ex.getMessage());
            ex.printStackTrace();
        }
        return listOld;
    }

    /**
     * 
     * de novo preprocess PDB sequence update Used for update complete, and got
     * the updated pdb sequences files
     * 
     * @param dataVersion
     * @param listOld
     * @param infileName
     * @param outfileName
     */
    public void denovoPreprocessPDBsequencesUpdate(String dateVersion, List<String> listOld, String infileName,
            String outfileName) {
        try {
            log.info("[Update] Updating PDB sequences ... ");
            HashMap<String, String> hm = new HashMap<String, String>();
            for (String deletePDB : listOld) {
                hm.put(deletePDB, "");
            }
            LinkedHashMap<String, ProteinSequence> a = FastaReaderHelper
                    .readFastaProteinSequence(new File(outfileName));
            StringBuffer sb = new StringBuffer();
            for (Entry<String, ProteinSequence> entry : a.entrySet()) {
                String pdbName = entry.getValue().getOriginalHeader().toString().split("\\s+")[0].split("_")[0];
                if (!hm.containsKey(pdbName)) {
                    sb.append(">" + entry.getValue().getOriginalHeader().toString() + "\n"
                            + entry.getValue().getSequenceAsString() + "\n");
                }
            }
            LinkedHashMap<String, ProteinSequence> b = FastaReaderHelper.readFastaProteinSequence(new File(infileName));
            for (Entry<String, ProteinSequence> entry : b.entrySet()) {
                sb.append(">" + entry.getValue().getOriginalHeader().toString() + "\t" + dateVersion + "\n"
                        + entry.getValue().getSequenceAsString() + "\n");
            }
            // one line contains all AA
            FileWriter fw = new FileWriter(new File(outfileName));
            fw.write(sb.toString());
            fw.close();
        } catch (Exception ex) {
            log.error("[Preprocessing] Fatal Error: Could not Successfully Preprocessing PDB sequences");
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Obsolete, use Thymeleaf, but does not start automatically
     * 
     * @param releaseResultFilename
     * @param propertyFilename
     */
    /*
     * public void releasTagUpdate(String releaseResultFilename, String
     * propertyFilename){ try{ log.info(
     * "[Update] Generate releaseTag and statistics for update ..."); boolean
     * tag = true; List<String> contents= FileUtils.readLines(new
     * File(releaseResultFilename)); if(contents.size()!=8){ tag = false; }
     * 
     * String outstr = ""; outstr = outstr + "updateDate=" + contents.get(1) +
     * "\r\n"; outstr = outstr + "pdbEntries=" + contents.get(3) + "\r\n";
     * outstr = outstr + "pdbUniqueEntries=" + contents.get(5) + "\r\n"; outstr
     * = outstr + "alignmentEntries=" + contents.get(7) + "\r\n";
     * 
     * FileUtils.writeStringToFile(new File(propertyFilename), outstr);
     * 
     * if(!tag){ log.error(
     * "[Update] Update Error: Could not Successfully generate releaseTag from "
     * + releaseResultFilename +" to "+ propertyFilename +", Please Check"); }
     * 
     * }catch(Exception ex){ log.error(
     * "[Update] Update Error: Could not Successfully generate releaseTag from "
     * + releaseResultFilename +" to "+ propertyFilename +", Please Check");
     * log.error(ex.getMessage()); ex.printStackTrace(); } }
     */

    public void releasTagUpdateSQL(String releaseResultFilename, String sqlFilename) {
        try {
            log.info("[Update] Generate releaseTag and statistics for update ...");
            boolean tag = true;
            List<String> contents = FileUtils.readLines(new File(releaseResultFilename));
            if (contents.size() != 8) {
                tag = false;
            }

            String outstr = "SET autocommit = 0;\nstart transaction;\n";
            outstr = outstr
                    + "INSERT IGNORE INTO `update_record`(`UPDATE_DATE`,`SEG_NUM`,`PDB_NUM`,`ALIGNMENT_NUM`) VALUES('"
                    + contents.get(1) + "', '" + contents.get(3) + "', '" + contents.get(5) + "', '" + contents.get(7)
                    + "');\n";
            outstr = outstr + "commit;\n";

            FileUtils.writeStringToFile(new File(sqlFilename), outstr);

            if (!tag) {
                log.error("[Update] Update Error: Could not Successfully generate releaseTag from "
                        + releaseResultFilename + " to " + sqlFilename + ", Please Check");
            }

        } catch (Exception ex) {
            log.error("[Update] Update Error: Could not Successfully generate releaseTag from " + releaseResultFilename
                    + " to " + sqlFilename + ", Please Check");
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
