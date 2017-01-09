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
 * @author Juexin Wang
 *
 */
@RestController // shorthand for @Controller, @ResponseBody
@CrossOrigin(origins = "*") // allow all cross-domain requests
@Api(tags = "Genome", description = "GRCh37")
@RequestMapping(value = "/g2s/")
public class GenomeAlignmentController {
    @Autowired
    private GeneSequenceRepository geneSequenceRepository;
    @Autowired
    private EnsemblRepository ensemblRepository;
    @Autowired
    private SeqIdAlignmentController seqController;
   
    // Genome to Structure:
    // whether ensembl exists by API
                @RequestMapping(value = "/GenomeStructureRecognition/{chromosomeNum}/{position}/{nucleotideType}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
                @ApiOperation("Whether Position Valid in Genome")
                public String getExistedEnsemblIdinGenome(
                        @ApiParam(required = true, value = "Input Chromomsome Number e.g. X")
                        @PathVariable String chromosomeNum,
                        @ApiParam(required = true, value = "Input Nucleotide Position e.g. 66937331")
                        @PathVariable long position,
                        @ApiParam(required = true, value = "Input Nucleotide Type e.g. T Note: Please Input Correct Type")
                        @PathVariable String nucleotideType ) {

                    // Calling GenomeNexus
                    UtilAPI uapi = new UtilAPI();
                    List<GenomeResidueInput> grlist = new ArrayList<GenomeResidueInput>();
                    try {
                        grlist = uapi.callAPI(chromosomeNum, position, nucleotideType);
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
    
                
                
                
    /*
    @ApiOperation(value = "Whether Position Valid in Genome", nickname = "GenomeStructureRecognitionQuery")
    @ApiResponses(value = { @ApiResponse(code = 200, message = "Success", responseContainer = "boolean"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/GenomeStructureRecognitionQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public String getExistedEnsemblIdinGenome(
            @RequestParam(required = true) @ApiParam(value = "Input Chromomsome Number e.g. X", required = true, allowMultiple = true) String chromosomeNum,
            @RequestParam(required = true) @ApiParam(value = "Input Nucleotide Position e.g. 66937331", required = true, allowMultiple = true) long positionNum,
            @RequestParam(required = true) @ApiParam(value = "Input Nucleotide Type e.g. T Note: Please Input Correct Type", required = true, allowMultiple = true) String nucleotideType) {

        // Calling GenomeNexus
        UtilAPI uapi = new UtilAPI();
        List<GenomeResidueInput> grlist = new ArrayList<GenomeResidueInput>();
        try {
            grlist = uapi.callAPI(chromosomeNum, positionNum, nucleotideType);
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
    */

                @RequestMapping(value = "/GenomeStructureResidueMapping/{chromosomeNum}/{position}/{nucleotideType}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
                @ApiOperation("Get Residue Mapping from Genome Position")
                public List<GenomeResidue> getPdbResidueByEnsemblIdGenome(
                        @ApiParam(required = true, value = "Input Chromomsome Number e.g. X")
                        @PathVariable String chromosomeNum,
                        @ApiParam(required = true, value = "Input Nucleotide Position e.g. 66937331")
                        @PathVariable long position,
                        @ApiParam(required = true, value = "Input Nucleotide Type e.g. T Note: Please Input Correct Type")
                        @PathVariable String nucleotideType) {

                    // Calling GenomeNexus
                    UtilAPI uapi = new UtilAPI();

                    List<GenomeResidueInput> grlist = new ArrayList<GenomeResidueInput>();
                    try {
                        grlist = uapi.callAPI(chromosomeNum, position, nucleotideType);
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

                        if (geneSequenceRepository.findBySeqId(ensembllist.get(0).getSeqId()).size() != 0) {
                            Ensembl es = gr.getEnsembl();
                            es.setSeqId(ensembllist.get(0).getSeqId());
                            // System.out.println("API
                            // ensemblID:\t"+es.getEnsemblid()+"\t:"+es.getSeqId());
                            gr.setEnsembl(es);
                            grlistValid.add(gr);
                        }
                    }

                    if (grlistValid.size() >= 1) {
                        List<GenomeResidue> outlist = new ArrayList<GenomeResidue>();
                        for (GenomeResidueInput gr : grlistValid) {
                            System.out.println("Out:\t" + gr.getEnsembl().getSeqId() + "\t:"
                                    + Integer.toString(gr.getResidue().getResidueNum()));
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
                

/*
    @ApiOperation(value = "Get Residue Mapping from Genome Position", nickname = "GenomeStructureResidueMappingQuery")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = GenomeResidue.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/GenomeStructureResidueMappingQuery", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public List<GenomeResidue> getPdbResidueByEnsemblIdGenome(
            @RequestParam(required = true) @ApiParam(value = "Input Chromomsome Number e.g. X", required = true, allowMultiple = true) String chromosomeNum,
            @RequestParam(required = true) @ApiParam(value = "Input Nucleotide Position e.g. 66937331", required = true, allowMultiple = true) long positionNum,
            @RequestParam(required = true) @ApiParam(value = "Input Nucleotide Type e.g. T Note: Please Input Correct Type", required = true, allowMultiple = true) String nucleotideType) {

        // Calling GenomeNexus
        UtilAPI uapi = new UtilAPI();

        List<GenomeResidueInput> grlist = new ArrayList<GenomeResidueInput>();
        try {
            grlist = uapi.callAPI(chromosomeNum, positionNum, nucleotideType);
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

            if (geneSequenceRepository.findBySeqId(ensembllist.get(0).getSeqId()).size() != 0) {
                Ensembl es = gr.getEnsembl();
                es.setSeqId(ensembllist.get(0).getSeqId());
                // System.out.println("API
                // ensemblID:\t"+es.getEnsemblid()+"\t:"+es.getSeqId());
                gr.setEnsembl(es);
                grlistValid.add(gr);
            }
        }

        if (grlistValid.size() >= 1) {
            List<GenomeResidue> outlist = new ArrayList<GenomeResidue>();
            for (GenomeResidueInput gr : grlistValid) {
                System.out.println("Out:\t" + gr.getEnsembl().getSeqId() + "\t:"
                        + Integer.toString(gr.getResidue().getResidueNum()));
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
    */

}
