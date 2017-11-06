package org.cbioportal.pdb_annotation.web.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.cbioportal.pdb_annotation.web.domain.AlignmentRepository;
import org.cbioportal.pdb_annotation.web.domain.EnsemblRepository;
import org.cbioportal.pdb_annotation.web.domain.GeneSequenceRepository;
import org.cbioportal.pdb_annotation.web.domain.UniprotRepository;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.Ensembl;
import org.cbioportal.pdb_annotation.web.models.GenomeResidueInput;
import org.cbioportal.pdb_annotation.web.models.Uniprot;
import org.cbioportal.pdb_annotation.web.models.api.UtilAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 *
 * Recognize whether input alignments is existed in the system, not used for
 * cBioportal, but it still works
 *
 * @author Juexin Wang
 *
 */
@RestController // shorthand for @Controller, @ResponseBody
// @CrossOrigin(origins = "*") // allow all cross-domain requests
// @Api(tags = "RecognizeAlignments", description = "ensembl/uniprot/hgvs")
// @RequestMapping(value = "/api/")
public class AlignmentRecognitionController {

    final static Logger log = Logger.getLogger(AlignmentRecognitionController.class);

    @Autowired
    private AlignmentRepository alignmentRepository;
    @Autowired
    private GeneSequenceRepository geneSequenceRepository;
    @Autowired
    private UniprotRepository uniprotRepository;
    @Autowired
    private EnsemblRepository ensemblRepository;

    @ApiOperation(value = "Checks if alignment exists for the given id", nickname = "alignmentExistsByIdGET")
    @RequestMapping(value = "/recognitionAlignments/{id_type}:{id:.+}",
        method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean alignmentExistsByIdGET(
            @ApiParam(required = true, value = "Input id_type: ensembl; uniprot; uniprot_isoform; hgvs; hgvs38")
            @PathVariable String id_type,
            @ApiParam(required = true, value = "Input id e.g. \n"
                    + "ensembl:ENSP00000484409.1/ENSG00000141510.16/ENST00000504290.5;\n"
                    + "uniprot:P04637/P53_HUMAN; \n" + "uniprot_isoform:P04637_9/P53_HUMAN_9;\n"
                    + "hgvs:17:g.79478130C>G;\n" + "hgvs38:17:g.7676594T>G ")
            @PathVariable String id)
    {
        ArrayList<Alignment> outList = new ArrayList<Alignment>();
        if (id_type.equals("ensembl")) {
            if (id.startsWith("ENSP")) {// EnsemblID:
                // ENSP00000484409.1/ENSP00000484409
                if (!id.startsWith("ENSP") || (id.length() != 15 && id.length() != 17)) {
                    return false;
                }
                List<Ensembl> ensembllist = ensemblRepository.findByEnsemblIdStartingWith(id);
                if (ensembllist.size() >= 1) {
                    for (Ensembl ensembl : ensembllist) {
                        if (geneSequenceRepository.findBySeqId(ensembl.getSeqId()).size() != 0) {
                            return true;
                        }
                    }
                    return false;
                } else {
                    return false;
                }
            } else if (id.startsWith("ENSG")) {// EnsemblGene:
                // ENSG00000141510.16
                List<Ensembl> ensembllist = ensemblRepository.findByEnsemblGene(id);
                if (ensembllist.size() >= 1) {
                    return geneSequenceRepository.findBySeqId(ensembllist.get(0).getSeqId()).size() != 0;
                } else {
                    return false;
                }
            } else if (id.startsWith("ENST")) {// EnsemblTranscript:
                // ENST00000504290.5
                List<Ensembl> ensembllist = ensemblRepository.findByEnsemblTranscript(id);
                if (ensembllist.size() >= 1) {
                    return geneSequenceRepository.findBySeqId(ensembllist.get(0).getSeqId()).size() != 0;
                } else {
                    return false;
                }
            } else {
                log.info("Error in Input. id_type:Ensembl id: " + id);
            }

        } else if (id_type.equals("uniprot")) {
            if (id.length() == 6 && id.split("_").length != 2) {// Accession:
                // P04637
                List<Uniprot> uniprotList = uniprotRepository.findByUniprotAccession(id);
                for (Uniprot entry : uniprotList) {
                    outList.addAll(alignmentRepository.findBySeqId(entry.getSeqId()));
                }
                return outList.size() != 0;

            } else if (id.split("_").length == 2) {// ID: P53_HUMAN
                List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(id);

                Set<String> uniprotAccSet = new HashSet<String>();
                for (Uniprot uniprot : uniprotList) {
                    uniprotAccSet.add(uniprot.getUniprotAccession());
                }

                Iterator<String> it = uniprotAccSet.iterator();
                while (it.hasNext()) {
                    if (getExistedUniprotAccessioninAlignment(it.next())) {
                        return true;
                    }
                }
                return false;
            } else {
                log.info("Error in Input. id_type:Uniprot id: " + id);
            }

        } else if (id_type.equals("uniprot_isoform")) {
            if (id.split("_").length == 2 && id.split("_")[0].length() == 6) {// Accession:
                // P04637
                return (getExistedUniprotAccessionIsoinAlignment(id.split("_")[0], id.split("_")[1]));

            } else if (id.split("_").length == 3) {// ID: P53_HUMAN
                return (getExistedUniprotIdIsoinAlignment(id.split("_")[0] + "_" + id.split("_")[1], id.split("_")[2]));

            } else {
                log.info("Error in Input. id_type:Uniprot_isoform id: " + id);
            }

        } else if (id_type.equals("hgvs")) {

            String genomeVersion = "GRCH37";

            String chromosomeNum = id.split(":g\\.")[0];
            String tmp = id.split(":g\\.")[1];
            long pos = Long.parseLong(tmp.substring(0, tmp.length() - 3));
            String nucleotideType = tmp.substring(tmp.length() - 3, tmp.length() - 2);
            return (getExistedEnsemblIdinGenome(chromosomeNum, pos, nucleotideType, genomeVersion));

        } else if (id_type.equals("hgvs38")) {

            String genomeVersion = "GRCH38";

            String chromosomeNum = id.split(":g\\.")[0];
            String tmp = id.split(":g\\.")[1];
            long pos = Long.parseLong(tmp.substring(0, tmp.length() - 3));
            String nucleotideType = tmp.substring(tmp.length() - 3, tmp.length() - 2);
            return (getExistedEnsemblIdinGenome(chromosomeNum, pos, nucleotideType, genomeVersion));

        } else {
            log.info("Error in Input. id_type:" + id_type + " id: " + id);
        }
        return outList.size() != 0;

    }

    // Implementation of API getExistedEnsemblIdinGenome
    private boolean getExistedEnsemblIdinGenome(String chromosomeNum, long position, String nucleotideType,
            String genomeVersion) {
        // Calling GenomeNexus
        UtilAPI uapi = new UtilAPI();
        List<GenomeResidueInput> grlist = new ArrayList<GenomeResidueInput>();
        try {
            grlist = uapi.callAPI(chromosomeNum, position, nucleotideType, genomeVersion);
        } catch (HttpClientErrorException ex) {
            ex.printStackTrace();
            // org.springframework.web.client.HttpClientErrorException: 400 Bad
            // Request
            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        List<Ensembl> ensembllist = new ArrayList<Ensembl>();
        for (GenomeResidueInput gr : grlist) {
            ensembllist.addAll(ensemblRepository.findByEnsemblIdStartingWith(gr.getEnsembl().getEnsemblid()));
        }
        if (ensembllist.size() >= 1) {
            for (Ensembl ensembl : ensembllist) {
                if (geneSequenceRepository.findBySeqId(ensembl.getSeqId()).size() != 0) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    // P04637
    public boolean getExistedUniprotAccessioninAlignment(
            @ApiParam(required = true, value = "Input Uniprot Accession e.g. P04637") @PathVariable String uniprotAccession) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotAccession(uniprotAccession);
        ArrayList<Alignment> outList = new ArrayList<Alignment>();
        for (Uniprot entry : uniprotList) {
            outList.addAll(alignmentRepository.findBySeqId(entry.getSeqId()));
        }
        return outList.size() != 0;
    }

    // P53_HUMAN
    public boolean getExistedUniprotIdAlignment(
            @ApiParam(required = true, value = "Input Uniprot Id e.g. P53_HUMAN") @PathVariable String uniprotId) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);

        Set<String> uniprotAccSet = new HashSet<String>();
        for (Uniprot uniprot : uniprotList) {
            uniprotAccSet.add(uniprot.getUniprotAccession());
        }

        Iterator<String> it = uniprotAccSet.iterator();
        while (it.hasNext()) {
            if (getExistedUniprotAccessioninAlignment(it.next())) {
                return true;
            }
        }
        return false;
    }

    // P04637_9
    public boolean getExistedUniprotAccessionIsoinAlignment(String uniprotAccession, String isoform) {

        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotAccessionIso(uniprotAccession + "_" + isoform);
        if (uniprotlist.size() == 1) {
            return geneSequenceRepository.findBySeqId(uniprotlist.get(0).getSeqId()).size() != 0;
        } else {
            return false;
        }
    }

    // P53_HUMAN_9
    public boolean getExistedUniprotIdIsoinAlignment(String uniprotId, String isoform) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);

        Set<String> uniprotAccSet = new HashSet<String>();
        for (Uniprot uniprot : uniprotList) {
            uniprotAccSet.add(uniprot.getUniprotAccession());
        }

        Iterator<String> it = uniprotAccSet.iterator();
        while (it.hasNext()) {
            if (getExistedUniprotAccessionIsoinAlignment(it.next(), isoform)) {
                return true;
            }
        }
        return false;

    }

}
