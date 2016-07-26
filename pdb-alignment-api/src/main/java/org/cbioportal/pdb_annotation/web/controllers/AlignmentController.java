package org.cbioportal.pdb_annotation.web.controllers;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.pdb_annotation.web.domain.AlignmentRepository;
import org.cbioportal.pdb_annotation.web.domain.EnsemblRepository;
import org.cbioportal.pdb_annotation.web.domain.PdbRepository;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

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

	private AlignmentRepository alignmentRepository;
	private EnsemblRepository ensemblRepository;
	private PdbRepository pdbRepository;

	@Autowired
	public AlignmentController(AlignmentRepository alignmentRepository, EnsemblRepository ensemblRepository,
			PdbRepository pdbRepository) {
		this.alignmentRepository = alignmentRepository;
		this.ensemblRepository = ensemblRepository;
		this.pdbRepository = pdbRepository;
	}

	@ApiOperation(value = "get pdb alignments by ensemblId", nickname = "getPdbAlignmentByEnsemblId")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success", response = Alignment.class, responseContainer = "List"),
			@ApiResponse(code = 400, message = "Bad Request") })
	@RequestMapping(value = "/StructureMappingQuery", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	@ResponseStatus(HttpStatus.OK)
	public List<Alignment> getPdbAlignmentByEnsemblId(
			@RequestParam @ApiParam(value = "Input ensembl id. For example ENSP00000483207.2", required = true, allowMultiple = true) String ensemblId) {
		List<Alignment> list = new ArrayList();
		try {
			List<Alignment> it = alignmentRepository.findByEnsemblId(ensemblId);
			list.addAll(it);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return list;
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
		try {
			List<Alignment> it = alignmentRepository.findByEnsemblId(ensemblId);
			if (it.size() != 0) {
				return true;
			} else {
				return false;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

}
