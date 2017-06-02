package org.cbioportal.pdb_annotation.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.cbioportal.pdb_annotation.util.ReadConfig;
import org.cbioportal.pdb_annotation.util.blast.BlastDataBase;
import org.cbioportal.pdb_annotation.util.blast.BlastOutput;
import org.cbioportal.pdb_annotation.util.blast.BlastOutputIterations;
import org.cbioportal.pdb_annotation.util.blast.BlastResult;
import org.cbioportal.pdb_annotation.util.blast.Hit;
import org.cbioportal.pdb_annotation.util.blast.Hsp;
import org.cbioportal.pdb_annotation.util.blast.Iteration;
import org.cbioportal.pdb_annotation.util.blast.IterationHits;

/**
 * SQL Insert statements Generation
 * 
 * @author Juexin Wang
 *
 */

public class PdbScriptsPipelineMakeSQL {
    final static Logger log = Logger.getLogger(PdbScriptsPipelineMakeSQL.class);
    private static final String HTTP_AGENT_PROPERTY_VALUE = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
    private BlastDataBase db;
    private int matches;
    private int seqFileCount;
    private String workspace;
    private String sqlInsertFile;
    private String sqlInsertOutputInterval;
    private String sqlDeleteFile;
    private String insertSequenceSQL;
    private boolean updateTag;//if update, then true;

    /**
     * 
     * Constructor
     * 
     * @param app
     */
    PdbScriptsPipelineMakeSQL(PdbScriptsPipelineRunCommand app) {
        this.db = app.getDb();
        this.matches = app.getMatches();
        this.seqFileCount = app.getSeqFileCount();
        this.workspace = ReadConfig.workspace;
        this.sqlInsertFile = ReadConfig.sqlInsertFile;
        this.sqlInsertOutputInterval = ReadConfig.sqlInsertOutputInterval;
        this.sqlDeleteFile = ReadConfig.sqlDeleteFile;
        this.insertSequenceSQL = ReadConfig.insertSequenceSQL;
        this.updateTag = app.isUpdateTag();
    }

    /**
     * parse XML blast results to INSERT SQL file
     * 
     * @param oneInputTag
     *            multiple SQL or not
     * @param currentDir
     *            on which directory to store this sql
     */
    public void parse2sql(boolean oneInputTag, String currentDir, int countnum) {
        //System.out.println(this.updateTag);
        System.setProperty("javax.xml.accessExternalDTD", "all");
        System.setProperty("http.agent", HTTP_AGENT_PROPERTY_VALUE); // http.agent
                                                                     // is
                                                                     // needed
                                                                     // to fetch
                                                                     // dtd from
                                                                     // some
                                                                     // servers
        //System.out.println("this.seqFileCount:" + this.seqFileCount);
        this.workspace = currentDir;
        this.seqFileCount = countnum;
        if (!oneInputTag) {
            // multiple input, multiple sql generated incrementally
            if (this.seqFileCount == -1) {
                parseblastresultsSmallMem();
            } else {
                HashMap<String, String> pdbHm = new HashMap<String, String>();
                for (int i = 0; i < this.seqFileCount; i++) {
                    parseblastresultsSmallMem(i, pdbHm);
                }
            }
        } else {
            // test for small datasets: single input, single sql generated in
            // one time
            List<BlastResult> outresults = parseblastresultsSingle(currentDir);
            generateSQLstatementsSingle(outresults, currentDir);
        }
    }
    
    /**
     * Used for save space
     * @param oneInputTag
     * @param currentDir
     * @param countnum
     * @param i
     */
    public void parse2sqlPartition(boolean oneInputTag, String currentDir, int countnum, int i, HashMap<String, String> pdbHm) {
        //System.out.println(this.updateTag);
        System.setProperty("javax.xml.accessExternalDTD", "all");
        System.setProperty("http.agent", HTTP_AGENT_PROPERTY_VALUE); // http.agent
                                                                     // is
                                                                     // needed
                                                                     // to fetch
                                                                     // dtd from
                                                                     // some
                                                                     // servers
        //System.out.println("this.seqFileCount:" + this.seqFileCount);
        this.workspace = currentDir;
        this.seqFileCount = countnum;
        if (!oneInputTag) {
            // multiple input, multiple sql generated incrementally
            if (this.seqFileCount == -1) {
                parseblastresultsSmallMem();
            } else {
                
                
                parseblastresultsSmallMem(i, pdbHm);
                
            }
        } else {
            // test for small datasets: single input, single sql generated in
            // one time
            List<BlastResult> outresults = parseblastresultsSingle(currentDir);
            generateSQLstatementsSingle(outresults, currentDir);
        }
    }

    /**
     * parse Single blast XML results, output to SQL file incrementally
     * 
     */
    public void parseblastresultsSmallMem() {
        try {
            log.info("[BLAST] Read blast results from xml file...");
            File blastresults = new File(this.workspace + this.db.resultfileName);
            File outputfile = new File(this.workspace + this.sqlInsertFile);
            HashMap<String, String> pdbHm = new HashMap<String, String>();
            int count = parsexml(blastresults, outputfile, pdbHm);
            this.matches = count;
            log.info("[BLAST] Insert Statements of the file is : " + this.matches);
        } catch (Exception ex) {
            log.error("[BLAST] Error Parsing BLAST Result");
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * parse multiple blast XML results, output to SQL file incrementally
     * 
     * @param filecount:
     *            id of the multiple files
     * @return
     */
    public void parseblastresultsSmallMem(int filecount, HashMap<String, String> pdbHm) {
        try {
            log.info("[BLAST] Read blast results from " + filecount + "th xml file...");
            File blastresults = new File(this.workspace + this.db.resultfileName + "." + filecount);
            File outputfile;
            // Check whether multiple files existed
            if (this.seqFileCount != -1) {
                outputfile = new File(this.workspace + this.sqlInsertFile + "." + filecount);
            } else {
                outputfile = new File(this.workspace + this.sqlInsertFile);
            }
            int count = parsexml(blastresults, outputfile, pdbHm);
            this.matches = this.matches + count;
            log.info("[BLAST] Insert statements after parsing " + filecount + "th xml : " + this.matches);
        } catch (Exception ex) {
            log.error("[BLAST] Error Parsing BLAST Result");
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * main body of parsing xml
     * 
     * @param blastresults
     * @param outputfile
     * @param pdbHm
     * @return count
     */
    public int parsexml(File blastresults, File outputfile, HashMap<String, String> pdbHm) {
        int count = 1;
        try {
            JAXBContext jc = JAXBContext.newInstance("org.cbioportal.pdb_annotation.util.blast");
            Unmarshaller u = jc.createUnmarshaller();
            u.setSchema(null);
            BlastOutput blast = (BlastOutput) u.unmarshal(blastresults);
            List<BlastResult> results = new ArrayList<BlastResult>();
            BlastOutputIterations iterations = blast.getBlastOutputIterations();
            log.info("[BLAST] Start parsing results...");
            int sql_insert_output_interval = Integer.parseInt(this.sqlInsertOutputInterval);
            for (Iteration iteration : iterations.getIteration()) {
                String querytext = iteration.getIterationQueryDef();
                IterationHits hits = iteration.getIterationHits();
                for (Hit hit : hits.getHit()) {
                    results.addAll(parseSingleAlignment(querytext, hit, count));
                    count=results.size()+1;
                   
                    //No need anymore
                    //TODO: need number
                    /*
                    if (count % sql_insert_output_interval == 0) {
                        // Once get the criteria, output contents to the SQL
                        // file
                        genereateSQLstatementsSmallMem(results, pdbHm, count, outputfile);
                        count+=results.size();
                        results.clear();                        
                    }
                    */
                }
            }
            // output remaining contents to the SQL file
            genereateSQLstatementsSmallMem(results, pdbHm, count, outputfile);
            
        } catch (Exception ex) {
            log.error("[BLAST] Error Parsing BLAST Result");
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
        return count;
    }

    /**
     * generate SQL insert text to Table pdb_entry
     * 
     * @param br
     * @return
     */
    public String makeTable_pdb_entry_insert(BlastResult br) {
        String pdbNo = br.getSseqid().split("\\s+")[0];
        String[] strarrayS = pdbNo.split("_");
        String segStart = br.getSseqid().split("\\s+")[3];
        
        String str = "INSERT IGNORE INTO `pdb_entry` (`PDB_NO`,`PDB_ID`,`CHAIN`,`PDB_SEG`,`SEG_START`) VALUES ('" + pdbNo
                + "', '" + strarrayS[0] + "', '" + strarrayS[1] + "', '" + strarrayS[2] + "', '" + segStart + "');\n";
        return str;
    }

    /**
     * generate SQL insert text to Table pdb_ensembl_alignment
     * 
     * @param br
     * @return generated SQL statements
     */
    public String makeTable_pdb_ensembl_insert(BlastResult br) {
        String[] strarrayQ = br.getQseqid().split(";");
        String pdbNo =  br.getSseqid().split("\\s+")[0];
        String[] strarrayS = pdbNo.split("_");
        String segStart = br.getSseqid().split("\\s+")[3];
        String str = "INSERT INTO `pdb_seq_alignment` (`PDB_NO`,`PDB_ID`,`CHAIN`,`PDB_SEG`,`SEG_START`,`SEQ_ID`,`PDB_FROM`,`PDB_TO`,`SEQ_FROM`,`SEQ_TO`,`EVALUE`,`BITSCORE`,`IDENTITY`,`IDENTP`,`SEQ_ALIGN`,`PDB_ALIGN`,`MIDLINE_ALIGN`,`UPDATE_DATE`)VALUES ('"
                + pdbNo + "','" + strarrayS[0] + "','" + strarrayS[1] + "','" + strarrayS[2] + "','" + segStart + "','"
                + strarrayQ[0] + "'," + br.getsStart() + "," + br.getsEnd() + "," + br.getqStart() + "," + br.getqEnd()
                + ",'" + br.getEvalue() + "'," + br.getBitscore() + "," + br.getIdent() + "," + br.getIdentp() + ",'"
                + br.getSeq_align() + "','" + br.getPdb_align() + "','" + br.getMidline_align() + "',CURDATE());\n";
        return str;
    }
    
    
    /**
     * Used for Update:
     * generate SQL insert text to Table pdb_ensembl_alignment
     * 
     * The only variate in the procedure is alignment limit, which now is set as 50
     * 
     * Call Procedure InsertUpdate ()
     * 
     * The Procedure is integrated with pdb.sql:
     * 
DROP PROCEDURE IF EXISTS `InsertUpdate`;
DELIMITER //
CREATE PROCEDURE InsertUpdate(IN inPDB_NO VARCHAR(12), IN inPDB_ID VARCHAR(4), IN inCHAIN VARCHAR(4), IN inPDB_SEG VARCHAR(2), IN inSEG_START VARCHAR(4), IN inSEQ_ID int, IN inPDB_FROM int, IN inPDB_TO int, IN inSEQ_FROM int, IN inSEQ_TO int, IN inEVALUE double, IN inBITSCORE float, IN inIDENTITY float, IN inIDENTP float, IN inSEQ_ALIGN text, IN inPDB_ALIGN text, IN inMIDLINE_ALIGN text, IN inUPDATE_DATE DATE )
BEGIN
DECLARE maxEvalue double;
DECLARE countEvalue double;
SELECT COUNT(*) INTO countEvalue FROM pdb_seq_alignment where PDB_NO=inPDB_NO;
SELECT MAX(D) INTO countEvalue FROM pdb_seq_alignment where PDB_NO=inPDB_NO;
IF(inEVALUE<maxEvalue) THEN
  IF(countEvalue<50) THEN
    INSERT INTO `pdb_seq_alignment` (`PDB_NO`,`PDB_ID`,`CHAIN`,`PDB_SEG`,`SEG_START`,`SEQ_ID`,`PDB_FROM`,`PDB_TO`,`SEQ_FROM`,`SEQ_TO`,`EVALUE`,`BITSCORE`,`IDENTITY`,`IDENTP`,`SEQ_ALIGN`,`PDB_ALIGN`,`MIDLINE_ALIGN`,`UPDATE_DATE`) VALUES (inPDB_NO,inPDB_ID,inCHAIN,inPDB_SEG,inSEG_START,inSEQ_ID,inPDB_FROM,inPDB_TO,inSEQ_FROM,inSEQ_TO,inEVALUE,inBITSCORE,inIDENTITY,inIDENTP,inSEQ_ALIGN,inPDB_ALIGN,inMIDLINE_ALIGN,inUPDATE_DATE);
  ELSE
    DELETE FROM `pdb_seq_alignment` WHERE (PDB_NO=inPDB_NO and EVALUE=inEVALUE);
    INSERT INTO `pdb_seq_alignment` (`PDB_NO`,`PDB_ID`,`CHAIN`,`PDB_SEG`,`SEG_START`,`SEQ_ID`,`PDB_FROM`,`PDB_TO`,`SEQ_FROM`,`SEQ_TO`,`EVALUE`,`BITSCORE`,`IDENTITY`,`IDENTP`,`SEQ_ALIGN`,`PDB_ALIGN`,`MIDLINE_ALIGN`,`UPDATE_DATE`) VALUES (inPDB_NO,inPDB_ID,inCHAIN,inPDB_SEG,inSEG_START,inSEQ_ID,inPDB_FROM,inPDB_TO,inSEQ_FROM,inSEQ_TO,inEVALUE,inBITSCORE,inIDENTITY,inIDENTP,inSEQ_ALIGN,inPDB_ALIGN,inMIDLINE_ALIGN,inUPDATE_DATE);
  END IF;
ELSE
  IF(countEvalue<50) THEN
    INSERT INTO `pdb_seq_alignment` (`PDB_NO`,`PDB_ID`,`CHAIN`,`PDB_SEG`,`SEG_START`,`SEQ_ID`,`PDB_FROM`,`PDB_TO`,`SEQ_FROM`,`SEQ_TO`,`EVALUE`,`BITSCORE`,`IDENTITY`,`IDENTP`,`SEQ_ALIGN`,`PDB_ALIGN`,`MIDLINE_ALIGN`,`UPDATE_DATE`) VALUES (inPDB_NO,inPDB_ID,inCHAIN,inPDB_SEG,inSEG_START,inSEQ_ID,inPDB_FROM,inPDB_TO,inSEQ_FROM,inSEQ_TO,inEVALUE,inBITSCORE,inIDENTITY,inIDENTP,inSEQ_ALIGN,inPDB_ALIGN,inMIDLINE_ALIGN,inUPDATE_DATE);
  END IF;
END IF;
END //
DELIMITER ;
     * 
     * 
     * 
     * 
     * @param br
     * @return generated SQL statements
     */
    public String makeTable_pdb_ensembl_insert_Update(BlastResult br) {
        String[] strarrayQ = br.getQseqid().split(";");
        String pdbNo =  br.getSseqid().split("\\s+")[0];
        String[] strarrayS = pdbNo.split("_");
        String segStart = br.getSseqid().split("\\s+")[3];
        
        String str = "call InsertUpdate('"
                + pdbNo + "','" + strarrayS[0] + "','" + strarrayS[1] + "','" + strarrayS[2] + "','" + segStart + "','"
                + strarrayQ[0] + "'," + br.getsStart() + "," + br.getsEnd() + "," + br.getqStart() + "," + br.getqEnd()
                + ",'" + br.getEvalue() + "'," + br.getBitscore() + "," + br.getIdent() + "," + br.getIdentp() + ",'"
                + br.getSeq_align() + "','" + br.getPdb_align() + "','" + br.getMidline_align() + "',CURDATE());\n";
               
        /*
        String str = "INSERT INTO `pdb_seq_alignment` (`PDB_NO`,`PDB_ID`,`CHAIN`,`PDB_SEG`,`SEG_START`,`SEQ_ID`,`PDB_FROM`,`PDB_TO`,`SEQ_FROM`,`SEQ_TO`,`EVALUE`,`BITSCORE`,`IDENTITY`,`IDENTP`,`SEQ_ALIGN`,`PDB_ALIGN`,`MIDLINE_ALIGN`,`UPDATE_DATE`)VALUES ('"
                + pdbNo + "','" + strarrayS[0] + "','" + strarrayS[1] + "','" + strarrayS[2] + "','" + segStart + "','"
                + strarrayQ[0] + "'," + br.getsStart() + "," + br.getsEnd() + "," + br.getqStart() + "," + br.getqEnd()
                + ",'" + br.getEvalue() + "'," + br.getBitscore() + "," + br.getIdent() + "," + br.getIdentp() + ",'"
                + br.getSeq_align() + "','" + br.getPdb_align() + "','" + br.getMidline_align() + "',CURDATE());\n";
        */
        return str;
    }
    
    
    

    /**
     * Parse list of String blast results to input SQL statements, time and
     * memory consuming for huge files Use
     * 
     * @param outresults
     *            List<BlastResult>
     */
    public void generateSQLstatementsSingle(List<BlastResult> results, String currentDir) {
        try {
            log.info("[SHELL] Start Write insert.sql File...");
            File file = new File(currentDir + this.sqlInsertFile);
            // HashMap pdbHm is designed to avoid duplication of
            // primary keys in SQL
            // if we already have the entry, do nothing; otherwise generate the
            // SQL and add the keys into the HashMap
            HashMap<String, String> pdbHm = new HashMap<String, String>();
            List<String> outputlist = makeSQLText(results, pdbHm);
            FileUtils.writeLines(file, outputlist, "");
            log.info("[SHELL] Write insert.sql Done");
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * Parse multiple list of String blast results to multiple input SQL
     * statements
     * 
     * @param List<BlastResult>
     *            results
     * @param pdbHm
     * @param count
     * @param outputfile
     */
    public void genereateSQLstatementsSmallMem(List<BlastResult> results, HashMap<String, String> pdbHm, long count,
            File outputfile) {
        try {
            log.info("[SHELL] Start Write insert.sql File from Alignment " + count + "...");
            if (count == 1) {
                // check, if starts, make sure it is empty
                if (outputfile.exists()) {
                    outputfile.delete();
                }
            }
            List<String> outputlist = makeSQLText(results, pdbHm);
            FileUtils.writeLines(outputfile, outputlist, "", true);
            outputlist = null;
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }

    /**
     * main body of generate SQL Insert text
     * 
     * @param results
     * @param pdbHm
     * @return
     */
    public List<String> makeSQLText(List<BlastResult> results, HashMap<String, String> pdbHm) {
        List<String> outputlist = new ArrayList<String>();
        // Add transaction
        outputlist.add("SET autocommit = 0;");
        outputlist.add("start transaction;");
        for (BlastResult br : results) {
            if (pdbHm.containsKey(br.getSseqid())) {
                // do nothing
            } else {
                outputlist.add(makeTable_pdb_entry_insert(br));
                pdbHm.put(br.getSseqid(), "");
            }
            //If it is update, then call function
            if(this.updateTag){
                outputlist.add(makeTable_pdb_ensembl_insert_Update(br));
            //If it is init, generate INSERT statements
            }else{
                outputlist.add(makeTable_pdb_ensembl_insert(br));
            }
            
        }
        outputlist.add("commit;");
        return outputlist;
    }

    /**
     * Parse one single file of blast results to list of String, time and memory
     * consuming for huge files
     * 
     * @return List<BlastResult>
     */
    public List<BlastResult> parseblastresultsSingle(String currentDir) {
        List<BlastResult> results = new ArrayList<BlastResult>();
        try {
            log.info("[BLAST] Read blast results from xml file...");
            File blastresults = new File(currentDir + this.db.resultfileName);
            JAXBContext jc = JAXBContext.newInstance("org.cbioportal.pdb_annotation.util.blast");
            Unmarshaller u = jc.createUnmarshaller();
            u.setSchema(null);
            BlastOutput blast = (BlastOutput) u.unmarshal(blastresults);
            BlastOutputIterations iterations = blast.getBlastOutputIterations();
            int count = 1;
            for (Iteration iteration : iterations.getIteration()) {
                String querytext = iteration.getIterationQueryDef();
                IterationHits hits = iteration.getIterationHits();
                for (Hit hit : hits.getHit()) {
                    results.addAll(parseSingleAlignment(querytext, hit, count));
                    count = results.size()+1;
                }
            }
            this.matches = count - 1;
            log.info("[BLAST] Total Insert " + this.matches + " alignments");
        } catch (Exception ex) {
            log.error("[BLAST] Error Parsing BLAST Result");
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
        return results;
    }

    /**
     * Parse XML structure into Object BlastResult
     * 
     * @param querytext
     * @param hit
     * @param count
     * @return
     */
    public List<BlastResult> parseSingleAlignment(String querytext, Hit hit, int count) {
        
        List<BlastResult> resultList = new ArrayList<BlastResult>();
        
        List<Hsp> tmplist = hit.getHitHsps().getHsp();
        for (Hsp tmp : tmplist) {
            BlastResult br = new BlastResult(count);
            br.qseqid = querytext;
            br.sseqid = hit.getHitDef();
            br.ident = Double.parseDouble(tmp.getHspIdentity()); 
            br.identp = Double.parseDouble(tmp.getHspPositive()); 
            br.evalue = Double.parseDouble(tmp.getHspEvalue()); 
            br.bitscore = Double.parseDouble(tmp.getHspBitScore()); 
            br.qStart = Integer.parseInt(tmp.getHspQueryFrom()); 
            br.qEnd = Integer.parseInt(tmp.getHspQueryTo()); 
            br.sStart = Integer.parseInt(tmp.getHspHitFrom()); 
            br.sEnd = Integer.parseInt(tmp.getHspHitTo()); 
            br.seq_align = tmp.getHspQseq(); 
            br.pdb_align = tmp.getHspHseq(); 
            br.midline_align = tmp.getHspMidline();
            resultList.add(br);
            count++;
        }
         
        return resultList;
    }

    /**
     * generate sql in delete
     * 
     * @param currentDir
     * @param list
     */
    public void generateDeleteSql(String currentDir, List<String> list) {
        try {
            log.info("[PIPELINE] Generating delete SQL statements");
            File outfile = new File(currentDir + this.sqlDeleteFile);
            List<String> outputlist = new ArrayList<String>();
            // Add transaction
            outputlist.add("SET autocommit = 0;");
            outputlist.add("start transaction;");
            for (String pdbName : list) {
                String str = "DELETE pdb_seq_alignment FROM pdb_seq_alignment inner join pdb_entry on pdb_entry.pdb_no=pdb_seq_alignment.pdb_no WHERE pdb_seq_alignment.pdb_id='"
                        + pdbName + "';\n";
                outputlist.add(str);
                String str1 = "DELETE FROM pdb_entry WHERE PDB_ID='" + pdbName + "';\n";
                outputlist.add(str1);
            }
            outputlist.add("commit;");
            FileUtils.writeLines(outfile, outputlist, "");
            log.info("[SHELL] Totally delete " + list.size() + " obsolete and modified alignments");
        } catch (Exception ex) {
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
    }
}
