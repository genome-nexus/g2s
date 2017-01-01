package org.cbioportal.pdb_annotation.web.controllers;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.pdb_annotation.web.domain.AlignmentRepository;
import org.cbioportal.pdb_annotation.web.domain.GeneSequenceRepository;
import org.cbioportal.pdb_annotation.web.domain.UniprotRepository;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.Residue;
import org.cbioportal.pdb_annotation.web.models.Uniprot;
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
* Controller of the API: Input UniprotID
*
* @author Juexin Wang
*
*/
@RestController // shorthand for @Controller, @ResponseBody
@CrossOrigin(origins = "*") // allow all cross-domain requests
@Api(tags = "Uniprot", description = "Swissprot")
@RequestMapping(value = "/g2s/")
public class UniprotIdAlignmentController {
    @Autowired
    private AlignmentRepository alignmentRepository;
    @Autowired
    private GeneSequenceRepository geneSequenceRepository;
    @Autowired
    private UniprotRepository uniprotRepository;
    @Autowired
    private SeqIdAlignmentController seqController;
    
    //Query from UniprotIdIso
    @ApiOperation(value = "Get PDB Alignments by UniprotId and Isofrom", nickname = "UniprotIsoformStructureMappingQuery")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Alignment.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/UniprotIsoformStructureMappingQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<Alignment> getPdbAlignmentByUniprotIdIso (
            @RequestParam @ApiParam(value = "Input uniprot Id e.g. Q26540", required = true, allowMultiple = true) String uniprotId, 
            @RequestParam(required = true)
            @ApiParam(value = "Input Isoform e.g. 1", required = true, allowMultiple = true) String isoform){
        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotIdIso(uniprotId+"_"+isoform); 
        if(uniprotlist.size()==1){
            return alignmentRepository.findBySeqId(uniprotlist.get(0).getSeqId());
        }else{
            return new ArrayList<Alignment>();
        }
    }

    @ApiOperation(value = "Whether Isoform of UniprotId Exists", nickname = "UniprotIsoformRecognitionQuery")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", responseContainer = "boolean"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/UniprotIsoformRecognitionQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public boolean getExistedUniprotIdIsoinAlignment(
            @RequestParam @ApiParam(value = "Input UniprotId e.g. Q26540", required = true, allowMultiple = true) String uniprotId, 
            @RequestParam(required = true)
            @ApiParam(value = "Input Isoform e.g. 1", required = true, allowMultiple = true) String isoform){
        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotIdIso(uniprotId+"_"+isoform);
        if(uniprotlist.size()==1){
            return geneSequenceRepository.findBySeqId(uniprotlist.get(0).getSeqId()).size() != 0;
        }else{
            return false;
        }
    }
        
    @ApiOperation(value = "Get Residue Mapping by UniprotId, Isofrom and Residue Position", nickname = "UniprotIsoformResidueMappingQuery")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Residue.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/UniprotIsoformResidueMappingQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<Residue> getPdbResidueByUniprotIdIso (
            @RequestParam(required = true)
            @ApiParam(value = "Input Uniprot Id e.g. Q26540", required = true, allowMultiple = true) String uniprotId, 
            @RequestParam(required = true)
            @ApiParam(value = "Input Isoform e.g. 1", required = true, allowMultiple = true) String isoform,
            @RequestParam(required = true)
            @ApiParam(value = "Input Residue Position e.g. 12", required = true, allowMultiple = true) String aaPosition) {
        
        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotIdIso(uniprotId+"_"+isoform);       
        if(uniprotlist.size()==1){
            return seqController.getPdbResidueBySeqId(uniprotlist.get(0).getSeqId(),aaPosition) ;
        }else{
            return new ArrayList<Residue>();
        }
    }
    
    //Query from UniprotId
    @ApiOperation(value = "Get PDB Alignments by UniprotId", nickname = "UniprotStructureMappingQuery")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Alignment.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/UniprotStructureMappingQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<Alignment> getPdbAlignmentByUniprotId (
            @RequestParam @ApiParam(value = "Input Uniprot Id e.g. Q26540", required = true, allowMultiple = true) String uniprotId){
        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId); 
        ArrayList<Alignment> outList = new ArrayList<Alignment>();       
        for(Uniprot entry: uniprotList){
            outList.addAll(alignmentRepository.findBySeqId(entry.getSeqId()));
        }       
        return outList;       
    }

    @ApiOperation(value = "Whether UniprotId Exists", nickname = "UniprotRecognitionQuery")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", responseContainer = "boolean"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/UniprotRecognitionQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public boolean getExistedUniprotIdinAlignment(
            @RequestParam @ApiParam(value = "Input UniprotId e.g. Q26540", required = true, allowMultiple = true) String uniprotId){
        
        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId); 
        ArrayList<Alignment> outList = new ArrayList<Alignment>();       
        for(Uniprot entry: uniprotList){
            outList.addAll(alignmentRepository.findBySeqId(entry.getSeqId()));
        }       
        return outList.size()!=0;
    }
        
    @ApiOperation(value = "Get Residue Mapping by UniprotId and Residue Number", nickname = "UniprotResidueMappingQuery")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Residue.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/UniprotResidueMappingQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<Residue> getPdbResidueByUniprotId (
            @RequestParam(required = true)
            @ApiParam(value = "Input Uniprot Id e.g. Q26540", required = true, allowMultiple = true) String uniprotId, 
            @RequestParam(required = true)
            @ApiParam(value = "Input Residue Position e.g. 12", required = true, allowMultiple = true) String aaPosition) {
        
        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId); 
        ArrayList<Residue> outList = new ArrayList<Residue>();       
        for(Uniprot entry: uniprotList){
            outList.addAll(seqController.getPdbResidueBySeqId(entry.getSeqId(),aaPosition));
        }       
        return outList;
    }

}
