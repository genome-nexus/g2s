package org.cbioportal.pdb_annotation.web.controllers;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.cbioportal.pdb_annotation.scripts.PdbScriptsPipelineRunCommand;
import org.cbioportal.pdb_annotation.util.ReadConfig;
import org.cbioportal.pdb_annotation.web.domain.AlignmentRepository;
import org.cbioportal.pdb_annotation.web.domain.EnsemblRepository;
import org.cbioportal.pdb_annotation.web.domain.GeneSequenceRepository;
import org.cbioportal.pdb_annotation.web.domain.UniprotRepository;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.AlignmentEnsembl;
import org.cbioportal.pdb_annotation.web.models.BlastStatistics;
import org.cbioportal.pdb_annotation.web.models.CompleteAlignment;
import org.cbioportal.pdb_annotation.web.models.CompleteResidue;
import org.cbioportal.pdb_annotation.web.models.Ensembl;
import org.cbioportal.pdb_annotation.web.models.GenomeResidueInput;
import org.cbioportal.pdb_annotation.web.models.InputAlignment;
import org.cbioportal.pdb_annotation.web.models.InputSequence;
import org.cbioportal.pdb_annotation.web.models.ProteinSequenceAlignment;
import org.cbioportal.pdb_annotation.web.models.ProteinSequenceParam;
import org.cbioportal.pdb_annotation.web.models.ProteinSequenceParamResidue;
import org.cbioportal.pdb_annotation.web.models.ProteinSequenceResidue;
import org.cbioportal.pdb_annotation.web.models.Residue;
import org.cbioportal.pdb_annotation.web.models.ResidueResult;
import org.cbioportal.pdb_annotation.web.models.ResidueMapping;
import org.cbioportal.pdb_annotation.web.models.Uniprot;
import org.cbioportal.pdb_annotation.web.models.api.UtilAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * 
 * Main controller getAlignments: Get Alignments
 * 
 * @author Juexin Wang
 *
 */
@RestController // shorthand for @Controller, @ResponseBody
@CrossOrigin(origins = "*") // allow all cross-domain requests
@Api(tags = "Get Alignments", description = "ensembl/uniprot/sequences")
@RequestMapping(value = "/api/")
public class MainGetAlignmentsController {
    final static Logger log = Logger.getLogger(MainGetAlignmentsController.class);

    @Autowired
    private AlignmentRepository alignmentRepository;
    @Autowired
    private EnsemblRepository ensemblRepository;
    @Autowired
    private SeqIdAlignmentController seqController;

    @RequestMapping(value = "/alignments/{id_type}/{id:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by ProteinId")
    public List<Alignment> getAlignment(
            @ApiParam(required = true, value = "Input id_type: ensembl; uniprot; uniprot_isoform") @PathVariable String id_type,
            @ApiParam(required = true, value = "Input id e.g.\n"
                    + "ensembl:ENSP00000484409.1/ENSG00000141510.16/ENST00000504290.5; "
                    + "uniprot:P04637/P53_HUMAN; uniprot_isoform:P04637_9/P53_HUMAN_9 ") @PathVariable String id) {
        ArrayList<Alignment> outList = new ArrayList<Alignment>();
        if (id_type.equals("ensembl")) {
            if (id.startsWith("ENSP")) {// EnsemblID:
                // ENSP00000484409.1/ENSP00000484409
                List<Ensembl> ensembllist = ensemblRepository.findByEnsemblIdStartingWith(id);
                for (Ensembl ensembl : ensembllist) {
                    outList.addAll(alignmentRepository.findBySeqId(ensembl.getSeqId()));
                }
            } else if (id.startsWith("ENSG")) {// EnsemblGene:
                // ENSG00000141510.16
                List<Ensembl> ensembllist = ensemblRepository.findByEnsemblGene(id);
                if (ensembllist.size() >= 1) {
                    for (Ensembl en : ensembllist) {
                        System.out.println("en.getSeqId():" + en.getSeqId());
                        outList.addAll(alignmentRepository.findBySeqId(en.getSeqId()));
                    }
                }
            } else if (id.startsWith("ENST")) {// EnsemblTranscript:
                // ENST00000504290.5
                List<Ensembl> ensembllist = ensemblRepository.findByEnsemblTranscript(id);
                if (ensembllist.size() >= 1) {
                    for (Ensembl en : ensembllist) {
                        outList.addAll(alignmentRepository.findBySeqId(en.getSeqId()));
                    }
                }
            } else {
                log.info("Error in Input. id_type:Ensembl id: " + id);
            }
        } else if (id_type.equals("uniprot")) {
            if (id.length() == 6 && id.split("_").length != 2) {// Accession:
                // P04637
                outList.addAll(seqController.getPdbAlignmentByUniprotAccession(id));

            } else if (id.split("_").length == 2) {// ID: P53_HUMAN
                outList.addAll(seqController.getPdbAlignmentByUniprotId(id));
            } else {
                log.info("Error in Input. id_type:Uniprot id: " + id);
            }
        } else if (id_type.equals("uniprot_isoform")) {
            if (id.split("_").length == 2 && id.split("_")[0].length() == 6) {// Accession:
                // P04637
                outList.addAll(seqController.getPdbAlignmentByUniprotAccessionIso(id.split("_")[0], id.split("_")[1]));

            } else if (id.split("_").length == 3) {// ID: P53_HUMAN
                outList.addAll(seqController.getPdbAlignmentByUniprotIdIso(id.split("_")[0] + "_" + id.split("_")[1],
                        id.split("_")[2]));
            } else {
                log.info("Error in Input. id_type:Uniprot_isoform id: " + id);
            }
        } else {
            log.info("Error in Input. id_type:" + id_type + " id: " + id);
        }
        return outList;
    }

    @RequestMapping(value = "/alignments/{id_type}/{id:.+}/pdb/{pdb_id}_{chain_id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by ProteinId, PDBId and Chain")
    public List<Alignment> getAlignmentByPDB(
            @ApiParam(required = true, value = "Input id_type: ensembl; uniprot; uniprot_isoform") @PathVariable String id_type,
            @ApiParam(required = true, value = "Input id e.g.\n"
                    + "ensembl:ENSP00000484409.1/ENSG00000141510.16/ENST00000504290.5; "
                    + "uniprot:P04637/P53_HUMAN; uniprot_isoform:P04637_9/P53_HUMAN_9 ") @PathVariable String id,
            @ApiParam(required = true, value = "Input PDB Id e.g. 2fej") @PathVariable String pdb_id,
            @ApiParam(required = true, value = "Input Chain e.g. A") @PathVariable String chain_id) {

        ArrayList<Alignment> outList = new ArrayList<Alignment>();
        if (id_type.equals("ensembl")) {

            if (id.startsWith("ENSP")) {// EnsemblID:
                // ENSP00000484409.1/ENSP00000484409

                List<Ensembl> ensembllist = ensemblRepository.findByEnsemblIdStartingWith(id);
                for (Ensembl ensembl : ensembllist) {

                    List<Alignment> list = alignmentRepository.findBySeqId(ensembl.getSeqId());
                    List<Alignment> alilist = new ArrayList<Alignment>();

                    for (Alignment ali : list) {
                        String pd = ali.getPdbId().toLowerCase();
                        String ch = ali.getChain().toLowerCase();
                        if (pd.equals(pdb_id.toLowerCase()) && ch.equals(chain_id.toLowerCase())) {
                            alilist.add(ali);
                        }
                    }
                    outList.addAll(alilist);
                }

            } else if (id.startsWith("ENSG")) {// EnsemblGene:
                // ENSG00000141510.16
                List<Ensembl> ensembllist = ensemblRepository.findByEnsemblGene(id);
                if (ensembllist.size() > 0) {
                    List<Alignment> list = alignmentRepository.findBySeqId(ensembllist.get(0).getSeqId());

                    for (Alignment ali : list) {
                        String pd = ali.getPdbId().toLowerCase();
                        String ch = ali.getChain().toLowerCase();
                        if (pd.equals(pdb_id.toLowerCase()) && ch.equals(chain_id.toLowerCase())) {
                            outList.add(ali);
                        }
                    }
                }

            } else if (id.startsWith("ENST")) {// EnsemblTranscript:
                // ENST00000504290.5
                List<Ensembl> ensembllist = ensemblRepository.findByEnsemblTranscript(id);
                if (ensembllist.size() > 0) {
                    List<Alignment> list = alignmentRepository.findBySeqId(ensembllist.get(0).getSeqId());

                    for (Alignment ali : list) {
                        String pd = ali.getPdbId().toLowerCase();
                        String ch = ali.getChain().toLowerCase();
                        if (pd.equals(pdb_id.toLowerCase()) && ch.equals(chain_id.toLowerCase())) {
                            outList.add(ali);
                        }
                    }
                }

            } else {
                log.info("Error in Input. id_type:Uniprot id: " + id + " By PDB:" + pdb_id + " id:" + chain_id);
            }
        } else if (id_type.equals("uniprot")) {
            if (id.length() == 6 && id.split("_").length != 2) {// Accession:
                // P04637
                outList.addAll(seqController.getPdbAlignmentByUniprotAccession(id, pdb_id, chain_id));
            } else if (id.split("_").length == 2) {// ID: P53_HUMAN
                outList.addAll(seqController.getPdbAlignmentByUniprotId(id, pdb_id, chain_id));
            } else {
                log.info("Error in Input. id_type:Uniprot id: " + id + " By PDB:" + pdb_id + " id:" + chain_id);
            }
        } else if (id_type.equals("uniprot_isoform")) {
            if (id.split("_").length == 2 && id.split("_")[0].length() == 6) {// Accession:
                // P04637_9
                outList.addAll(seqController.getPdbAlignmentByUniprotAccessionIso(id.split("_")[0], id.split("_")[1],
                        pdb_id, chain_id));
            } else if (id.split("_").length == 3) {// ID: P53_HUMAN_9
                outList.addAll(seqController.getPdbAlignmentByUniprotIdIso(id.split("_")[0] + "_" + id.split("_")[1],
                        id.split("_")[2], pdb_id, chain_id));
            } else {
                log.info("Error in Input. id_type:Uniprot_isoform id: " + id);
            }
        } else {
            log.info("Error in Input. id_type:" + id_type + " id: " + id + " By PDB:" + pdb_id + " id:" + chain_id);
        }
        return outList;
    }

    @RequestMapping(value = "/alignments", method = { RequestMethod.GET,
            RequestMethod.POST }, produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    @ApiOperation("Get PDB Alignments by Protein Sequence")
    public List<Alignment> getPdbAlignmentBySequence(HttpServletRequest request,
            @ApiParam(required = true, value = "Input Protein Sequence: ETGQSVNDPGNMSFVKETVDKLLKGYDIRLRPDFGGPP") @RequestParam String sequence,
            @ApiParam(required = false, value = "Default Blast Parameters:\n"
                    + " Evalue=1e-10,Wordsize=3,Gapopen=11,Gapextend=1,\n" + " Matrix=BLOSUM62,Comp_based_stats=2,\n"
                    + "Threshold=11,Windowsize=40") @RequestParam(required = false) List<String> paramList
    /*
     * @ApiParam(value = "Blast Parameters Evalue: (Default) 1e-10") String
     * evalue,
     * 
     * @ApiParam(value = "Blast Parameters Wordsize: (Default) 3") String
     * wordsize,
     * 
     * @ApiParam(value = "Blast Parameters Gapopen: (Default) 11") String
     * gapopen,
     * 
     * @ApiParam(value = "Blast Parameters Gapextend: (Default) 1") String
     * gapextend,
     * 
     * @ApiParam(value = "Blast Parameters Matrix: (Default) BLOSUM62") String
     * matrix,
     * 
     * @ApiParam(value = "Blast Parameters Comp_based_stats: (Default) 2")
     * String compbasedstats,
     * 
     * @ApiParam(value = "Blast Parameters Threshold: (Default) 11") String
     * threshold,
     * 
     * @ApiParam(value = "Blast Parameters Windowsize: (Default) 40") String
     * windowsize
     */) {

        InputSequence inputsequence = new InputSequence();
        inputsequence.setSequence(sequence);
        inputsequence.setResidueNumList(null);

        ReadConfig rc = ReadConfig.getInstance();

        // Use default first
        inputsequence.setEvalue(rc.getBlastEvalue());
        inputsequence.setWord_size(rc.getBlastWordsize());
        inputsequence.setGapopen(rc.getBlastGapopen());
        inputsequence.setGapextend(rc.getBlastGapextend());
        inputsequence.setMatrix(rc.getBlastMatrix());
        inputsequence.setComp_based_stats(rc.getBlastComp());
        inputsequence.setThreshold(rc.getBlastThreshold());
        inputsequence.setWindow_size(rc.getBlastWindowsize());

        // If have user defined input, use them
        if (paramList != null) {
            for (String param : paramList) {
                String tmp[] = param.split("=");
                if (tmp[0].equals("Evalue")) {
                    inputsequence.setEvalue(tmp[1]);
                } else if (tmp[0].equals("Wordsize")) {
                    inputsequence.setWord_size(tmp[1]);
                } else if (tmp[0].equals("Gapopen")) {
                    inputsequence.setGapopen(tmp[1]);
                } else if (tmp[0].equals("Gapextend")) {
                    inputsequence.setGapextend(tmp[1]);
                } else if (tmp[0].equals("Matrix")) {
                    inputsequence.setMatrix(tmp[1]);
                } else if (tmp[0].equals("Comp_based_stats")) {
                    inputsequence.setComp_based_stats(tmp[1]);
                } else if (tmp[0].equals("Threshold")) {
                    inputsequence.setThreshold(tmp[1]);
                } else if (tmp[0].equals("Windowsize")) {
                    inputsequence.setWindow_size(tmp[1]);
                }
            }
        }

        PdbScriptsPipelineRunCommand pdbScriptsPipelineRunCommand = new PdbScriptsPipelineRunCommand();
        List<InputAlignment> alignments = pdbScriptsPipelineRunCommand.runCommand(inputsequence);

        List<Alignment> result = new ArrayList<Alignment>();

        for (InputAlignment ali : alignments) {
            Alignment re = new Alignment();
            re.setAlignmentId(ali.getAlignmentId());
            re.setBitscore((float) ali.getBitscore());
            re.setChain(ali.getChain());
            re.setEvalue(Double.toString(ali.getEvalue()));
            re.setIdentity(ali.getIdentity());
            re.setIdentityPositive(ali.getIdentp());
            re.setMidlineAlign(ali.getMidlineAlign());
            re.setPdbAlign(ali.getPdbAlign());
            re.setPdbFrom(ali.getPdbFrom());
            re.setPdbId(ali.getPdbId());
            re.setPdbNo(ali.getPdbNo());
            re.setPdbSeg(ali.getPdbSeg());
            re.setPdbTo(ali.getPdbTo());
            re.setSeqAlign(ali.getSeqAlign());
            re.setSeqFrom(ali.getSeqFrom());
            re.setSeqId(ali.getSeqId());
            re.setSeqTo(ali.getSeqTo());
            re.setSegStart(ali.getSegStart());

            DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
            Date today = Calendar.getInstance().getTime();
            re.setUpdateDate(df.format(today));

            result.add(re);
        }

        return result;
    }

}
