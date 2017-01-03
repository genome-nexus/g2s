package org.cbioportal.pdb_annotation.web.controllers;

import java.util.List;

import org.cbioportal.pdb_annotation.web.domain.AlignmentRepository;
import org.cbioportal.pdb_annotation.web.domain.EnsemblRepository;
import org.cbioportal.pdb_annotation.web.domain.GeneSequenceRepository;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.Ensembl;
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
 * Controller of the API: Input Ensembl
 *
 * @author Juexin Wang
 *
 */
@RestController // shorthand for @Controller, @ResponseBody
@CrossOrigin(origins = "*") // allow all cross-domain requests
@Api(tags = "Human Ensembl", description = " ")
@RequestMapping(value = "/g2s/")
public class EnsemblIdAlignmentController {

    @Autowired
    private AlignmentRepository alignmentRepository;
    @Autowired
    private GeneSequenceRepository geneSequenceRepository;
    @Autowired
    private EnsemblRepository ensemblRepository;
    @Autowired
    private SeqIdAlignmentController seqController;

    // Query from EnsemblId
    @ApiOperation(value = "Get PDB Alignments by EnsemblId", nickname = "EnsemblStructureMappingQuery")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Alignment.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/EnsemblStructureMappingQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<Alignment> getPdbAlignmentByEnsemblId(
            @RequestParam @ApiParam(value = "Input Ensembl Id e.g. ENSP00000489609.1", required = true, allowMultiple = true) String ensemblId) {
        List<Ensembl> ensembllist = ensemblRepository.findByEnsemblId(ensemblId);
        if (ensembllist.size() == 1) {
            return alignmentRepository.findBySeqId(ensembllist.get(0).getSeqId());
        } else {
            return null;
        }
    }

    @ApiOperation(value = "Whether EnsemblId exists", nickname = "EnsemblRecognitionQuery")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", responseContainer = "boolean"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/EnsemblRecognitionQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public boolean getExistedEnsemblIdinAlignment(
            @RequestParam @ApiParam(value = "Input Ensembl Id e.g. ENSP00000489609.1", required = true, allowMultiple = true) String ensemblId) {
        List<Ensembl> ensembllist = ensemblRepository.findByEnsemblId(ensemblId);
        if (ensembllist.size() == 1) {
            return geneSequenceRepository.findBySeqId(ensembllist.get(0).getSeqId()).size() != 0;
        } else {
            return false;
        }
    }

    @ApiOperation(value = "Get Residue Mapping by EnsemblId and Residue Number", nickname = "EnsemblResidueMappingQuery")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Residue.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/EnsemblResidueMappingQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<Residue> getPdbResidueByEnsemblId(
            @RequestParam(required = true) @ApiParam(value = "Input Ensembl Id e.g. ENSP00000489609.1", required = true, allowMultiple = true) String ensemblId,
            @RequestParam(required = true) @ApiParam(value = "Input Residue Position e.g. 300", required = true, allowMultiple = true) String aaPosition) {

        List<Ensembl> ensembllist = ensemblRepository.findByEnsemblId(ensemblId);

        if (ensembllist.size() == 1) {
            return seqController.getPdbResidueBySeqId(ensembllist.get(0).getSeqId(), aaPosition);
        } else {
            return null;
        }
    }

}
