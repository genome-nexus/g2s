package org.cbioportal.pdb_annotation.web;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.cbioportal.pdb_annotation.domain.*;
import org.cbioportal.pdb_annotation.service.PdbDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author Selcuk Onur Sumer
 */
@RestController // shorthand for @Controller, @ResponseBody
@CrossOrigin(origins="*") // allow all cross-domain requests
@RequestMapping(value = "/pdb_annotation/")
public class PdbAnnotationController
{
    private PdbUniprotAlignmentRepository pdbUniprotAlignmentRepository;
    private PdbUniprotResidueMappingRepository pdbUniprotResidueMappingRepository;
    private PdbDataService pdbDataService;

    @Autowired
    public PdbAnnotationController(
        PdbUniprotAlignmentRepository pdbUniprotAlignmentRepository,
        PdbUniprotResidueMappingRepository pdbUniprotResidueMappingRepository,
        PdbDataService pdbDataService)
    {
        this.pdbUniprotAlignmentRepository = pdbUniprotAlignmentRepository;
        this.pdbUniprotResidueMappingRepository = pdbUniprotResidueMappingRepository;
        this.pdbDataService = pdbDataService;
    }

    @ApiOperation(value = "get pdb uniprot alignments by pdb id",
        nickname = "getPdbAlignmentByPdb")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success",
            response = PdbUniprotAlignment.class,
            responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request")
    })
    @RequestMapping(value = "/alignment/byPdb/{pdbIds:.+}",
        method = RequestMethod.GET,
        produces = "application/json")
    public List<PdbUniprotAlignment> getPdbUniprotAlignmentByPdbId(
        @PathVariable
        @ApiParam(value = "Comma separated list of pdb ids. For example 1a37,1a4o",
            required = true,
            allowMultiple = true)
        List<String> pdbIds)
    {
        List<PdbUniprotAlignment> uniprotAlignments = new ArrayList<>();

        for (String pdbId : pdbIds)
        {
            List<PdbUniprotAlignment> alignment = getPdbUniprotAlignmentByPdbId(pdbId);

            if (alignment != null)
            {
                //postEnrichmentService.enrichAnnotation(annotation);
                uniprotAlignments.addAll(alignment);
            }
        }

        return uniprotAlignments;
    }

    @ApiOperation(value = "get pdb uniprot alignments by uniprot id",
        nickname = "getPdbAlignmentByUniprot")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success",
            response = PdbUniprotAlignment.class,
            responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request")
    })
    @RequestMapping(value = "/alignment/byUniprot/{uniprotIds:.+}",
        method = RequestMethod.GET,
        produces = "application/json")
    public List<PdbUniprotAlignment> getPdbUniprotAlignmentByUniprotId(
        @PathVariable
        @ApiParam(value = "Comma separated list of uniprot ids. For example P53_HUMAN,RASK_HUMAN",
            required = true,
            allowMultiple = true)
        List<String> uniprotIds)
    {
        List<PdbUniprotAlignment> uniprotAlignments = new ArrayList<>();

        for (String uniprotId : uniprotIds)
        {
            List<PdbUniprotAlignment> alignment = getPdbUniprotAlignmentByUniprotId(uniprotId);

            if (alignment != null)
            {
                //postEnrichmentService.enrichAnnotation(annotation);
                uniprotAlignments.addAll(alignment);
            }
        }

        return uniprotAlignments;
    }

    @ApiOperation(value = "get pdb header info by pdb id",
        nickname = "getPdbHeader")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success",
            response = PdbHeader.class,
            responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request")
    })
    @RequestMapping(value = "/header/{pdbIds:.+}",
        method = RequestMethod.GET,
        produces = "application/json")
    public List<PdbHeader> getPdbHeader(
        @PathVariable
        @ApiParam(value = "Comma separated list of pdb ids. For example 1a37,1a4o",
            required = true,
            allowMultiple = true)
        List<String> pdbIds)
    {
        List<PdbHeader> pdbHeaderList = new LinkedList<>();

        // remove duplicates
        Set<String> pdbIdSet = new LinkedHashSet<>(pdbIds);

        for (String pdbId : pdbIdSet)
        {
            PdbHeader header = pdbDataService.getPdbHeader(pdbId);

            if (header != null)
            {
                pdbHeaderList.add(header);
            }
        }

        return pdbHeaderList;
    }

    @ApiOperation(value = "get position mapping for alignments",
        nickname = "getPositionMap")
    @ApiResponses(value = {
        // TODO this corresponds to List of PdbUniprotResidueMappings, not to the actual model
        @ApiResponse(code = 200, message = "Success",
            response = PdbUniprotResidueMapping.class,
            responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request")
    })
    @RequestMapping(value = "/header/{positions}/{alignments}",
        method = RequestMethod.GET,
        produces = "application/json")
    public Map<Long, Set<PdbUniprotResidueMapping>> getPositionMap(
        @PathVariable
        @ApiParam(value = "Comma separated list of uniprot positions. For example 97,100,105",
            required = true,
            allowMultiple = true)
        List<Long> positions,
        @PathVariable
        @ApiParam(value = "Comma separated list of alignment ids. For example 3412121,3412119",
            required = true,
            allowMultiple = true)
        List<Long> alignments)
    {
        Map<Long, Set<PdbUniprotResidueMapping>> map = new HashMap<>();
        List<PdbUniprotResidueMapping> mappings = new LinkedList<>();

        // remove duplicates
        Set<Long> positionSet = new LinkedHashSet<>(positions);
        Set<Long> alignmentSet = new LinkedHashSet<>(alignments);

        for (Long alignmentId : alignmentSet)
        {
            // get the matching mapping for this alignment id
            List<PdbUniprotResidueMapping> mapping =
                getPdbUniprotResidueMapping(alignmentId, positionSet);

            if (mapping != null)
            {
                mappings.addAll(mapping);
            }
        }

        // create a map of
        // <uniprot position, set of PdbUniprotResidueMapping instances> pairs
        for (PdbUniprotResidueMapping mapping : mappings)
        {
            Set<PdbUniprotResidueMapping> mappingSet =
                map.get(mapping.getUniprotPosition());

            // init the set if not initialized yet
            if (mappingSet == null)
            {
                mappingSet = new LinkedHashSet<>();
                map.put(mapping.getUniprotPosition(), mappingSet);
            }

            mappingSet.add(mapping);
        }

        return map;
    }

    @ApiOperation(value = "get alignment summary by uniprot id",
        nickname = "getAlignmentSummary")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Success",
            response = AlignmentSummary.class,
            responseContainer = "List"),
        @ApiResponse(code = 400, message = "Bad Request")
    })
    @RequestMapping(value = "/summary/{uniprotIds:.+}",
        method = RequestMethod.GET,
        produces = "application/json")
    public List<AlignmentSummary> getAlignmentSummary(
        @PathVariable
        @ApiParam(value = "Comma separated list of uniprot ids. For example P53_HUMAN,RASK_HUMAN",
            required = true,
            allowMultiple = true)
        List<String> uniprotIds)
    {
        List<AlignmentSummary> summaryList = new LinkedList<>();

        for (String uniprotId : uniprotIds)
        {
            AlignmentSummary summary = getAlignmentSummary(uniprotId);

            if (summary != null)
            {
                summaryList.add(summary);
            }
        }

        return summaryList;
    }

    public AlignmentSummary getAlignmentSummary(String uniprotId)
    {
        AlignmentSummary summary = new AlignmentSummary(uniprotId);
        List<PdbUniprotAlignment> list = pdbUniprotAlignmentRepository.findByUniprotId(uniprotId);

        if (list != null)
        {
            summary.setAlignmentCount(list.size());
        }
        else
        {
            summary.setAlignmentCount(0);
        }

        return summary;
    }

    public List<PdbUniprotAlignment> getPdbUniprotAlignmentByUniprotId(String uniprotId)
    {
        return pdbUniprotAlignmentRepository.findByUniprotId(uniprotId);
    }

    public List<PdbUniprotAlignment> getPdbUniprotAlignmentByPdbId(String pdbId)
    {
        return pdbUniprotAlignmentRepository.findByPdbId(pdbId);
    }

    public List<PdbUniprotResidueMapping> getPdbUniprotResidueMapping(
        long alignmentId, Set<Long> positions)
    {
        List<PdbUniprotResidueMapping> list = new LinkedList<>();

        // first, get all PdbUniprotResidueMappings matching the given alignment id
        // TODO do we need to sort the list by ascending uniprot positions?
        List<PdbUniprotResidueMapping> mappings =
            pdbUniprotResidueMappingRepository.findByAlignmentId(alignmentId);

        // then, filter by provided uniprot positions
        for (PdbUniprotResidueMapping mapping : mappings)
        {
            // only add positions matching the ones in the provided set
            if (positions.contains(mapping.getUniprotPosition()))
            {
                list.add(mapping);
            }
        }

        return list;
    }
}
