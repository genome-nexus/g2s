package org.cbioportal.pdb_annotation.scripts;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.io.FastaReaderHelper;
import org.biojava.nbio.core.sequence.io.FastaWriterHelper;
import org.cbioportal.pdb_annotation.util.CommandProcessUtil;
import org.cbioportal.pdb_annotation.util.FTPClientUtil;
import org.cbioportal.pdb_annotation.util.PdbSequenceUtil;
import org.cbioportal.pdb_annotation.util.ReadConfig;

/**
 * Preprocessing the input PDB and Ensembl files, both in init and update pipeline
 *
 * @author Juexin Wang
 *
 */

public class PdbScriptsPipelinePreprocessing {
    final static Logger log = Logger.getLogger(PdbScriptsPipelinePreprocessing.class);
    public int ensemblFileCount;
    public int ensemblInputInterval;

    public PdbScriptsPipelinePreprocessing() {
        this.ensemblInputInterval = Integer.parseInt(ReadConfig.ensemblInputInterval);
        this.ensemblFileCount = -1;
    }

    public int getEnsembl_file_count() {
        return ensemblFileCount;
    }

    public void setEnsembl_file_count(int ensembl_file_count) {
        this.ensemblFileCount = ensembl_file_count;
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
                    sb.append(">" + entry.getValue().getOriginalHeader() + "\n" + entry.getValue().getSequenceAsString() + "\n");
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
     * preprocess PDB sequence update
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
     * @return count
     */
    public int preprocessGENEsequences(String infilename, String outfilename) {
        // count of all generated small files
        int filecount = 0;
        try {
            log.info("[Preprocessing] Preprocessing Ensembl sequences... ");
            List<String> list = new ArrayList<String>();
            LinkedHashMap<String, ProteinSequence> a = FastaReaderHelper.readFastaProteinSequence(new File(infilename));
            Collection<ProteinSequence> c = new ArrayList<ProteinSequence>();
            int count = 0; // line count of the original FASTA file
            for (Entry<String, ProteinSequence> entry : a.entrySet()) {
                c.add(entry.getValue());
                list.add(entry.getValue().getOriginalHeader());
                if (count % this.ensemblInputInterval == this.ensemblInputInterval - 1) {
                    FastaWriterHelper.writeProteinSequence(new File(outfilename + "." + new Integer(filecount).toString()), c);
                    c.clear();
                    filecount++;
                }
                count++;
            }
            if(c.size()!=0){
                FastaWriterHelper.writeProteinSequence(new File(outfilename + "." + new Integer(filecount++).toString()), c);
            }           
            setEnsembl_file_count(filecount);
            generateEnsemblSQLTmpFile(list);
        } catch (Exception ex) {
            log.error("[Preprocessing] Fatal Error: Could not Successfully Preprocessing Ensembl sequences");
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
        return filecount;
    }

    /**
     * Generate TmpEnsemblSQLFile
     * Ensembl header for generating SQL insert
     *
     * @param list
     */
    public void generateEnsemblSQLTmpFile(List<String> list) {
        List<String> outputlist = new ArrayList<String>();
        try {
            //Add transaction
            outputlist.add("SET autocommit = 0;");
            outputlist.add("start transaction;");
            for(String str:list) {
                String[] strarrayQ = str.split("\\s+");
                outputlist.add("INSERT IGNORE INTO `ensembl_entry`(`ENSEMBL_ID`,`ENSEMBL_GENE`,`ENSEMBL_TRANSCRIPT`) VALUES('"
                        + strarrayQ[0] + "', '" + strarrayQ[3].split(":")[1] + "', '" + strarrayQ[4].split(":")[1]
                                + "');");
            }
            outputlist.add("commit;");
            // Write File named as sqlEnsemblSQL in application.properties
            FileUtils.writeLines(new File(ReadConfig.workspace+ReadConfig.sqlEnsemblSQL), outputlist);
        } catch(Exception ex) {
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
            for(String pdbName:listNew) {
                //listNewCont = listNewCont + fcu.readFTPfile2Str(ReadConfig.pdbFastaService + pdbName);
                //System.out.println(pdbName);
                listNewCont = listNewCont + pu.readPDB2Results(pdbName);
            }
            FileUtils.writeStringToFile(addFastaFile, listNewCont);
        } catch(Exception ex) {
            log.error("[SHELL] Error in fetching weekly updates: "+ex.getMessage());
            ex.printStackTrace();
        }
        return listOld;
    }


}
