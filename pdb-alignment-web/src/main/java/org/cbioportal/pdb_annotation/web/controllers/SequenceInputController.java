package org.cbioportal.pdb_annotation.web.controllers;


import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.cbioportal.pdb_annotation.scripts.PdbScriptsPipelineRunCommand;
import org.cbioportal.pdb_annotation.util.ReadConfig;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.BlastStatistics;
import org.cbioportal.pdb_annotation.web.models.CompleteAlignment;
import org.cbioportal.pdb_annotation.web.models.CompleteResidue;
import org.cbioportal.pdb_annotation.web.models.InputAlignment;
import org.cbioportal.pdb_annotation.web.models.InputResidue;
import org.cbioportal.pdb_annotation.web.models.InputSequence;
import org.cbioportal.pdb_annotation.web.models.ProteinSequenceAlignment;
import org.cbioportal.pdb_annotation.web.models.ProteinSequenceParam;
import org.cbioportal.pdb_annotation.web.models.ProteinSequenceParamResidue;
import org.cbioportal.pdb_annotation.web.models.ProteinSequenceResidue;
import org.cbioportal.pdb_annotation.web.models.Residue;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponses;

/**
 * Input the raw protein sequence as POST
 * 
 * @author wangjue
 *
 */

@RestController // shorthand for @Controller, @ResponseBody
@CrossOrigin(origins = "*") // allow all cross-domain requests
@Api(tags = "Protein Sequence", description = "Raw input")
@RequestMapping(value = "/g2s/")
public class SequenceInputController {
    
    /**
     * 
     * Keep this and re-implement the alignments and residues, after it completed, this function will be changed
     * 
     * @param sequence
     * @return
     */
    /*
    @RequestMapping(value = "/ProteinSequence/{sequence}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by Protein Sequence")
    public List<InputResidue> getPdbAlignmentBySequence(
            @ApiParam(required = true, value = "Input Protein Sequence: ETGQSVNDPGNMSFVKETVDKLLKGYDIRLRPDFGGPP") @PathVariable String sequence) {
        
        InputSequence inputsequence = new InputSequence();
        inputsequence.setSequence(sequence);
        
        inputsequence.setResidueNum("");
        
        //Add several default, may change it later
        inputsequence.setEvalue("1e-10");
        inputsequence.setWord_size("3");
        inputsequence.setGapopen("11");
        inputsequence.setGapextend("1");
        inputsequence.setMatrix("BLOSUM62");
        inputsequence.setComp_based_stats("2");
        inputsequence.setThreshold("11");
        inputsequence.setWindow_size("40");
        

        PdbScriptsPipelineRunCommand pdbScriptsPipelineRunCommand = new PdbScriptsPipelineRunCommand();
        List<InputAlignment> alignments = pdbScriptsPipelineRunCommand.runCommand(inputsequence);
        
        List<InputResidue> residues = new ArrayList<InputResidue>();
        int inputAA = 0;
        if (!inputsequence.getResidueNum().equals("")) {
            inputAA = Integer.parseInt(inputsequence.getResidueNum());
        }
        
        for (InputAlignment ali : alignments) {
            // if getResidueNum is empty, then return alignments
            // else, return residues
            if (inputsequence.getResidueNum().equals("")
                    || (inputAA >= ali.getSeqFrom() && inputAA <= ali.getSeqTo())) {
                InputResidue re = new InputResidue();
                re.setAlignmentId(ali.getAlignmentId());
                re.setBitscore(ali.getBitscore());
                re.setChain(ali.getChain());
                re.setSeqAlign(ali.getSeqAlign());
                re.setSeqFrom(ali.getSeqFrom());
                re.setSeqId(ali.getSeqId());
                re.setSeqTo(ali.getSeqTo());
                re.setSegStart(ali.getSegStart());
                re.setEvalue(ali.getEvalue());
                re.setIdentity(ali.getIdentity());
                re.setIdentp(ali.getIdentp());
                re.setMidlineAlign(ali.getMidlineAlign());
                re.setPdbAlign(ali.getPdbAlign());
                re.setPdbFrom(ali.getPdbFrom());
                re.setPdbId(ali.getPdbId());
                re.setPdbNo(ali.getPdbNo());
                re.setPdbSeg(ali.getPdbSeg());
                re.setPdbTo(ali.getPdbTo());
                if (!(inputsequence.getResidueNum().equals(""))) {
                    re.setResidueName(
                            ali.getPdbAlign().substring(inputAA - ali.getSeqFrom(), inputAA - ali.getSeqFrom() + 1));
                    re.setResidueNum(new Integer(
                            Integer.parseInt(ali.getSegStart()) - 1 + ali.getPdbFrom() + (inputAA - ali.getSeqFrom()))
                                    .toString());
                }
                // For percentage
                int queryLength = ali.getSeqAlign().length();
                int targetLength = ali.getPdbAlign().length();
                int queryGapLength = StringUtils.countMatches(ali.getSeqAlign(), "-");
                int targetGapLength = StringUtils.countMatches(ali.getPdbAlign(), "-");
                int gapLength = Math.abs(queryGapLength - targetGapLength);

                // Test:
                if (queryLength != targetLength) {
                    System.out.println("Error! in " + ali.getPdbNo());
                }

                re.setIdentityPercentage(String.format("%.2f", ali.getIdentity() * 1.0f / queryLength));
                re.setPositivePercentage(String.format("%.2f", ali.getIdentp() * 1.0f / queryLength));
                re.setGapPercentage(String.format("%.2f", gapLength * 1.0f / queryLength));
                re.setGap(gapLength);
                re.setLength(queryLength);
                re.setIdentityPercentageStr("(" + ali.getIdentity() + "/" + queryLength + ")");
                re.setPositivePercentageStr("(" + ali.getIdentp() + "/" + queryLength + ")");
                re.setGapPercentageStr("(" + gapLength + "/" + queryLength + ")");

                // Parameters for output TODO: not optimize
                re.setParaEvalue(inputsequence.getEvalue());
                re.setWord_size(inputsequence.getWord_size());
                re.setGapopen(inputsequence.getGapopen());
                re.setGapextend(inputsequence.getGapextend());
                re.setMatrix(inputsequence.getMatrix());
                re.setComp_based_stats(inputsequence.getComp_based_stats());
                re.setThreshold(inputsequence.getThreshold());
                re.setWindow_size(inputsequence.getWindow_size());

                re.setBlast_dblen(ali.getBlast_dblen());
                re.setBlast_dbnum(ali.getBlast_dbnum());
                re.setBlast_effspace(ali.getBlast_effspace());
                re.setBlast_entropy(ali.getBlast_entropy());
                re.setBlast_hsplen(ali.getBlast_hsplen());
                re.setBlast_kappa(ali.getBlast_kappa());
                re.setBlast_lambda(ali.getBlast_lambda());
                re.setBlast_reference(ali.getBlast_reference());
                re.setBlast_version(ali.getBlast_version());

                // input
                re.setSequence(inputsequence.getSequence());
                re.setInputResidueNum(inputsequence.getResidueNum());

                residues.add(re);
            }
        }
               
        return residues;
    }
    */
    
    
    @RequestMapping(value = "/ProteinSequenceAlignment/{sequence}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by Protein Sequence")
    public ProteinSequenceAlignment getPdbAlignmentBySequence(
            HttpServletRequest request,
            @ApiParam(required = true, value = "Input Protein Sequence: ETGQSVNDPGNMSFVKETVDKLLKGYDIRLRPDFGGPP") @PathVariable String sequence,
            //@ApiParam(value = "1e-10") @RequestParam String evalue
            @ApiParam(value = "Blast Parameters Evalue: (Default) 1e-10") String evalue,
            @ApiParam(value = "Blast Parameters Wordsize: (Default) 3") String wordsize,
            @ApiParam(value = "Blast Parameters Gapopen: (Default) 11") String gapopen,
            @ApiParam(value = "Blast Parameters Gapextend: (Default) 1") String gapextend,
            @ApiParam(value = "Blast Parameters Matrix: (Default) BLOSUM62") String matrix,
            @ApiParam(value = "Blast Parameters Comp_based_stats: (Default) 2") String compbasedstats,
            @ApiParam(value = "Blast Parameters Threshold: (Default) 11") String threshold,
            @ApiParam(value = "Blast Parameters Windowsize: (Default) 40") String windowsize
            ) {
        
        InputSequence inputsequence = new InputSequence();
        inputsequence.setSequence(sequence);        
        inputsequence.setResidueNum("");
        
        ReadConfig rc = ReadConfig.getInstance();
        
        //If no input, use default data
        if(evalue == null){
            inputsequence.setEvalue(rc.getBlastEvalue());
        }else{
            inputsequence.setEvalue(evalue);
        }
        if(wordsize == null){
            inputsequence.setWord_size(rc.getBlastWordsize());
        }else{
            inputsequence.setWord_size(wordsize);
        }
        if(gapopen == null){
            inputsequence.setGapopen(rc.getBlastGapopen());
        }else{
            inputsequence.setGapopen(gapopen);
        }
        if(gapextend == null){
            inputsequence.setGapextend(rc.getBlastGapextend());
        }else{
            inputsequence.setGapextend(gapextend);
        }
        if(matrix == null){
            inputsequence.setMatrix(rc.getBlastMatrix());
        }else{
            inputsequence.setMatrix(matrix);
        }
        if(compbasedstats == null){
            inputsequence.setComp_based_stats(rc.getBlastComp());
        }else{
            inputsequence.setComp_based_stats(compbasedstats);
        }
        if(threshold == null){
            inputsequence.setThreshold(rc.getBlastThreshold());
        }else{
            inputsequence.setThreshold(threshold);
        }
        if(windowsize == null){
            inputsequence.setWindow_size(rc.getBlastWindowsize());
        }else{
            inputsequence.setWindow_size(windowsize);
        }

        
        PdbScriptsPipelineRunCommand pdbScriptsPipelineRunCommand = new PdbScriptsPipelineRunCommand();
        List<InputAlignment> alignments = pdbScriptsPipelineRunCommand.runCommand(inputsequence);
        
        ProteinSequenceAlignment result = new ProteinSequenceAlignment();
        //Reset the return data structures
        ProteinSequenceParam param = new ProteinSequenceParam();
        param.setSequence(inputsequence.getSequence());
        
        //parameter
        param.setComp_based_stats(inputsequence.getComp_based_stats());
        param.setEvalue(inputsequence.getEvalue());
        param.setGapextend(inputsequence.getGapextend());
        param.setGapopen(inputsequence.getGapopen());
        param.setMatrix(inputsequence.getMatrix());
        param.setThreshold(inputsequence.getThreshold());
        param.setWindow_size(inputsequence.getWindow_size());
        param.setWord_size(inputsequence.getWord_size());
        result.setParam(param);
        
        //blast parameter
        BlastStatistics blastStat = new BlastStatistics();
        if(alignments.size()>0){
            InputAlignment tmp = alignments.get(0);
            
            blastStat.setBlast_dblen(tmp.getBlast_dblen());
            blastStat.setBlast_dbnum(tmp.getBlast_dbnum());
            blastStat.setBlast_effspace(tmp.getBlast_effspace());
            blastStat.setBlast_entropy(tmp.getBlast_entropy());
            blastStat.setBlast_hsplen(tmp.getBlast_hsplen());
            blastStat.setBlast_kappa(tmp.getBlast_kappa());
            blastStat.setBlast_lambda(tmp.getBlast_lambda());
            blastStat.setBlast_reference(tmp.getBlast_reference());
            blastStat.setBlast_version(tmp.getBlast_version());
        }
        result.setBlastStat(blastStat);
        
               
        
        List<CompleteAlignment> residues = new ArrayList<CompleteAlignment>();             
        
        for (InputAlignment ali : alignments) {
                CompleteAlignment re = new CompleteAlignment();
                re.setAlignmentId(ali.getAlignmentId());
                re.setBitscore((float)ali.getBitscore());
                re.setChain(ali.getChain());
                re.setSeqAlign(ali.getSeqAlign());
                re.setSeqFrom(ali.getSeqFrom());
                re.setSeqId(ali.getSeqId());
                re.setSeqTo(ali.getSeqTo());
                re.setSegStart(ali.getSegStart());
                re.setEvalue(Double.toString(ali.getEvalue()));
                re.setIdentity(ali.getIdentity());
                re.setIdentityPositive(ali.getIdentp());
                re.setMidlineAlign(ali.getMidlineAlign());
                re.setPdbAlign(ali.getPdbAlign());
                re.setPdbFrom(ali.getPdbFrom());
                re.setPdbId(ali.getPdbId());
                re.setPdbNo(ali.getPdbNo());
                re.setPdbSeg(ali.getPdbSeg());
                re.setPdbTo(ali.getPdbTo());
                
                // For percentage
                int queryLength = ali.getSeqAlign().length();
                int targetLength = ali.getPdbAlign().length();
                int queryGapLength = StringUtils.countMatches(ali.getSeqAlign(), "-");
                int targetGapLength = StringUtils.countMatches(ali.getPdbAlign(), "-");
                int gapLength = Math.abs(queryGapLength - targetGapLength);

                // Test:
                if (queryLength != targetLength) {
                    System.out.println("Error! in " + ali.getPdbNo());
                }

                re.setIdentityPercentage(String.format("%.2f", ali.getIdentity() * 1.0f / queryLength));
                re.setPositivePercentage(String.format("%.2f", ali.getIdentp() * 1.0f / queryLength));
                re.setGapPercentage(String.format("%.2f", gapLength * 1.0f / queryLength));
                re.setGap(gapLength);
                re.setLength(queryLength);
           
                residues.add(re);
            
        }
        result.setAlignment(residues);;
               
        return result;
    }
    
    
    
    @RequestMapping(value = "/ProteinSequenceAlignmentResidue/{sequence}/{position}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by Protein Sequence and Residue position")
    public ProteinSequenceResidue getPdbAlignmentReisudeBySequence(
            @ApiParam(required = true, value = "Input Protein Sequence: ETGQSVNDPGNMSFVKETVDKLLKGYDIRLRPDFGGPP") @PathVariable String sequence,
            @ApiParam(required = true, value = "Input Residue Position e.g. 20") @PathVariable String position,
            @ApiParam(value = "Blast Parameters Evalue: (Default) 1e-10") String evalue,
            @ApiParam(value = "Blast Parameters Wordsize: (Default) 3") String wordsize,
            @ApiParam(value = "Blast Parameters Gapopen: (Default) 11") String gapopen,
            @ApiParam(value = "Blast Parameters Gapextend: (Default) 1") String gapextend,
            @ApiParam(value = "Blast Parameters Matrix: (Default) BLOSUM62") String matrix,
            @ApiParam(value = "Blast Parameters Comp_based_stats: (Default) 2") String compbasedstats,
            @ApiParam(value = "Blast Parameters Threshold: (Default) 11") String threshold,
            @ApiParam(value = "Blast Parameters Windowsize: (Default) 40") String windowsize
            ) {
        
        InputSequence inputsequence = new InputSequence();
        inputsequence.setSequence(sequence);        
        inputsequence.setResidueNum(position);
        
        
        
        ReadConfig rc = ReadConfig.getInstance();
        
        //If no input, use default data
        if(evalue == null){
            inputsequence.setEvalue(rc.getBlastEvalue());
        }else{
            inputsequence.setEvalue(evalue);
        }
        if(wordsize == null){
            inputsequence.setWord_size(rc.getBlastWordsize());
        }else{
            inputsequence.setWord_size(wordsize);
        }
        if(gapopen == null){
            inputsequence.setGapopen(rc.getBlastGapopen());
        }else{
            inputsequence.setGapopen(gapopen);
        }
        if(gapextend == null){
            inputsequence.setGapextend(rc.getBlastGapextend());
        }else{
            inputsequence.setGapextend(gapextend);
        }
        if(matrix == null){
            inputsequence.setMatrix(rc.getBlastMatrix());
        }else{
            inputsequence.setMatrix(matrix);
        }
        if(compbasedstats == null){
            inputsequence.setComp_based_stats(rc.getBlastComp());
        }else{
            inputsequence.setComp_based_stats(compbasedstats);
        }
        if(threshold == null){
            inputsequence.setThreshold(rc.getBlastThreshold());
        }else{
            inputsequence.setThreshold(threshold);
        }
        if(windowsize == null){
            inputsequence.setWindow_size(rc.getBlastWindowsize());
        }else{
            inputsequence.setWindow_size(windowsize);
        }
        
        
        PdbScriptsPipelineRunCommand pdbScriptsPipelineRunCommand = new PdbScriptsPipelineRunCommand();
        List<InputAlignment> alignments = pdbScriptsPipelineRunCommand.runCommand(inputsequence);
        
        ProteinSequenceResidue result = new ProteinSequenceResidue();
        //Reset the return data structures
        ProteinSequenceParamResidue param = new ProteinSequenceParamResidue();
        param.setSequence(inputsequence.getSequence());
        param.setResidueName(inputsequence.getResidueName());
        param.setResidueNum(inputsequence.getResidueNum());
        //parameter
        param.setComp_based_stats(inputsequence.getComp_based_stats());
        param.setEvalue(inputsequence.getEvalue());
        param.setGapextend(inputsequence.getGapextend());
        param.setGapopen(inputsequence.getGapopen());
        param.setMatrix(inputsequence.getMatrix());
        param.setThreshold(inputsequence.getThreshold());
        param.setWindow_size(inputsequence.getWindow_size());
        param.setWord_size(inputsequence.getWord_size());
        result.setParam(param);
        
        //blast parameter
        BlastStatistics blastStat = new BlastStatistics();
        if(alignments.size()>0){
            InputAlignment tmp = alignments.get(0);
            
            blastStat.setBlast_dblen(tmp.getBlast_dblen());
            blastStat.setBlast_dbnum(tmp.getBlast_dbnum());
            blastStat.setBlast_effspace(tmp.getBlast_effspace());
            blastStat.setBlast_entropy(tmp.getBlast_entropy());
            blastStat.setBlast_hsplen(tmp.getBlast_hsplen());
            blastStat.setBlast_kappa(tmp.getBlast_kappa());
            blastStat.setBlast_lambda(tmp.getBlast_lambda());
            blastStat.setBlast_reference(tmp.getBlast_reference());
            blastStat.setBlast_version(tmp.getBlast_version());
        }
        result.setBlastStat(blastStat);
        
               
        
        List<CompleteResidue> residues = new ArrayList<CompleteResidue>();       
        int inputAA = Integer.parseInt(position);       
        
        for (InputAlignment ali : alignments) {
            if (inputAA >= ali.getSeqFrom() && inputAA <= ali.getSeqTo()) {
                CompleteResidue re = new CompleteResidue();
                re.setAlignmentId(ali.getAlignmentId());
                re.setBitscore((float)ali.getBitscore());
                re.setChain(ali.getChain());
                re.setSeqAlign(ali.getSeqAlign());
                re.setSeqFrom(ali.getSeqFrom());
                re.setSeqId(ali.getSeqId());
                re.setSeqTo(ali.getSeqTo());
                re.setSegStart(ali.getSegStart());
                re.setEvalue(Double.toString(ali.getEvalue()));
                re.setIdentity(ali.getIdentity());
                re.setIdentityPositive(ali.getIdentp());
                re.setMidlineAlign(ali.getMidlineAlign());
                re.setPdbAlign(ali.getPdbAlign());
                re.setPdbFrom(ali.getPdbFrom());
                re.setPdbId(ali.getPdbId());
                re.setPdbNo(ali.getPdbNo());
                re.setPdbSeg(ali.getPdbSeg());
                re.setPdbTo(ali.getPdbTo());
                
                re.setResidueName(
                            ali.getPdbAlign().substring(inputAA - ali.getSeqFrom(), inputAA - ali.getSeqFrom() + 1));
                re.setResidueNum(
                            Integer.parseInt(ali.getSegStart()) - 1 + ali.getPdbFrom() + (inputAA - ali.getSeqFrom()));
                
                // For percentage
                int queryLength = ali.getSeqAlign().length();
                int targetLength = ali.getPdbAlign().length();
                int queryGapLength = StringUtils.countMatches(ali.getSeqAlign(), "-");
                int targetGapLength = StringUtils.countMatches(ali.getPdbAlign(), "-");
                int gapLength = Math.abs(queryGapLength - targetGapLength);

                // Test:
                if (queryLength != targetLength) {
                    System.out.println("Error! in " + ali.getPdbNo());
                }

                re.setIdentityPercentage(String.format("%.2f", ali.getIdentity() * 1.0f / queryLength));
                re.setPositivePercentage(String.format("%.2f", ali.getIdentp() * 1.0f / queryLength));
                re.setGapPercentage(String.format("%.2f", gapLength * 1.0f / queryLength));
                re.setGap(gapLength);
                re.setLength(queryLength);
           
                residues.add(re);
            }
        }
        result.setResidues(residues);
               
        return result;
    }
    
    


    
}
