package org.cbioportal.pdb_annotation.web.controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.cbioportal.pdb_annotation.scripts.PdbScriptsPipelineRunCommand;
import org.cbioportal.pdb_annotation.util.ReadConfig;
import org.cbioportal.pdb_annotation.web.domain.AlignmentRepository;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.BlastStatistics;
import org.cbioportal.pdb_annotation.web.models.CompleteAlignment;
import org.cbioportal.pdb_annotation.web.models.CompleteResidue;
import org.cbioportal.pdb_annotation.web.models.InputAlignment;
import org.cbioportal.pdb_annotation.web.models.InputSequence;
import org.cbioportal.pdb_annotation.web.models.ProteinSequenceAlignment;
import org.cbioportal.pdb_annotation.web.models.ProteinSequenceParam;
import org.cbioportal.pdb_annotation.web.models.ProteinSequenceParamResidue;
import org.cbioportal.pdb_annotation.web.models.ProteinSequenceResidue;
import org.cbioportal.pdb_annotation.web.models.ResidueMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Other Controllers, for detailed results of Alignments and ResidueMappings
 * 
 * @author wangjue
 *
 */
@RestController // shorthand for @Controller, @ResponseBody
// @CrossOrigin(origins = "*") // allow all cross-domain requests
// @Api(tags = "OtherInnerID", description = "Other")
// @RequestMapping(value = "/api/")
public class OtherContoller {

    final static Logger log = Logger.getLogger(OtherContoller.class);

    @Autowired
    private AlignmentRepository alignmentRepository;

    @RequestMapping(value = "/alignmentsComplex", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by Protein Sequence")
    public ProteinSequenceAlignment getPdbAlignmentBySequenceFull(HttpServletRequest request,
            @ApiParam(required = true, value = "Input Protein Sequence: ETGQSVNDPGNMSFVKETVDKLLKGYDIRLRPDFGGPP") @RequestParam String sequence,
            @ApiParam(required = false, value = "Default Blast Parameters:\n"
                    + " Evalue=1e-10,Wordsize=3,Gapopen=11,Gapextend=1,\n" + " Matrix=BLOSUM62,Comp_based_stats=2,\n"
                    + "Threshold=11,Windowsize=40") @RequestParam(required = false) List<String> paramList
    /*
     * @ApiParam(value = "Blast Parameters Evalue: (Default) 1e-10") String
     * evalue,
     * 
     * @ApiParam(value = "Blast Parameters Wordsize: (Default) 3") String
     * wordsize,
     * 
     * @ApiParam(value = "Blast Parameters Gapopen: (Default) 11") String
     * gapopen,
     * 
     * @ApiParam(value = "Blast Parameters Gapextend: (Default) 1") String
     * gapextend,
     * 
     * @ApiParam(value = "Blast Parameters Matrix: (Default) BLOSUM62") String
     * matrix,
     * 
     * @ApiParam(value = "Blast Parameters Comp_based_stats: (Default) 2")
     * String compbasedstats,
     * 
     * @ApiParam(value = "Blast Parameters Threshold: (Default) 11") String
     * threshold,
     * 
     * @ApiParam(value = "Blast Parameters Windowsize: (Default) 40") String
     * windowsize
     */) {

        InputSequence inputsequence = new InputSequence();
        inputsequence.setSequence(sequence);
        inputsequence.setResidueNumList(null);

        ReadConfig rc = ReadConfig.getInstance();

        // Use default first
        inputsequence.setEvalue(rc.getBlastEvalue());
        inputsequence.setWord_size(rc.getBlastWordsize());
        inputsequence.setGapopen(rc.getBlastGapopen());
        inputsequence.setGapextend(rc.getBlastGapextend());
        inputsequence.setMatrix(rc.getBlastMatrix());
        inputsequence.setComp_based_stats(rc.getBlastComp());
        inputsequence.setThreshold(rc.getBlastThreshold());
        inputsequence.setWindow_size(rc.getBlastWindowsize());

        // If have user defined input, use them
        for (String param : paramList) {
            String tmp[] = param.split("=");
            if (tmp[0].equals("Evalue")) {
                inputsequence.setEvalue(tmp[1]);
            } else if (tmp[0].equals("Wordsize")) {
                inputsequence.setWord_size(tmp[1]);
            } else if (tmp[0].equals("Gapopen")) {
                inputsequence.setGapopen(tmp[1]);
            } else if (tmp[0].equals("Gapextend")) {
                inputsequence.setGapextend(tmp[1]);
            } else if (tmp[0].equals("Matrix")) {
                inputsequence.setMatrix(tmp[1]);
            } else if (tmp[0].equals("Comp_based_stats")) {
                inputsequence.setComp_based_stats(tmp[1]);
            } else if (tmp[0].equals("Threshold")) {
                inputsequence.setThreshold(tmp[1]);
            } else if (tmp[0].equals("Windowsize")) {
                inputsequence.setWindow_size(tmp[1]);
            }
        }

        PdbScriptsPipelineRunCommand pdbScriptsPipelineRunCommand = new PdbScriptsPipelineRunCommand();
        List<InputAlignment> alignments = pdbScriptsPipelineRunCommand.runCommand(inputsequence);

        ProteinSequenceAlignment result = new ProteinSequenceAlignment();
        // Reset the return data structures
        ProteinSequenceParam param = new ProteinSequenceParam();
        param.setSequence(inputsequence.getSequence());

        // parameter
        param.setComp_based_stats(inputsequence.getComp_based_stats());
        param.setEvalue(inputsequence.getEvalue());
        param.setGapextend(inputsequence.getGapextend());
        param.setGapopen(inputsequence.getGapopen());
        param.setMatrix(inputsequence.getMatrix());
        param.setThreshold(inputsequence.getThreshold());
        param.setWindow_size(inputsequence.getWindow_size());
        param.setWord_size(inputsequence.getWord_size());
        result.setParam(param);

        // blast parameter
        BlastStatistics blastStat = new BlastStatistics();
        if (alignments.size() > 0) {
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
            re.setBitscore((float) ali.getBitscore());
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
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            Date today = Calendar.getInstance().getTime();
            re.setUpdateDate(df.format(today));

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
        result.setAlignment(residues);

        return result;
    }

    @RequestMapping(value = "/alignments/residueMappingComplex", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by Protein Sequence and Residue position")
    public ProteinSequenceResidue getPdbAlignmentReisudeBySequenceFull(
            @ApiParam(required = true, value = "Input Protein Sequence: ETGQSVNDPGNMSFVKETVDKLLKGYDIRLRPDFGGPP") @RequestParam String sequence,
            @ApiParam(required = false, value = "Input Residue Position e.g. 20") @RequestParam(required = false) List<String> positionList,
            @ApiParam(required = false, value = "Default Blast Parameters:\n"
                    + " Evalue=1e-10,Wordsize=3,Gapopen=11,Gapextend=1,\n" + " Matrix=BLOSUM62,Comp_based_stats=2,\n"
                    + "Threshold=11,Windowsize=40") @RequestParam(required = false) List<String> paramList
    /*
     * @ApiParam(value = "Blast Parameters Evalue: (Default) 1e-10") String
     * evalue,
     * 
     * @ApiParam(value = "Blast Parameters Wordsize: (Default) 3") String
     * wordsize,
     * 
     * @ApiParam(value = "Blast Parameters Gapopen: (Default) 11") String
     * gapopen,
     * 
     * @ApiParam(value = "Blast Parameters Gapextend: (Default) 1") String
     * gapextend,
     * 
     * @ApiParam(value = "Blast Parameters Matrix: (Default) BLOSUM62") String
     * matrix,
     * 
     * @ApiParam(value = "Blast Parameters Comp_based_stats: (Default) 2")
     * String compbasedstats,
     * 
     * @ApiParam(value = "Blast Parameters Threshold: (Default) 11") String
     * threshold,
     * 
     * @ApiParam(value = "Blast Parameters Windowsize: (Default) 40") String
     * windowsize
     */) {

        InputSequence inputsequence = new InputSequence();
        inputsequence.setSequence(sequence);
        inputsequence.setResidueNumList(positionList);

        ReadConfig rc = ReadConfig.getInstance();

        // Use default first
        inputsequence.setEvalue(rc.getBlastEvalue());
        inputsequence.setWord_size(rc.getBlastWordsize());
        inputsequence.setGapopen(rc.getBlastGapopen());
        inputsequence.setGapextend(rc.getBlastGapextend());
        inputsequence.setMatrix(rc.getBlastMatrix());
        inputsequence.setComp_based_stats(rc.getBlastComp());
        inputsequence.setThreshold(rc.getBlastThreshold());
        inputsequence.setWindow_size(rc.getBlastWindowsize());

        // If have user defined input, use them
        for (String param : paramList) {
            String tmp[] = param.split("=");
            if (tmp[0].equals("Evalue")) {
                inputsequence.setEvalue(tmp[1]);
            } else if (tmp[0].equals("Wordsize")) {
                inputsequence.setWord_size(tmp[1]);
            } else if (tmp[0].equals("Gapopen")) {
                inputsequence.setGapopen(tmp[1]);
            } else if (tmp[0].equals("Gapextend")) {
                inputsequence.setGapextend(tmp[1]);
            } else if (tmp[0].equals("Matrix")) {
                inputsequence.setMatrix(tmp[1]);
            } else if (tmp[0].equals("Comp_based_stats")) {
                inputsequence.setComp_based_stats(tmp[1]);
            } else if (tmp[0].equals("Threshold")) {
                inputsequence.setThreshold(tmp[1]);
            } else if (tmp[0].equals("Windowsize")) {
                inputsequence.setWindow_size(tmp[1]);
            }
        }

        PdbScriptsPipelineRunCommand pdbScriptsPipelineRunCommand = new PdbScriptsPipelineRunCommand();
        List<InputAlignment> alignments = pdbScriptsPipelineRunCommand.runCommand(inputsequence);

        ProteinSequenceResidue result = new ProteinSequenceResidue();
        // Reset the return data structures
        ProteinSequenceParamResidue param = new ProteinSequenceParamResidue();
        param.setSequence(inputsequence.getSequence());
        param.setResidueNameList(inputsequence.getResidueNameList());
        param.setResidueNumList(inputsequence.getResidueNumList());
        // parameter
        param.setComp_based_stats(inputsequence.getComp_based_stats());
        param.setEvalue(inputsequence.getEvalue());
        param.setGapextend(inputsequence.getGapextend());
        param.setGapopen(inputsequence.getGapopen());
        param.setMatrix(inputsequence.getMatrix());
        param.setThreshold(inputsequence.getThreshold());
        param.setWindow_size(inputsequence.getWindow_size());
        param.setWord_size(inputsequence.getWord_size());
        result.setParam(param);

        // blast parameter
        BlastStatistics blastStat = new BlastStatistics();
        if (alignments.size() > 0) {
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

        for (InputAlignment ali : alignments) {

            CompleteResidue rm = new CompleteResidue();

            rm.setAlignmentId(ali.getAlignmentId());
            rm.setBitscore((float) ali.getBitscore());
            rm.setChain(ali.getChain());
            rm.setSeqAlign(ali.getSeqAlign());
            rm.setSeqFrom(ali.getSeqFrom());
            rm.setSeqId(ali.getSeqId());
            rm.setSeqTo(ali.getSeqTo());
            rm.setSegStart(ali.getSegStart());
            rm.setEvalue(Double.toString(ali.getEvalue()));
            rm.setIdentity(ali.getIdentity());
            rm.setIdentityPositive(ali.getIdentp());
            rm.setMidlineAlign(ali.getMidlineAlign());
            rm.setPdbAlign(ali.getPdbAlign());
            rm.setPdbFrom(ali.getPdbFrom());
            rm.setPdbId(ali.getPdbId());
            rm.setPdbNo(ali.getPdbNo());
            rm.setPdbSeg(ali.getPdbSeg());
            rm.setPdbTo(ali.getPdbTo());
            DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            Date today = Calendar.getInstance().getTime();
            rm.setUpdateDate(df.format(today));

            List<ResidueMapping> residueMapping = new ArrayList<ResidueMapping>();
            for (String position : positionList) {
                int inputAA = Integer.parseInt(position);
                if (inputAA >= ali.getSeqFrom() && inputAA <= ali.getSeqTo()) {
                    ResidueMapping rp = new ResidueMapping();
                    rp.setQueryAminoAcid(sequence.substring(inputAA - 1, inputAA));
                    rp.setQueryPosition(inputAA);
                    rp.setPdbAminoAcid(
                            ali.getPdbAlign().substring(inputAA - ali.getSeqFrom(), inputAA - ali.getSeqFrom() + 1));
                    rp.setPdbPosition(
                            Integer.parseInt(ali.getSegStart()) - 1 + ali.getPdbFrom() + (inputAA - ali.getSeqFrom()));
                    // Withdraw if mapped to linker of the protein
                    if (!rp.getPdbAminoAcid().equals("X")) {
                        residueMapping.add(rp);
                    }
                }
            }

            rm.setResidueMapping(residueMapping);

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

            rm.setIdentityPercentage(String.format("%.2f", ali.getIdentity() * 1.0f / queryLength));
            rm.setPositivePercentage(String.format("%.2f", ali.getIdentp() * 1.0f / queryLength));
            rm.setGapPercentage(String.format("%.2f", gapLength * 1.0f / queryLength));
            rm.setGap(gapLength);
            rm.setLength(queryLength);

            residues.add(rm);

        }
        result.setResidues(residues);

        return result;
    }

    // TODO Now we are not use it
    // Query by AlignmentId, get all the Residue Mapping
    // @RequestMapping(value =
    // "/ResidueMappingFromAlignmentId/{alignmentId:.+}", method =
    // RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    // @ApiOperation("Get All Residue Mapping by AlignmentId")
    public List<ResidueMapping> getResidueMappingByAlignmentId(
            @ApiParam(required = true, value = "Input AlignmentId e.g. 883556") @PathVariable int alignmentId) {

        Alignment ali = alignmentRepository.findByAlignmentId(alignmentId);
        List<ResidueMapping> residueMapping = new ArrayList<ResidueMapping>();

        for (int i = ali.getSeqFrom(); i <= ali.getSeqTo(); i++) {
            ResidueMapping residue = new ResidueMapping();
            residue.setQueryPosition(i);
            residue.setQueryAminoAcid(ali.getSeqAlign().substring(i - ali.getSeqFrom(), i - ali.getSeqFrom() + 1));
            residue.setPdbPosition(Integer.parseInt(ali.getSegStart()) - 1 + ali.getPdbFrom() + (i - ali.getSeqFrom()));
            residue.setPdbAminoAcid(ali.getPdbAlign().substring(i - ali.getSeqFrom(), i - ali.getSeqFrom() + 1));
            residueMapping.add(residue);
        }
        return residueMapping;
    }

}
