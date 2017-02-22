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

    // Query from UniprotIdIso
    @RequestMapping(value = "/UniprotIsoformStructureMapping/{uniprotId}/{isoform}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by UniprotId and Isofrom")
    public List<Alignment> getPdbAlignmentByUniprotIdIso(
            @ApiParam(required = true, value = "Input uniprot Id e.g. P04637") @PathVariable String uniprotId,
            @ApiParam(required = true, value = "Input Isoform e.g. 9") @PathVariable String isoform) {
        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotIdIso(uniprotId + "_" + isoform);
        if (uniprotlist.size() == 1) {
            return alignmentRepository.findBySeqId(uniprotlist.get(0).getSeqId());
        } else {
            return new ArrayList<Alignment>();
        }
    }

    @RequestMapping(value = "/UniprotIsoformRecognition/{uniprotId}/{isoform}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Whether Isoform of UniprotId Exists")
    public boolean getExistedUniprotIdIsoinAlignment(
            @ApiParam(required = true, value = "Input uniprot Id e.g. P04637") @PathVariable String uniprotId,
            @ApiParam(required = true, value = "Input Isoform e.g. 9") @PathVariable String isoform) {
        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotIdIso(uniprotId + "_" + isoform);
        if (uniprotlist.size() == 1) {
            return geneSequenceRepository.findBySeqId(uniprotlist.get(0).getSeqId()).size() != 0;
        } else {
            return false;
        }
    }

    @RequestMapping(value = "/UniprotIsoformResidueMapping/{uniprotId}/{isoform}/{position}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get Residue Mapping by UniprotId, Isofrom and Residue Position")
    public List<Residue> getPdbResidueByUniprotIdIso(
            @ApiParam(required = true, value = "Input uniprot Id e.g. P04637") @PathVariable String uniprotId,
            @ApiParam(required = true, value = "Input Isoform e.g. 9") @PathVariable String isoform,
            @ApiParam(required = true, value = "Input Residue Position e.g. 100") @PathVariable String position) {

        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotIdIso(uniprotId + "_" + isoform);
        if (uniprotlist.size() == 1) {
            return seqController.getPdbResidueBySeqId(uniprotlist.get(0).getSeqId(), position);
        } else {
            return new ArrayList<Residue>();
        }
    }

    // Query from UniprotId
    @RequestMapping(value = "/UniprotStructureMapping/{uniprotId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by UniprotId")
    public List<Alignment> getPdbAlignmentByUniprotId(
            @ApiParam(required = true, value = "Input uniprot Id e.g. P04637") @PathVariable String uniprotId) {
        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);
        ArrayList<Alignment> outList = new ArrayList<Alignment>();
        for (Uniprot entry : uniprotList) {
            outList.addAll(alignmentRepository.findBySeqId(entry.getSeqId()));
        }
        return outList;
    }

    @RequestMapping(value = "/UniprotRecognition/{uniprotId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Whether UniprotId Exists")
    public boolean getExistedUniprotIdinAlignment(
            @ApiParam(required = true, value = "Input uniprot Id e.g. P04637") @PathVariable String uniprotId) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);
        ArrayList<Alignment> outList = new ArrayList<Alignment>();
        for (Uniprot entry : uniprotList) {
            outList.addAll(alignmentRepository.findBySeqId(entry.getSeqId()));
        }
        return outList.size() != 0;
    }

    @RequestMapping(value = "/UniprotResidueMapping/{uniprotId}/{position}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get Residue Mapping by UniprotId and Residue Position")
    public List<Residue> getPdbResidueByUniprotId(
            @ApiParam(required = true, value = "Input uniprot Id e.g. P04637") @PathVariable String uniprotId,
            @ApiParam(required = true, value = "Input Residue Position e.g. 100") @PathVariable String position) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);
        ArrayList<Residue> outList = new ArrayList<Residue>();
        for (Uniprot entry : uniprotList) {
            outList.addAll(seqController.getPdbResidueBySeqId(entry.getSeqId(), position));
        }
        return outList;
    }

}
