package org.cbioportal.pdb_annotation.web.controllers;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.pdb_annotation.web.domain.AlignmentRepository;
import org.cbioportal.pdb_annotation.web.domain.GeneSequenceRepository;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.Residue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@Api(tags = "Protein SeqId", description = "Inner ID")
@RequestMapping(value = "/g2s/")
public class SeqIdAlignmentController {
    
    @Autowired
    private AlignmentRepository alignmentRepository;
    @Autowired
    private GeneSequenceRepository geneSequenceRepository;
    
    
    //Query from seqId
    @ApiOperation(value = "Get PDB Alignments by Protein SeqId", nickname = "getPdbAlignmentByGeneSequenceId")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Alignment.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/GeneSeqStructureMappingQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<Alignment> getPdbAlignmentByGeneSequenceId (
            @RequestParam @ApiParam(value = "Input SeqId e.g. 1233", required = true, allowMultiple = true) String seqId) {
        return alignmentRepository.findBySeqId(seqId);
    }

    @ApiOperation(value = "Whether Protein SeqId Exists", nickname = "getExistedSeqIdinAlignment")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", responseContainer = "boolean"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/GeneSeqRecognitionQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public boolean getExistedSeqIdinAlignment(
            @RequestParam @ApiParam(value = "Input SeqId e.g. 1233", required = true, allowMultiple = true) String seqId) {
        return geneSequenceRepository.findBySeqId(seqId).size() != 0;
    }
    
    
    @ApiOperation(value = "Get Residue Mapping by Protein SeqId and Residue Position", nickname = "getPdbResidueBySeqId")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Residue.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/GeneSeqResidueMappingQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<Residue> getPdbResidueBySeqId (
            @RequestParam(required = true)
            @ApiParam(value = "Input SeqId e.g. 1233", required = true, allowMultiple = true) String seqId, 
            @RequestParam(required = true)
            @ApiParam(value = "Input Residue Position e.g. 42", required = true, allowMultiple = true) String aaPosition) {
        List<Alignment> it = alignmentRepository.findBySeqId(seqId);
        List<Residue> outit = new ArrayList<Residue> ();
        int inputAA = Integer.parseInt(aaPosition);
        for(Alignment ali:it){
            if(inputAA>=ali.getSeqFrom() && inputAA<=ali.getSeqTo()){
                Residue re = new Residue(); 
                re.setAlignmentId(ali.getAlignmentId());
                re.setBitscore(ali.getBitscore());
                re.setChain(ali.getChain());
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
                re.setResidueName(ali.getPdbAlign().substring(inputAA-ali.getSeqFrom(), inputAA-ali.getSeqFrom()+1));
                re.setResidueNum(ali.getPdbFrom()+(inputAA-ali.getSeqFrom())); 
                outit.add(re);
            }
        }
        return outit;
    }

}
