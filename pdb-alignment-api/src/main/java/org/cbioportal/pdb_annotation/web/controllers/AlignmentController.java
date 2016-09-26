package org.cbioportal.pdb_annotation.web.controllers;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.ArrayList;
import java.util.List;
import org.cbioportal.pdb_annotation.web.domain.AlignmentRepository;
import org.cbioportal.pdb_annotation.web.domain.EnsemblRepository;
import org.cbioportal.pdb_annotation.web.domain.PdbRepository;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.Ensembl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * Controller of the main API: AlignmentController
 *
 * @author Juexin Wang
 *
 */
@RestController // shorthand for @Controller, @ResponseBody
@CrossOrigin(origins = "*") // allow all cross-domain requests
@RequestMapping(value = "/pdb_annotation/")
public class AlignmentController {
    @Autowired
    private AlignmentRepository alignmentRepository;
    @Autowired
    private EnsemblRepository ensemblRepository;
    @Autowired
    private PdbRepository pdbRepository;

    @ApiOperation(value = "get pdb alignments by ensemblId", nickname = "getPdbAlignmentByEnsemblId")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Alignment.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/StructureMappingQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<Alignment> getPdbAlignmentByEnsemblId (
            @RequestParam @ApiParam(value = "Input ensembl id. For example ENSP00000483207.2", required = true, allowMultiple = true) String ensemblId) {
        return alignmentRepository.findByEnsemblId(ensemblId);
    }

    @ApiOperation(value = "get whether ensemblId exists by ensemblId", nickname = "getExistedEnsemblidinAlignment")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Alignment.class, responseContainer = "boolean"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/ProteinIdentifierRecognitionQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public boolean getExistedEnsemblidinAlignment(
            @RequestParam @ApiParam(value = "Input ensembl id. For example ENSP00000483207.2", required = true, allowMultiple = true) String ensemblId) {
        return ensemblRepository.findByEnsemblId(ensemblId).size() != 0;
    }
}
