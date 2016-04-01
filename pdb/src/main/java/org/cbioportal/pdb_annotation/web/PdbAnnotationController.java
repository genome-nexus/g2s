package org.cbioportal.pdb_annotation.web;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.cbioportal.pdb_annotation.domain.PdbUniprotAlignment;
import org.cbioportal.pdb_annotation.domain.PdbUniprotAlignmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Selcuk Onur Sumer
 */
@RestController // shorthand for @Controller, @ResponseBody
@CrossOrigin(origins="*") // allow all cross-domain requests
@RequestMapping(value = "/pdb_annotation/")
public class PdbAnnotationController
{
	private PdbUniprotAlignmentRepository pdbUniprotAlignmentRepository;

	@Autowired
	public PdbAnnotationController(PdbUniprotAlignmentRepository pdbUniprotAlignmentRepository)
	{
		this.pdbUniprotAlignmentRepository = pdbUniprotAlignmentRepository;
	}

	@ApiOperation(value = "getVariantAnnotation",
			nickname = "getVariantAnnotation")
	@ApiResponses(value = {
			@ApiResponse(code = 200, message = "Success",
					response = PdbUniprotAlignment.class,
					responseContainer = "List"),
			@ApiResponse(code = 400, message = "Bad Request")
	})
	@RequestMapping(value = "/alignment/{pdbIds:.+}",
			method = RequestMethod.GET,
			produces = "application/json")
	public List<PdbUniprotAlignment> getPdbUniprotAlignment(
			@PathVariable
			@ApiParam(value="Comma separated list of pdb ids. For example 1a37,1a4o",
					required = true,
					allowMultiple = true)
			List<String> pdbIds)
	{
		List<PdbUniprotAlignment> uniprotAlignments = new ArrayList<>();


		for (String pdbId: pdbIds)
		{
			List<PdbUniprotAlignment> alignment = getPdbUniprotAlignment(pdbId);

			if (alignment != null)
			{
				//postEnrichmentService.enrichAnnotation(annotation);
				uniprotAlignments.addAll(alignment);
			}
		}

		return uniprotAlignments;
	}

	public List<PdbUniprotAlignment> getPdbUniprotAlignment(String pdbId)
	{
		return pdbUniprotAlignmentRepository.findByPdbId(pdbId);
	}
}
