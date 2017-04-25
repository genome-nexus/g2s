package org.cbioportal.pdb_annotation.web.controllers;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.pdb_annotation.web.domain.AlignmentRepository;
import org.cbioportal.pdb_annotation.web.domain.GeneSequenceRepository;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.Residue;
import org.cbioportal.pdb_annotation.web.models.ResiduePresent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 *
 * Controller of the API: Input Protein SeqId
 *
 * @author Juexin Wang
 *
 */
@RestController // shorthand for @Controller, @ResponseBody
@CrossOrigin(origins = "*") // allow all cross-domain requests
@Api(tags = "QueryInnerID", description = "Inner ID")
@RequestMapping(value = "/g2s/")
public class SeqIdAlignmentController {

    @Autowired
    private AlignmentRepository alignmentRepository;
    @Autowired
    private GeneSequenceRepository geneSequenceRepository;

    @RequestMapping(value = "/GeneSeqStructureMapping/{seqId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by Protein SeqId")
    public List<Alignment> getPdbAlignmentByGeneSequenceId(
            @ApiParam(required = true, value = "Input SeqId e.g. 25625") @PathVariable String seqId) {
        return alignmentRepository.findBySeqId(seqId);
    }

    // Query from EnsemblId
    @ApiOperation(value = "Get PDB Alignments by EnsemblId", nickname = "EnsemblStructureMappingQuery")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Alignment.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/EnsemblStructureMappingQueryT", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public String getPdbAlignmentByEnsemblId(
            @RequestParam @ApiParam(value = "Input Ensembl Id e.g. ENSP00000489609.1", required = true, allowMultiple = true) String ensemblId) {
        return ensemblId + "SS";
    }

    @RequestMapping(value = "/GeneSeqStructureMapping/{seqId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by Protein SeqId")
    public List<Alignment> getPdbAlignmentByGeneSequenceIdPOST(
            @ApiParam(required = true, value = "Input SeqId e.g. 25625") @PathVariable String seqId) {
        return alignmentRepository.findBySeqId(seqId);
    }

    @RequestMapping(value = "/GeneSeqRecognition/{seqId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Whether Protein SeqId Exists")
    public boolean getExistedSeqIdinAlignment(
            @ApiParam(required = true, value = "Input SeqId e.g. 25625") @PathVariable String seqId) {
        return geneSequenceRepository.findBySeqId(seqId).size() != 0;
    }

    @RequestMapping(value = "/GeneSeqResidueMapping/{seqId}/{position}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get Residue Mapping by Protein SeqId and Residue Position")
    public List<Residue> getPdbResidueBySeqId(
            @ApiParam(required = true, value = "Input SeqId e.g. 25625") @PathVariable String seqId,
            @ApiParam(required = true, value = "Input Residue Position e.g. 100") @PathVariable String position) {
        List<Alignment> it = alignmentRepository.findBySeqId(seqId);
        List<Residue> outit = new ArrayList<Residue>();
        int inputAA = Integer.parseInt(position);
        for (Alignment ali : it) {
            if (inputAA >= ali.getSeqFrom() && inputAA <= ali.getSeqTo()) {
                Residue re = new Residue();
                re.setAlignmentId(ali.getAlignmentId());
                re.setBitscore(ali.getBitscore());
                re.setChain(ali.getChain());
                re.setSegStart(ali.getSegStart());
                re.setSeqAlign(ali.getSeqAlign());
                re.setSeqFrom(ali.getSeqFrom());
                re.setSeqId(ali.getSeqId());
                re.setSeqTo(ali.getSeqTo());
                re.setEvalue(ali.getEvalue());
                re.setIdentity(ali.getIdentity());
                re.setIdentityPositive(ali.getIdentityPositive());
                re.setMidlineAlign(ali.getMidlineAlign());
                re.setPdbAlign(ali.getPdbAlign());
                re.setPdbFrom(ali.getPdbFrom());
                re.setPdbId(ali.getPdbId());
                re.setPdbNo(ali.getPdbNo());
                re.setPdbSeg(ali.getPdbSeg());
                re.setPdbTo(ali.getPdbTo());
                re.setUpdateDate(ali.getUpdateDate());
                re.setResidueName(
                        ali.getPdbAlign().substring(inputAA - ali.getSeqFrom(), inputAA - ali.getSeqFrom() + 1));
                re.setResidueNum(
                        Integer.parseInt(ali.getSegStart()) - 1 + ali.getPdbFrom() + (inputAA - ali.getSeqFrom()));
                outit.add(re);
            }
        }
        return outit;
    }

    // Query by AlignmentId, get all the Residue Mapping
    @RequestMapping(value = "/ResidueMappingFromAlignmentId/{alignmentId:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get All Residue Mapping by AlignmentId")
    public List<ResiduePresent> getResidueMappingByAlignmentId(
            @ApiParam(required = true, value = "Input AlignmentId e.g. 883556") @PathVariable int alignmentId) {

        Alignment ali = alignmentRepository.findByAlignmentId(alignmentId);
        List<ResiduePresent> residueList = new ArrayList<ResiduePresent>();

        for (int i = ali.getSeqFrom(); i <= ali.getSeqTo(); i++) {
            ResiduePresent residue = new ResiduePresent();
            residue.setInputNum(i);
            residue.setInputName(ali.getSeqAlign().substring(i - ali.getSeqFrom(), i - ali.getSeqFrom() + 1));
            residue.setResidueNum(Integer.parseInt(ali.getSegStart()) - 1 + ali.getPdbFrom() + (i - ali.getSeqFrom()));
            residue.setResidueName(ali.getPdbAlign().substring(i - ali.getSeqFrom(), i - ali.getSeqFrom() + 1));
            residueList.add(residue);
        }
        return residueList;
    }

}
