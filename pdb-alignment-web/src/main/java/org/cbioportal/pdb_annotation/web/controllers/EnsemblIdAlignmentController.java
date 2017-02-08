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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    @RequestMapping(value = "/EnsemblStructureMapping/{ensemblId:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by EnsemblId")
    public List<Alignment> getPdbAlignmentByEnsemblId(
            @ApiParam(required = true, value = "Input Ensembl Id e.g. ENSP00000489609.1")
            @PathVariable String ensemblId) {
        
        System.out.println(ensemblId);
        List<Ensembl> ensembllist = ensemblRepository.findByEnsemblId(ensemblId);
        System.out.println(ensembllist.size());
        if (ensembllist.size() == 1) {
            return alignmentRepository.findBySeqId(ensembllist.get(0).getSeqId());
        } else {
            return null;
        }
        
    }
    
    
    @RequestMapping(value = "/EnsemblRecognition/{ensemblId:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Whether EnsemblId exists")
    public boolean getExistedEnsemblIdinAlignment(
            @ApiParam(required = true, value = "Input Ensembl Id e.g. ENSP00000489609.1")
            @PathVariable String ensemblId) {
        List<Ensembl> ensembllist = ensemblRepository.findByEnsemblId(ensemblId);
        if (ensembllist.size() == 1) {
            return geneSequenceRepository.findBySeqId(ensembllist.get(0).getSeqId()).size() != 0;
        } else {
            return false;
        }
    }


    @RequestMapping(value = "/EnsemblResidueMapping/{ensemblId:.+}/{position}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get Residue Mapping by EnsemblId and Residue Number")
    public List<Residue> getPdbResidueByEnsemblId(
            @ApiParam(required = true, value = "Input Ensembl Id e.g. ENSP00000489609.1")
            @PathVariable String ensemblId,
            @ApiParam(required = true, value = "Input Residue Position e.g. 300")
            @PathVariable String position) {

        List<Ensembl> ensembllist = ensemblRepository.findByEnsemblId(ensemblId);

        if (ensembllist.size() == 1) {
            return seqController.getPdbResidueBySeqId(ensembllist.get(0).getSeqId(), position);
        } else {
            return null;
        }
    }
    

}
