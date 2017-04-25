package org.cbioportal.pdb_annotation.web.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.cbioportal.pdb_annotation.scripts.PdbScriptsPipelineRunCommand;
import org.cbioportal.pdb_annotation.web.domain.AlignmentRepository;
import org.cbioportal.pdb_annotation.web.domain.EnsemblRepository;
import org.cbioportal.pdb_annotation.web.domain.GeneSequenceRepository;
import org.cbioportal.pdb_annotation.web.domain.UniprotRepository;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.AlignmentEnsembl;
import org.cbioportal.pdb_annotation.web.models.Ensembl;
import org.cbioportal.pdb_annotation.web.models.GenomeResidueInput;
import org.cbioportal.pdb_annotation.web.models.Residue;
import org.cbioportal.pdb_annotation.web.models.Uniprot;
import org.cbioportal.pdb_annotation.web.models.api.UtilAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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
@Api(tags = "GetAlignments", description = "ensembl/uniprot")
@RequestMapping(value = "/g2s/")
public class MainGetAlignmentsController {
    final static Logger log = Logger.getLogger(MainGetAlignmentsController.class);

    // getAlignments/{id_type}:{id}, `id_type` can be `ensembl`, `uniprot`,
    // `hgvs`
    // getAlignments/{id_type}:{id}/pdb:{pdb_id}_{chain_id}

    @Autowired
    private AlignmentRepository alignmentRepository;
    @Autowired
    private UniprotRepository uniprotRepository;
    @Autowired
    private EnsemblRepository ensemblRepository;

    @RequestMapping(value = "/getAlignments/{id_type}:{id:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments")
    public List<Alignment> getAlignment(
            @ApiParam(required = true, value = "Input id_type: ensembl; uniprot; uniprot_isoform") @PathVariable String id_type,
            @ApiParam(required = true, value = "Input id e.g. ensembl:ENSP00000484409.1/ENSG00000141510.16/ENST00000504290.5; "
                                    +"uniprot:P04637/P53_HUMAN; uniprot_isoform:P04637_9/P53_HUMAN_9 ") @PathVariable String id) {
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
                        System.out.println("en.getSeqId():"+en.getSeqId());
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
                outList.addAll(getPdbAlignmentByUniprotAccession(id));

            } else if (id.split("_").length == 2) {// ID: P53_HUMAN
                outList.addAll(getPdbAlignmentByUniprotId(id));
            } else {
                log.info("Error in Input. id_type:Uniprot id: " + id);
            }
        } else if (id_type.equals("uniprot_isoform")) {
            if (id.split("_").length == 2 && id.split("_")[0].length() == 6) {// Accession: P04637
                outList.addAll(getPdbAlignmentByUniprotAccessionIso(id.split("_")[0], id.split("_")[1]));

            } else if (id.split("_").length == 3) {// ID: P53_HUMAN
                outList.addAll(getPdbAlignmentByUniprotIdIso(id.split("_")[0]+"_"+id.split("_")[1], id.split("_")[2]));
            } else {
                log.info("Error in Input. id_type:Uniprot_isoform id: " + id);
            }
        } else {
            log.info("Error in Input. id_type:" + id_type + " id: " + id);
        }
        return outList;
    }

    @RequestMapping(value = "/getAlignments/{id_type}:{id:.+}/pdb:{pdb_id}_{chain_id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by id, PDBId and Chain")
    public List<Alignment> getAlignmentByPDB(
            @ApiParam(required = true, value = "Input id_type: ensembl; uniprot; uniprot_isoform") @PathVariable String id_type,
            @ApiParam(required = true, value = "Input id e.g. ensembl:ENSP00000484409.1/ENSG00000141510.16/ENST00000504290.5; "
                                    +"uniprot:P04637/P53_HUMAN; uniprot_isoform:P04637_9/P53_HUMAN_9 ") @PathVariable String id,
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
                outList.addAll(getPdbAlignmentByUniprotAccession(id, pdb_id, chain_id));
            } else if (id.split("_").length == 2) {// ID: P53_HUMAN
                outList.addAll(getPdbAlignmentByUniprotId(id, pdb_id, chain_id));
            } else {
                log.info("Error in Input. id_type:Uniprot id: " + id + " By PDB:" + pdb_id + " id:" + chain_id);
            }
        } else if (id_type.equals("uniprot_isoform")) {
            if (id.split("_").length == 2 && id.split("_")[0].length() == 6) {// Accession:
                // P04637_9
                outList.addAll(
                        getPdbAlignmentByUniprotAccessionIso(id.split("_")[0], id.split("_")[1], pdb_id, chain_id));
            } else if (id.split("_").length == 3) {// ID: P53_HUMAN_9
                outList.addAll(getPdbAlignmentByUniprotIdIso(id.split("_")[0]+"_"+id.split("_")[1], id.split("_")[2], pdb_id, chain_id));
            } else {
                log.info("Error in Input. id_type:Uniprot_isoform id: " + id);
            }
        } else {
            log.info("Error in Input. id_type:" + id_type + " id: " + id + " By PDB:" + pdb_id + " id:" + chain_id);
        }
        return outList;
    }

    // P04637
    public List<Alignment> getPdbAlignmentByUniprotAccession(String uniprotAccession) {
        List<Uniprot> uniprotList = uniprotRepository.findByUniprotAccession(uniprotAccession);
        ArrayList<Alignment> outList = new ArrayList<Alignment>();
        for (Uniprot entry : uniprotList) {
            outList.addAll(alignmentRepository.findBySeqId(entry.getSeqId()));
        }
        return outList;
    }

    // P04637 2fej A
    public List<Alignment> getPdbAlignmentByUniprotAccession(String uniprotAccession, String pdbId, String chain) {

        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotAccession(uniprotAccession);
        if (uniprotlist.size() > 0) {
            List<Alignment> list = alignmentRepository.findBySeqId(uniprotlist.get(0).getSeqId());
            List<Alignment> outlist = new ArrayList<Alignment>();

            for (Alignment ali : list) {
                String pd = ali.getPdbId().toLowerCase();
                String ch = ali.getChain().toLowerCase();
                if (pd.equals(pdbId.toLowerCase()) && ch.equals(chain.toLowerCase())) {
                    outlist.add(ali);
                }
            }
            return outlist;
        } else {
            return null;
        }
    }

    // P53_HUMAN
    //
    public List<Alignment> getPdbAlignmentByUniprotId(String uniprotId) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);

        Set<String> uniprotAccSet = new HashSet<String>();
        for (Uniprot uniprot : uniprotList) {
            uniprotAccSet.add(uniprot.getUniprotAccession());
        }

        List<Alignment> outlist = new ArrayList<Alignment>();
        Iterator<String> it = uniprotAccSet.iterator();
        while (it.hasNext()) {
            outlist.addAll(getPdbAlignmentByUniprotAccession(it.next()));
        }

        return outlist;
    }

    // P53_HUMAN 2fej A
    public List<Alignment> getPdbAlignmentByUniprotId(String uniprotId, String pdbId, String chain) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);

        Set<String> uniprotAccSet = new HashSet<String>();
        for (Uniprot uniprot : uniprotList) {
            uniprotAccSet.add(uniprot.getUniprotAccession());
        }

        List<Alignment> outlist = new ArrayList<Alignment>();
        Iterator<String> it = uniprotAccSet.iterator();
        while (it.hasNext()) {
            outlist.addAll(getPdbAlignmentByUniprotAccession(it.next(), pdbId, chain));
        }

        return outlist;
    }

    // P04637_9
    public List<Alignment> getPdbAlignmentByUniprotAccessionIso(String uniprotAccession, String isoform) {

        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotAccessionIso(uniprotAccession + "_" + isoform);
        if (uniprotlist.size() == 1) {
            System.out.println(uniprotlist.get(0).getSeqId());
            return alignmentRepository.findBySeqId(uniprotlist.get(0).getSeqId());
        } else {
            return new ArrayList<Alignment>();
        }
    }

    // P04637_9 2fej A
    public List<Alignment> getPdbAlignmentByUniprotAccessionIso(String uniprotAccession, String isoform, String pdbId,
            String chain) {

        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotAccessionIso(uniprotAccession + "_" + isoform);
        if (uniprotlist.size() == 1) {
            List<Alignment> list = alignmentRepository.findBySeqId(uniprotlist.get(0).getSeqId());
            List<Alignment> outlist = new ArrayList<Alignment>();

            for (Alignment ali : list) {
                String pd = ali.getPdbId().toLowerCase();
                String ch = ali.getChain().toLowerCase();
                if (pd.equals(pdbId.toLowerCase()) && ch.equals(chain.toLowerCase())) {
                    outlist.add(ali);
                }
            }
            return outlist;
        } else {
            return null;
        }
    }

    // P53_HUMAN_9
    public List<Alignment> getPdbAlignmentByUniprotIdIso(String uniprotId, String isoform) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);

        Set<String> uniprotAccSet = new HashSet<String>();
        for (Uniprot uniprot : uniprotList) {
            uniprotAccSet.add(uniprot.getUniprotAccession());
        }

        List<Alignment> outlist = new ArrayList<Alignment>();
        Iterator<String> it = uniprotAccSet.iterator();
        while (it.hasNext()) {
            outlist.addAll(getPdbAlignmentByUniprotAccessionIso(it.next(), isoform));
        }

        return outlist;

    }

    // P53_HUMAN_9 2fej A
    public List<Alignment> getPdbAlignmentByUniprotIdIso(String uniprotId, String isoform, String pdbId, String chain) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);

        Set<String> uniprotAccSet = new HashSet<String>();
        for (Uniprot uniprot : uniprotList) {
            uniprotAccSet.add(uniprot.getUniprotAccession());
        }

        List<Alignment> outlist = new ArrayList<Alignment>();
        Iterator<String> it = uniprotAccSet.iterator();
        while (it.hasNext()) {
            outlist.addAll(getPdbAlignmentByUniprotAccessionIso(it.next(), isoform, pdbId, chain));
        }

        return outlist;
    }

}
