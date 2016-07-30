package org.cbioportal.pdb_annotation.scripts;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import org.apache.commons.io.FileUtils;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.io.FastaReaderHelper;
import org.biojava.nbio.core.sequence.io.FastaWriterHelper;
import org.cbioportal.pdb_annotation.util.ReadConfig;
import org.springframework.stereotype.Component;

/**
 * Preprocessing the input PDB and Ensembl files, both in init and update pipeline
 *
 * @author Juexin Wang
 *
 */
@Component
public class PdbScriptsPipelinePreprocessing {
    public String ensemblInputInterval;
    public String ensemblSQLFile;
    public int ensemblFileCount;

    public PdbScriptsPipelinePreprocessing() {
        this.ensemblFileCount = -1;
    }

    public PdbScriptsPipelinePreprocessing(ReadConfig rc) {
        this.ensemblFileCount = -1;
        this.ensemblInputInterval = rc.ensembl_input_interval;
        this.ensemblSQLFile=rc.workspace+rc.sql_ensemblSQL;
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
     * @return Success/Failure
     */
    public boolean preprocessPDBsequences(String infileName, String outfileName) {
        try {
            System.out.println("[Preprocessing] Preprocessing PDB sequences... ");
            LinkedHashMap<String, ProteinSequence> a = FastaReaderHelper.readFastaProteinSequence(new File(infileName));
            StringBuffer sb = new StringBuffer();
            for (Entry<String, ProteinSequence> entry : a.entrySet()) {
                String[] tmp = entry.getValue().getOriginalHeader().toString().split("\\s+");
                if (tmp[1].equals("mol:protein")) {
                    sb.append(">" + entry.getValue().getOriginalHeader() + "\n" + entry.getValue().getSequenceAsString() + "\n");
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
     * preprocess PDB sequence update
     * @param infileName
     * @param outfileName
     * @return
     */
    public boolean preprocessPDBsequencesUpdate(String infileName, String outfileName) {
        try {
            System.out.println("[Preprocessing] Preprocessing PDB sequences... ");
            LinkedHashMap<String, ProteinSequence> a = FastaReaderHelper.readFastaProteinSequence(new File(infileName));
            StringBuffer sb = new StringBuffer();
            for (Entry<String, ProteinSequence> entry : a.entrySet()) {
                String[] tmp = entry.getValue().getOriginalHeader().toString().split("\\|");
                String outstr = tmp[0].replaceAll(":", "_");
                sb.append(">" + outstr + "\n" + entry.getValue().getSequenceAsString() + "\n");
            }
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
     * @return count
     */
    public int preprocessGENEsequences(String infilename, String outfilename) {
        // count of all generated small files
        int filecount = 0;
        try {
            System.out.println("[Preprocessing] Preprocessing Ensembl sequences... ");
            List<String> list = new ArrayList();
            LinkedHashMap<String, ProteinSequence> a = FastaReaderHelper.readFastaProteinSequence(new File(infilename));
            Collection<ProteinSequence> c = new ArrayList<ProteinSequence>();
            int count = 0; // line count of the original FASTA file
            int ensembl_input_interval_int = Integer.parseInt(this.ensemblInputInterval);
            for (Entry<String, ProteinSequence> entry : a.entrySet()) {
                c.add(entry.getValue());
                list.add(entry.getValue().getOriginalHeader());
                if (count % ensembl_input_interval_int == ensembl_input_interval_int - 1) {
                    FastaWriterHelper.writeProteinSequence(new File(outfilename + "." + new Integer(filecount).toString()), c);
                    c.clear();
                    filecount++;
                }
                count++;
            }
            FastaWriterHelper.writeProteinSequence(new File(outfilename + "." + new Integer(filecount++).toString()), c);
            setEnsembl_file_count(filecount);
            generateEnsemblSQLTmpFile(list);
        } catch (Exception ex) {
            System.err.println("[Preprocessing] Fatal Error: Could not Successfully Preprocessing Ensembl sequences");
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
    public boolean generateEnsemblSQLTmpFile(List<String> list) {
        List outlist = new ArrayList();
        try {
            for(String str:list) {
                String[] strarrayQ = str.split("\\s+");
                outlist.add("INSERT IGNORE INTO `ensembl_entry`(`ENSEMBL_ID`,`ENSEMBL_GENE`,`ENSEMBL_TRANSCRIPT`) VALUES('"
                        + strarrayQ[0] + "', '" + strarrayQ[3].split(":")[1] + "', '" + strarrayQ[4].split(":")[1]
                        + "');");
            }
            FileUtils.writeLines(new File(this.ensemblSQLFile), outlist);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return true;
    }
}
