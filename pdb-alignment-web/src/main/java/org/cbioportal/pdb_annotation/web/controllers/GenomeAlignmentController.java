package org.cbioportal.pdb_annotation.web.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.cbioportal.pdb_annotation.web.domain.AlignmentRepository;
import org.cbioportal.pdb_annotation.web.domain.EnsemblRepository;
import org.cbioportal.pdb_annotation.web.domain.GeneSequenceRepository;
import org.cbioportal.pdb_annotation.web.domain.PdbRepository;
import org.cbioportal.pdb_annotation.web.domain.UniprotRepository;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.Ensembl;
import org.cbioportal.pdb_annotation.web.models.GenomeResidue;
import org.cbioportal.pdb_annotation.web.models.GenomeResidueInput;
import org.cbioportal.pdb_annotation.web.models.Residue;
import org.cbioportal.pdb_annotation.web.models.Uniprot;
import org.cbioportal.pdb_annotation.web.models.api.UtilAPI;
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
import org.springframework.web.client.HttpClientErrorException;

/**
 *
 * Controller of the API: Input Genome Position
 * 
 *     For GRCH38, calling from ENSEMBL
 *     For GRCH37, calling from Genome Nexus
 *
 * @author Juexin Wang
 *
 */
@RestController // shorthand for @Controller, @ResponseBody
@CrossOrigin(origins = "*") // allow all cross-domain requests
@Api(tags = "Genome", description = "GRCh37/GRCh38")
@RequestMapping(value = "/g2s/")
public class GenomeAlignmentController {
    @Autowired
    private GeneSequenceRepository geneSequenceRepository;
    @Autowired
    private EnsemblRepository ensemblRepository;
    @Autowired
    private SeqIdAlignmentController seqController;

    // For GRCH38, calling from ENSEMBL
    // Genome to Structure:
    // whether ensembl exists by API
    @RequestMapping(value = "/GenomeStructureRecognition/GRCH38/{chromosomeNum}/{position}/{nucleotideType}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Whether Position Valid in Genome")
    public String getExistedEnsemblIdinGenomeGRCH38(
            @ApiParam(required = true, value = "Input Chromomsome Number e.g. 17") @PathVariable String chromosomeNum,
            @ApiParam(required = true, value = "Input Nucleotide Position e.g. 7676594") @PathVariable long position,
            @ApiParam(required = true, value = "Input Nucleotide Type e.g. T Note: Please Input Correct Type") @PathVariable String nucleotideType) {

        String genomeVersion = "GRCH38";
        return getExistedEnsemblIdinGenome(chromosomeNum, position, nucleotideType, genomeVersion);        
    }

    @RequestMapping(value = "/GenomeStructureResidueMapping/GRCH38/{chromosomeNum}/{position}/{nucleotideType}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get Residue Mapping from Genome Position")
    public List<GenomeResidue> getPdbResidueByEnsemblIdGenomeGRCH38(
            @ApiParam(required = true, value = "Input Chromomsome Number e.g. 17") @PathVariable String chromosomeNum,
            @ApiParam(required = true, value = "Input Nucleotide Position e.g. 7676594") @PathVariable long position,
            @ApiParam(required = true, value = "Input Nucleotide Type e.g. T Note: Please Input Correct Type") @PathVariable String nucleotideType) {
        String genomeVersion = "GRCH38";
        return getPdbResidueByEnsemblIdGenome(chromosomeNum, position, nucleotideType, genomeVersion);
    }
    
    
    // For GRCH37, calling from GenomeNexus
    // Genome to Structure:
    // whether ensembl exists by API
    @RequestMapping(value = "/GenomeStructureRecognition/GRCH37/{chromosomeNum}/{position}/{nucleotideType}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Whether Position Valid in Genome")
    public String getExistedEnsemblIdinGenomeGRCH37(
            @ApiParam(required = true, value = "Input Chromomsome Number e.g. 17") @PathVariable String chromosomeNum,
            @ApiParam(required = true, value = "Input Nucleotide Position e.g. 79478130") @PathVariable long position,
            @ApiParam(required = true, value = "Input Nucleotide Type e.g. C Note: Please Input Correct Type") @PathVariable String nucleotideType) {

        String genomeVersion = "GRCH37";
        return getExistedEnsemblIdinGenome(chromosomeNum, position, nucleotideType, genomeVersion);        
    }

    @RequestMapping(value = "/GenomeStructureResidueMapping/GRCH37/{chromosomeNum}/{position}/{nucleotideType}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get Residue Mapping from Genome Position")
    public List<GenomeResidue> getPdbResidueByEnsemblIdGenomeGRCH37(
            @ApiParam(required = true, value = "Input Chromomsome Number e.g. 17") @PathVariable String chromosomeNum,
            @ApiParam(required = true, value = "Input Nucleotide Position e.g. 79478130") @PathVariable long position,
            @ApiParam(required = true, value = "Input Nucleotide Type e.g. C Note: Please Input Correct Type") @PathVariable String nucleotideType) {
        String genomeVersion = "GRCH37";
        return getPdbResidueByEnsemblIdGenome(chromosomeNum, position, nucleotideType, genomeVersion);
    }
    
    
    //Implementation of API getExistedEnsemblIdinGenome
    private String getExistedEnsemblIdinGenome(String chromosomeNum, long position, String nucleotideType, String genomeVersion){
        // Calling GenomeNexus
        UtilAPI uapi = new UtilAPI();
        List<GenomeResidueInput> grlist = new ArrayList<GenomeResidueInput>();
        try {
            grlist = uapi.callAPI(chromosomeNum, position, nucleotideType, genomeVersion);
        } catch (HttpClientErrorException ex) {
            ex.printStackTrace();
            // org.springframework.web.client.HttpClientErrorException: 400 Bad
            // Request
            return "Input error, Please check";
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
                    return "true";
                }
            }
            return "false";
        } else {
            return "false";
        }
    }
    
  //Implementation of API etPdbResidueByEnsemblIdGenome
    private List<GenomeResidue> getPdbResidueByEnsemblIdGenome(String chromosomeNum, long position, String nucleotideType, String genomeVersion){
        // Calling GenomeNexus
        UtilAPI uapi = new UtilAPI();

        List<GenomeResidueInput> grlist = new ArrayList<GenomeResidueInput>();
        try {
            grlist = uapi.callAPI(chromosomeNum, position, nucleotideType, genomeVersion);
        } catch (HttpClientErrorException ex) {
            ex.printStackTrace();
            // org.springframework.web.client.HttpClientErrorException: 400 Bad
            // Request
            return null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        List<GenomeResidueInput> grlistValid = new ArrayList<GenomeResidueInput>();
        for (GenomeResidueInput gr : grlist) {
            List<Ensembl> ensembllist = ensemblRepository.findByEnsemblIdStartingWith(gr.getEnsembl().getEnsemblid());
            // System.out.println(gr.getEnsembl().getEnsemblid());
            
            for(Ensembl ensembl:ensembllist){
                if (geneSequenceRepository.findBySeqId(ensembl.getSeqId()).size() != 0) {
                    Ensembl es = gr.getEnsembl();
                    es.setSeqId(ensembl.getSeqId());
                    // System.out.println("API
                    // ensemblID:\t"+es.getEnsemblid()+"\t:"+es.getSeqId());
                    gr.setEnsembl(es);
                    grlistValid.add(gr);
                }
                
            }

        }

        if (grlistValid.size() >= 1) {
            List<GenomeResidue> outlist = new ArrayList<GenomeResidue>();
            for (GenomeResidueInput gr : grlistValid) {
                // System.out.println("Out:\t" + gr.getEnsembl().getSeqId() +
                // "\t:"
                // + Integer.toString(gr.getResidue().getResidueNum()));
                List<Residue> list = seqController.getPdbResidueBySeqId(gr.getEnsembl().getSeqId(),
                        Integer.toString(gr.getResidue().getResidueNum()));
                Ensembl en = gr.getEnsembl();
                GenomeResidue ge = new GenomeResidue();
                try {
                    BeanUtils.copyProperties(ge, en);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                ge.setAlignments(list);
                outlist.add(ge);
            }
            return outlist;
        } else {
            return null;
        }
    }

}
