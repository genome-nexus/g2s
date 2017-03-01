package org.cbioportal.pdb_annotation.web.controllers;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.pdb_annotation.web.domain.AlignmentRepository;
import org.cbioportal.pdb_annotation.web.domain.EnsemblRepository;
import org.cbioportal.pdb_annotation.web.domain.GeneSequenceRepository;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.Ensembl;
import org.cbioportal.pdb_annotation.web.models.Residue;
import org.cbioportal.pdb_annotation.web.models.ResiduePresent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
*
* Controller of the API: Input AlignmentID, get all the residue mapping
*
* @author Juexin Wang
*
*/
@RestController // shorthand for @Controller, @ResponseBody
@CrossOrigin(origins = "*") // allow all cross-domain requests
@Api(tags = "Alignment", description = "ResidueMapping")
@RequestMapping(value = "/g2s/")
public class AllResidueAlignmentController {
   
    @Autowired
    private AlignmentRepository alignmentRepository;
    
    //Query by AlignmentId, get all the Residue Mapping 
    @RequestMapping(value = "/ResidueMappingFromAlignmentId/{alignmentId:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get All Residue Mapping by AlignmentId")
    public List<ResiduePresent> getResidueMappingByAlignmentId(
            @ApiParam(required = true, value = "Input AlignmentId e.g. 883556") @PathVariable int alignmentId) {

        Alignment ali = alignmentRepository.findByAlignmentId(alignmentId);
        List<ResiduePresent> residueList = new ArrayList<ResiduePresent>();
        
        for(int i=ali.getSeqFrom(); i<=ali.getSeqTo(); i++){
            ResiduePresent residue = new ResiduePresent();
            residue.setInputNum(i);
            residue.setInputName(ali.getSeqAlign().substring(i - ali.getSeqFrom(), i - ali.getSeqFrom() + 1));
            residue.setResidueNum(Integer.parseInt(ali.getSegStart()) - 1 + ali.getPdbFrom() + (i - ali.getSeqFrom()));
            residue.setResidueName( ali.getPdbAlign().substring(i - ali.getSeqFrom(), i - ali.getSeqFrom() + 1));
            residueList.add(residue);
        }        
        return residueList;        
    }

}
