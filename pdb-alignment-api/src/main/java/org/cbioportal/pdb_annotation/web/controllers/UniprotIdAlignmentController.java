package org.cbioportal.pdb_annotation.web.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.cbioportal.pdb_annotation.web.domain.AlignmentRepository;
import org.cbioportal.pdb_annotation.web.domain.GeneSequenceRepository;
import org.cbioportal.pdb_annotation.web.domain.UniprotRepository;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.Residue;
import org.cbioportal.pdb_annotation.web.models.Uniprot;
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


    @ApiOperation(value = "Retrieves PDB Alignments by Uniprot id and Isoform",
        nickname = "fetchPdbAlignmentsByUniprotIdAndIsoformGET")
    @RequestMapping(value = "/UniprotIsoformStructureMapping/{uniprotId}/{isoform}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Alignment> fetchPdbAlignmentsByUniprotIdAndIsoformGET(
            @ApiParam(required = true, value = "Uniprot id, e.g. Q26540")
            @PathVariable String uniprotId,
            @ApiParam(required = true, value = "Isoform, e.g. 1")
            @PathVariable String isoform)
    {
        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotIdIso(uniprotId + "_" + isoform);

        if (uniprotlist.size() > 0) {
            return alignmentRepository.findBySeqId(uniprotlist.get(0).getSeqId());
        } else {
            return Collections.emptyList();
        }
    }

    @ApiOperation(value = "Checks if Protein Sequence exists for a given Uniprot ID and Isoform",
        nickname = "sequenceExistsByUniprotIdAndIsoformGET")
    @RequestMapping(value = "/UniprotIsoformRecognition/{uniprotId}/{isoform}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean sequenceExistsByUniprotIdAndIsoformGET(
            @ApiParam(required = true, value = "Uniprot id, e.g. Q26540")
            @PathVariable String uniprotId,
            @ApiParam(required = true, value = "Isoform, e.g. 1")
            @PathVariable String isoform)
    {
        List<Uniprot> uniprotList = uniprotRepository.findByUniprotIdIso(uniprotId + "_" + isoform);

        return uniprotList.size() > 0 &&
            geneSequenceRepository.findBySeqId(uniprotList.get(0).getSeqId()).size() != 0;
    }


    @RequestMapping(value = "/UniprotIsoformResidueMapping/{uniprotId}/{isoform}/{position}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Retrieves Residue Mapping by UniprotId, Isofrom and Residue Position")
    public List<Residue> getPdbResidueByUniprotIdIso(
            @ApiParam(required = true, value = "Uniprot Id, e.g. Q26540")
            @PathVariable String uniprotId,
            @ApiParam(required = true, value = "Isoform, e.g. 1")
            @PathVariable String isoform,
            @ApiParam(required = true, value = "Residue Position, e.g. 12")
            @PathVariable String position)
    {
        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotIdIso(uniprotId + "_" + isoform);
        if (uniprotlist.size() == 1) {
            return seqController.fetchPdbResiduesBySeqIdGET(uniprotlist.get(0).getSeqId(), position);
        } else {
            return new ArrayList<Residue>();
        }
    }

    @ApiOperation(value = "Retrieves PDB Alignments by Uniprot id",
        nickname = "fetchPdbAlignmentsByUniprotIdGET")
    @RequestMapping(value = "/UniprotStructureMapping/{uniprotId}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Alignment> fetchPdbAlignmentsByUniprotIdGET(
            @ApiParam(required = true, value = "Uniprot Id, e.g. Q26540")
            @PathVariable String uniprotId)
    {
        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);
        ArrayList<Alignment> outList = new ArrayList<Alignment>();
        for (Uniprot entry : uniprotList) {
            outList.addAll(alignmentRepository.findBySeqId(entry.getSeqId()));
        }
        return outList;
    }

    @ApiOperation(value = "Checks if alignment exists for a given Uniprot id",
        nickname = "alignmentExistsByUniprotIdGET")
    @RequestMapping(value = "/UniprotRecognition/{uniprotId}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean alignmentExistsByUniprotIdGET(
            @ApiParam(required = true, value = "Uniprot id, e.g. Q26540")
            @PathVariable String uniprotId)
    {
        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);
        ArrayList<Alignment> outList = new ArrayList<Alignment>();
        for (Uniprot entry : uniprotList) {
            outList.addAll(alignmentRepository.findBySeqId(entry.getSeqId()));
        }
        return outList.size() != 0;
    }

    @ApiOperation(value = "Retrieves Residue Mapping by UniprotId and Residue Position",
        nickname = "fetchPdbResiduesByUniprotIdGET")
    @RequestMapping(value = "/UniprotResidueMapping/{uniprotId}/{position}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Residue> fetchPdbResiduesByUniprotIdGET(
            @ApiParam(required = true, value = "Uniprot Id, e.g. Q26540")
            @PathVariable String uniprotId,
            @ApiParam(required = true, value = "Residue Position, e.g. 12")
            @PathVariable String position)
    {
        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);
        ArrayList<Residue> outList = new ArrayList<Residue>();
        for (Uniprot entry : uniprotList) {
            outList.addAll(seqController.fetchPdbResiduesBySeqIdGET(entry.getSeqId(), position));
        }
        return outList;
    }

}
