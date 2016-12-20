package org.cbioportal.pdb_annotation.scripts;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.cbioportal.pdb_annotation.util.CommandProcessUtil;
import org.cbioportal.pdb_annotation.util.ReadConfig;
import org.cbioportal.pdb_annotation.util.blast.BlastDataBase;
import org.cbioportal.pdb_annotation.util.blast.BlastOutput;
import org.cbioportal.pdb_annotation.util.blast.BlastOutputIterations;
import org.cbioportal.pdb_annotation.util.blast.Hit;
import org.cbioportal.pdb_annotation.util.blast.Hsp;
import org.cbioportal.pdb_annotation.util.blast.Iteration;
import org.cbioportal.pdb_annotation.util.blast.IterationHits;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.Inputsequence;

/**
 * Shell-based command running
 *
 * @author Juexin Wang
 *
 */

public class PdbScriptsPipelineRunCommand {
    final static Logger log = Logger.getLogger(PdbScriptsPipelineRunCommand.class);
    private static final String HTTP_AGENT_PROPERTY_VALUE =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";   
    private BlastDataBase db;

    /**
     * Constructor
     */
    public BlastDataBase getDb() {
        return db;
    }

    public void setDb(BlastDataBase db) {
        this.db = db;
    }
    
    /**
     * main steps of init pipeline
     */
    public List<Alignment> runCommand(Inputsequence inputsequence) {   	

        CommandProcessUtil cu = new CommandProcessUtil();
        ReadConfig rc = ReadConfig.getInstance();
        this.db = new BlastDataBase(ReadConfig.pdbSeqresFastaFile);               
        
        ArrayList<String> paralist = new ArrayList<String>();
        
        webInput2File(inputsequence);

        //blastp input genes against pdb
        paralist = new ArrayList<String>();
        paralist.add(ReadConfig.uploaddir + inputsequence.getId()+".fasta");
        paralist.add(ReadConfig.uploaddir + this.db.resultfileName);
        paralist.add(ReadConfig.workspace + this.db.dbName);
        cu.runCommand("blastp", paralist); 
        
        //parse results and output results
        List<Alignment> outresults = parseblastresultsSingle(ReadConfig.uploaddir);
        
        //Clean Up
        paralist = new ArrayList<String>(); 
        paralist.add(ReadConfig.uploaddir + inputsequence.getId()+".fasta" );
        cu.runCommand("rm", paralist);
        
        paralist = new ArrayList<String>(); 
        paralist.add(ReadConfig.uploaddir + this.db.resultfileName );
        cu.runCommand("rm", paralist);
        
        return outresults;       
    }
    
    /**
     * 
     * @param inputsequence
     * @return
     */
    public void webInput2File(Inputsequence inputsequence){
        try{
            FileUtils.writeStringToFile(new File(ReadConfig.uploaddir + inputsequence.getId()+".fasta"), inputsequence.getSequence());
        }catch(Exception ex){
            ex.printStackTrace();
        }
    }
    
    
    /**
     * Parse one single file of blast results to list of String, time and memory
     * consuming for huge files
     * 
     * @return List<BlastResult>
     */
    public List<Alignment> parseblastresultsSingle(String currentDir) {
        System.setProperty("javax.xml.accessExternalDTD", "all");
        System.setProperty("http.agent", HTTP_AGENT_PROPERTY_VALUE); //http.agent is needed to fetch dtd from some servers
        List<Alignment> results = new ArrayList<Alignment>();
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
                    Alignment alignment = parseSingleAlignment(querytext, hit, count);
                    results.add(alignment);
                    count++;
                }
            }
            log.info("[BLAST] Total Insert " + (count - 1) + " alignments");
        } catch (Exception ex) {
            log.error("[BLAST] Error Parsing BLAST Result");
            log.error(ex.getMessage());
            ex.printStackTrace();
        }
        return results;
    }


    /**
     * Parse XML structure into Object Alignment
     * 
     * @param querytext
     * @param hit
     * @param count
     * @return
     */
    public Alignment parseSingleAlignment(String querytext, Hit hit, int count) {
        Alignment alignment = new Alignment(count);
        
        alignment.setSeqId(querytext.split("\\s+")[0]);
        alignment.setPdbNo(hit.getHitDef().split("\\s+")[0]);
        alignment.setPdbId(hit.getHitDef().split("\\s+")[0].split("_")[0]);
        alignment.setChain(hit.getHitDef().split("\\s+")[0].split("_")[1]);
        alignment.setPdbSeg(hit.getHitDef().split("\\s+")[0].split("_")[2]);
        for (Hsp tmp : hit.getHitHsps().getHsp()) {
            alignment.setIdentity(Double.parseDouble(tmp.getHspIdentity()));
            alignment.setIdentp(Double.parseDouble(tmp.getHspPositive()));
            alignment.setEvalue(Double.parseDouble(tmp.getHspEvalue()));
            alignment.setBitscore(Double.parseDouble(tmp.getHspBitScore()));
            alignment.setSeqFrom(Integer.parseInt(tmp.getHspQueryFrom()));
            alignment.setSeqTo(Integer.parseInt(tmp.getHspQueryTo()));
            alignment.setPdbFrom(Integer.parseInt(tmp.getHspHitFrom()));
            alignment.setPdbTo(Integer.parseInt(tmp.getHspHitTo()));
            alignment.setSeqAlign(tmp.getHspQseq());
            alignment.setPdbAlign(tmp.getHspHseq());
            alignment.setMidlineAlign(tmp.getHspMidline());
        }
        return alignment;
    }
    
}
