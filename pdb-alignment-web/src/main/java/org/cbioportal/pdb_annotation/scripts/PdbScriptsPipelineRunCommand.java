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
import org.cbioportal.pdb_annotation.util.blast.Statistics;
import org.cbioportal.pdb_annotation.web.models.InputAlignment;
import org.cbioportal.pdb_annotation.web.models.InputSequence;

/**
 * Shell-based command running
 *
 * @author Juexin Wang
 *
 */

public class PdbScriptsPipelineRunCommand {
    final static Logger log = Logger.getLogger(PdbScriptsPipelineRunCommand.class);
    private static final String HTTP_AGENT_PROPERTY_VALUE = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
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
    public List<InputAlignment> runCommand(InputSequence inputsequence) {

        CommandProcessUtil cu = new CommandProcessUtil();
        ReadConfig rc = ReadConfig.getInstance();
        this.db = new BlastDataBase(ReadConfig.pdbSeqresFastaFile);

        ArrayList<String> paralist = new ArrayList<String>();

        webInput2File(inputsequence);

        // blastp input genes against pdb
        paralist = new ArrayList<String>();
        paralist.add(ReadConfig.uploaddir + inputsequence.getId() + ".fasta");
        paralist.add(ReadConfig.uploaddir + this.db.resultfileName);
        paralist.add(ReadConfig.workspace + this.db.dbName);
        cu.runCommand("blastp", paralist, inputsequence);

        // parse results and output results
        List<InputAlignment> outresults = parseblastresultsSingle(ReadConfig.uploaddir);

        // Clean Up
        paralist = new ArrayList<String>();
        paralist.add(ReadConfig.uploaddir + inputsequence.getId() + ".fasta");
        cu.runCommand("rm", paralist, inputsequence);

        paralist = new ArrayList<String>();
        paralist.add(ReadConfig.uploaddir + this.db.resultfileName);
        cu.runCommand("rm", paralist, inputsequence);
        return outresults;
    }

    /**
     * 
     * @param inputsequence
     * @return
     */
    public void webInput2File(InputSequence inputsequence) {
        try {
            FileUtils.writeStringToFile(new File(ReadConfig.uploaddir + inputsequence.getId() + ".fasta"),
                    inputsequence.getSequence());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Parse one single file of blast results to list of String, time and memory
     * consuming for huge files
     * 
     * @return List<BlastResult>
     */
    public List<InputAlignment> parseblastresultsSingle(String currentDir) {
        System.setProperty("javax.xml.accessExternalDTD", "all");
        System.setProperty("http.agent", HTTP_AGENT_PROPERTY_VALUE); // http.agent
                                                                     // is
                                                                     // needed
                                                                     // to fetch
                                                                     // dtd from
                                                                     // some
                                                                     // servers
        List<InputAlignment> results = new ArrayList<InputAlignment>();
        try {
            log.info("[BLAST] Read blast results from xml file...");
            File blastresults = new File(currentDir + this.db.resultfileName);
            JAXBContext jc = JAXBContext.newInstance("org.cbioportal.pdb_annotation.util.blast");
            Unmarshaller u = jc.createUnmarshaller();
            u.setSchema(null);
            int count = 1;
            BlastOutput blast = (BlastOutput) u.unmarshal(blastresults);
            BlastOutputIterations iterations = blast.getBlastOutputIterations();
            for (Iteration iteration : iterations.getIteration()) {
                String querytext = iteration.getIterationQueryDef();
                IterationHits hits = iteration.getIterationHits();
                Statistics stat = iteration.getIterationStat().getStatistics();
                for (Hit hit : hits.getHit()) {

                    // Old: only select Best for one PDB
                    /*
                     * Alignment alignment = parseSingleAlignmentBest(querytext,
                     * hit);
                     * alignment.setBlast_dblen(stat.getStatisticsDbLen());
                     * alignment.setBlast_dbnum(stat.getStatisticsDbNum());
                     * alignment.setBlast_effspace(stat.getStatisticsEffSpace())
                     * ;
                     * alignment.setBlast_entropy(stat.getStatisticsEntropy());
                     * alignment.setBlast_hsplen(stat.getStatisticsHspLen());
                     * alignment.setBlast_kappa(stat.getStatisticsKappa());
                     * alignment.setBlast_lambda(stat.getStatisticsLambda());
                     * alignment.setBlast_reference(blast.
                     * getBlastOutputReference());
                     * alignment.setBlast_version(blast.getBlastOutputVersion())
                     * ; results.add(alignment);
                     */

                    // Select all for one PDB
                    results.addAll(parseSingleAlignment(querytext, hit, stat, blast, count));
                    count = results.size() + 1;
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
     * Parse XML structure into Object Alignment, only get best alignment for
     * one PDB
     * 
     * @param querytext
     * @param hit
     * @param count
     * @return
     */
    public InputAlignment parseSingleAlignmentBest(String querytext, Hit hit) {
        InputAlignment alignment = new InputAlignment();

        alignment.setSeqId(querytext.split("\\s+")[0]);
        alignment.setPdbNo(hit.getHitDef().split("\\s+")[0]);
        alignment.setPdbId(hit.getHitDef().split("\\s+")[0].split("_")[0]);
        alignment.setChain(hit.getHitDef().split("\\s+")[0].split("_")[1]);
        alignment.setPdbSeg(hit.getHitDef().split("\\s+")[0].split("_")[2]);
        alignment.setSegStart(hit.getHitDef().split("\\s+")[3]);

        List<Hsp> tlist = hit.getHitHsps().getHsp();
        Hsp tmp = tlist.get(0);
        alignment.setIdentity(Integer.parseInt(tmp.getHspIdentity()));
        alignment.setIdentp(Integer.parseInt(tmp.getHspPositive()));
        alignment.setEvalue(Double.parseDouble(tmp.getHspEvalue()));
        alignment.setBitscore(Double.parseDouble(tmp.getHspBitScore()));
        alignment.setSeqFrom(Integer.parseInt(tmp.getHspQueryFrom()));
        alignment.setSeqTo(Integer.parseInt(tmp.getHspQueryTo()));
        alignment.setPdbFrom(Integer.parseInt(tmp.getHspHitFrom()));
        alignment.setPdbTo(Integer.parseInt(tmp.getHspHitTo()));
        alignment.setSeqAlign(tmp.getHspQseq());
        alignment.setPdbAlign(tmp.getHspHseq());
        alignment.setMidlineAlign(tmp.getHspMidline());

        return alignment;
    }

    /**
     * 
     * Choose all alignments Parse XML structure into Object Alignment,
     * 
     * @param querytext
     * @param hit
     * @return
     */
    public List<InputAlignment> parseSingleAlignment(String querytext, Hit hit, Statistics stat, BlastOutput blast,
            int count) {
        List<InputAlignment> alignments = new ArrayList<InputAlignment>();

        List<Hsp> tlist = hit.getHitHsps().getHsp();
        for (Hsp tmp : tlist) {
            InputAlignment alignment = new InputAlignment(count);

            alignment.setSeqId(querytext.split("\\s+")[0]);
            alignment.setPdbNo(hit.getHitDef().split("\\s+")[0]);
            alignment.setPdbId(hit.getHitDef().split("\\s+")[0].split("_")[0]);
            alignment.setChain(hit.getHitDef().split("\\s+")[0].split("_")[1]);
            alignment.setPdbSeg(hit.getHitDef().split("\\s+")[0].split("_")[2]);
            alignment.setSegStart(hit.getHitDef().split("\\s+")[3]);

            // results;
            alignment.setIdentity(Integer.parseInt(tmp.getHspIdentity()));
            alignment.setIdentp(Integer.parseInt(tmp.getHspPositive()));
            alignment.setEvalue(Double.parseDouble(tmp.getHspEvalue()));
            alignment.setBitscore(Double.parseDouble(tmp.getHspBitScore()));
            alignment.setSeqFrom(Integer.parseInt(tmp.getHspQueryFrom()));
            alignment.setSeqTo(Integer.parseInt(tmp.getHspQueryTo()));
            alignment.setPdbFrom(Integer.parseInt(tmp.getHspHitFrom()));
            alignment.setPdbTo(Integer.parseInt(tmp.getHspHitTo()));
            alignment.setSeqAlign(tmp.getHspQseq());
            alignment.setPdbAlign(tmp.getHspHseq());
            alignment.setMidlineAlign(tmp.getHspMidline());

            alignment.setBlast_dblen(stat.getStatisticsDbLen());
            alignment.setBlast_dbnum(stat.getStatisticsDbNum());
            alignment.setBlast_effspace(stat.getStatisticsEffSpace());
            alignment.setBlast_entropy(stat.getStatisticsEntropy());
            alignment.setBlast_hsplen(stat.getStatisticsHspLen());
            alignment.setBlast_kappa(stat.getStatisticsKappa());
            alignment.setBlast_lambda(stat.getStatisticsLambda());
            alignment.setBlast_reference(blast.getBlastOutputReference());
            alignment.setBlast_version(blast.getBlastOutputVersion());

            alignments.add(alignment);
            count++;
        }

        return alignments;
    }

}
