package org.cbioportal.pdb_annotation.web.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.cbioportal.pdb_annotation.web.domain.AlignmentRepository;
import org.cbioportal.pdb_annotation.web.domain.EnsemblRepository;
import org.cbioportal.pdb_annotation.web.domain.GeneSequenceRepository;
import org.cbioportal.pdb_annotation.web.domain.UniprotRepository;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.Ensembl;
import org.cbioportal.pdb_annotation.web.models.GenomeResidueInput;
import org.cbioportal.pdb_annotation.web.models.Alignments;
import org.cbioportal.pdb_annotation.web.models.Uniprot;
import org.cbioportal.pdb_annotation.web.models.api.UtilAPI;
import org.cbioportal.pdb_annotation.web.models.ResidueMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 *
 * Controller of the API: Input Protein SeqId, inner control
 *
 * @author Juexin Wang
 *
 */
@RestController // shorthand for @Controller, @ResponseBody
// @CrossOrigin(origins = "*") // allow all cross-domain requests
// @Api(tags = "QueryInnerID", description = "Inner ID")
// @RequestMapping(value = "/api/")
public class SeqIdAlignmentController {

    @Autowired
    private AlignmentRepository alignmentRepository;
    @Autowired
    private GeneSequenceRepository geneSequenceRepository;
    @Autowired
    private UniprotRepository uniprotRepository;
    @Autowired
    private EnsemblRepository ensemblRepository;
    @Autowired
    private SeqIdAlignmentController seqController;

    @RequestMapping(value = "/GeneSeqStructureMapping/{seqId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by Protein SeqId")
    public List<Alignment> getPdbAlignmentByGeneSequenceId(
            @ApiParam(required = true, value = "Input SeqId e.g. 25625") @PathVariable String seqId) {
        return alignmentRepository.findBySeqId(seqId);
    }

    // Query from EnsemblId
    @ApiOperation(value = "Get PDB Alignments by EnsemblId", nickname = "EnsemblStructureMappingQuery")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Success", response = Alignment.class, responseContainer = "List"),
            @ApiResponse(code = 400, message = "Bad Request") })
    @RequestMapping(value = "/EnsemblStructureMappingQueryT", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public String getPdbAlignmentByEnsemblId(
            @RequestParam @ApiParam(value = "Input Ensembl Id e.g. ENSP00000489609.1", required = true, allowMultiple = true) String ensemblId) {
        return ensemblId + "SS";
    }

    @RequestMapping(value = "/GeneSeqStructureMapping/{seqId}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by Protein SeqId")
    public List<Alignment> getPdbAlignmentByGeneSequenceIdPOST(
            @ApiParam(required = true, value = "Input SeqId e.g. 25625") @PathVariable String seqId) {
        return alignmentRepository.findBySeqId(seqId);
    }

    @RequestMapping(value = "/GeneSeqRecognition/{seqId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Whether Protein SeqId Exists")
    public boolean getExistedSeqIdinAlignment(
            @ApiParam(required = true, value = "Input SeqId e.g. 25625") @PathVariable String seqId) {
        return geneSequenceRepository.findBySeqId(seqId).size() != 0;
    }

    /**
     * "Get All Residue Mapping by Protein SeqId
     * 
     * @param seqId
     * @return
     */
    public List<Alignments> getPdbResidueBySeqId(
            @ApiParam(required = true, value = "Input SeqId e.g. 25625") @PathVariable String seqId) {
        List<Alignment> it = alignmentRepository.findBySeqId(seqId);
        List<Alignments> outit = new ArrayList<Alignments>();

        for (Alignment ali : it) {
            Alignments rm = new Alignments();
            List<ResidueMapping> residueMapping = new ArrayList<ResidueMapping>();
            for (int inputAA = ali.getSeqFrom(); inputAA <= ali.getSeqTo(); inputAA++) {
                ResidueMapping rp = new ResidueMapping();

                rp.setQueryAminoAcid(
                        ali.getSeqAlign().substring(inputAA - ali.getSeqFrom(), inputAA - ali.getSeqFrom() + 1));
                rp.setQueryPosition(inputAA - ali.getSeqFrom() + 1);

                rp.setPdbAminoAcid(
                        ali.getPdbAlign().substring(inputAA - ali.getSeqFrom(), inputAA - ali.getSeqFrom() + 1));
                rp.setPdbPosition(
                        Integer.parseInt(ali.getSegStart()) - 1 + ali.getPdbFrom() + (inputAA - ali.getSeqFrom()));
                residueMapping.add(rp);
            }
            try {
                BeanUtils.copyProperties(rm, ali);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            rm.setResidueMapping(residueMapping);
            outit.add(rm);

        }
        return outit;
    }

    /**
     * "Get Residue Mapping by Protein SeqId and Residue Positions
     * 
     * @param seqId
     * @param positionList
     * @return
     */
    public List<Alignments> getPdbResidueBySeqId(
            @ApiParam(required = true, value = "Input SeqId e.g. 25625") @PathVariable String seqId,
            @ApiParam(required = true, value = "Input Residue Position e.g. 100") @PathVariable String position) {
        List<Alignment> it = alignmentRepository.findBySeqId(seqId);
        List<Alignments> outit = new ArrayList<Alignments>();
        int inputAA = Integer.parseInt(position);
        for (Alignment ali : it) {
            if (inputAA >= ali.getSeqFrom() && inputAA <= ali.getSeqTo()) {
                Alignments rm = new Alignments();
                ResidueMapping rp = new ResidueMapping();
                List<ResidueMapping> residueMapping = new ArrayList<ResidueMapping>();
                rp.setQueryAminoAcid(
                        ali.getSeqAlign().substring(inputAA - ali.getSeqFrom(), inputAA - ali.getSeqFrom() + 1));
                rp.setQueryPosition(inputAA);
                rp.setPdbPosition(
                        Integer.parseInt(ali.getSegStart()) - 1 + ali.getPdbFrom() + (inputAA - ali.getSeqFrom()));
                rp.setPdbAminoAcid(
                        ali.getPdbAlign().substring(inputAA - ali.getSeqFrom(), inputAA - ali.getSeqFrom() + 1));
                residueMapping.add(rp);               
                try {
                    BeanUtils.copyProperties(rm, ali);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                rm.setResidueMapping(residueMapping);
                outit.add(rm);
            }
        }
        return outit;
    }

    /**
     * "Get Residue Mapping by Protein SeqId and Residue Positions list
     * 
     * @param seqId
     * @param positionList
     * @return
     */
    public List<Alignments> getPdbResidueBySeqId(
            @ApiParam(required = true, value = "Input SeqId e.g. 25625") @PathVariable String seqId,
            @ApiParam(required = true, value = "Input Residue Position e.g. 99,100") @PathVariable List<String> positionList) {
        List<Alignment> it = alignmentRepository.findBySeqId(seqId);
        List<Alignments> outit = new ArrayList<Alignments>();

        for (Alignment ali : it) {
            Alignments rm = new Alignments();

            List<ResidueMapping> residueMapping = new ArrayList<ResidueMapping>();

            for (String position : positionList) {
                int inputAA = Integer.parseInt(position);
                if (inputAA >= ali.getSeqFrom() && inputAA <= ali.getSeqTo()) {

                    ResidueMapping rp = new ResidueMapping();
                    rp.setQueryAminoAcid(
                            ali.getSeqAlign().substring(inputAA - ali.getSeqFrom(), inputAA - ali.getSeqFrom() + 1));
                    rp.setQueryPosition(inputAA);
                    rp.setPdbPosition(
                            Integer.parseInt(ali.getSegStart()) - 1 + ali.getPdbFrom() + (inputAA - ali.getSeqFrom()));
                    rp.setPdbAminoAcid(
                            ali.getPdbAlign().substring(inputAA - ali.getSeqFrom(), inputAA - ali.getSeqFrom() + 1));
                    residueMapping.add(rp);

                }
            }
            try {
                BeanUtils.copyProperties(rm, ali);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            rm.setResidueMapping(residueMapping);
            outit.add(rm);
        }

        return outit;
    }

    /*
     * Support function for alignments
     */

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

    /**
     * ResidueMapping parts support
     */

    // Implementation of API etPdbResidueByEnsemblIdGenome
    public List<Alignments> getPdbResidueByEnsemblIdGenome(String chromosomeNum, long position,
            String nucleotideType, String genomeVersion) {
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

            for (Ensembl ensembl : ensembllist) {
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
            List<Alignments> outlist = new ArrayList<Alignments>();
            for (GenomeResidueInput gr : grlistValid) {
                // System.out.println("Out:\t" + gr.getEnsembl().getSeqId() +
                // "\t:"
                // + Integer.toString(gr.getResidue().getResidueNum()));
                List<Alignments> list = seqController.getPdbResidueBySeqId(gr.getEnsembl().getSeqId(),
                        Integer.toString(gr.getResidue().getResidueNum()));
                outlist.addAll(list);
            }
            return outlist;
        } else {
            return null;
        }
    }

    // P04637
    public List<Alignments> getPdbResidueByUniprotAccession(String uniprotAccession) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotAccession(uniprotAccession);
        ArrayList<Alignments> outList = new ArrayList<Alignments>();
        for (Uniprot entry : uniprotList) {
            outList.addAll(seqController.getPdbResidueBySeqId(entry.getSeqId()));
        }
        return outList;
    }

    // P04637, 100
    public List<Alignments> getPdbResidueByUniprotAccession(String uniprotAccession, String position) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotAccession(uniprotAccession);
        ArrayList<Alignments> outList = new ArrayList<Alignments>();
        for (Uniprot entry : uniprotList) {
            outList.addAll(seqController.getPdbResidueBySeqId(entry.getSeqId(), position));
        }
        return outList;
    }

    // P04637, 99,100
    public List<Alignments> getPdbResidueByUniprotAccession(String uniprotAccession, List<String> positionList) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotAccession(uniprotAccession);
        ArrayList<Alignments> outList = new ArrayList<Alignments>();
        for (Uniprot entry : uniprotList) {
            outList.addAll(seqController.getPdbResidueBySeqId(entry.getSeqId(), positionList));
        }
        return outList;
    }

    // P53_HUMAN
    public List<Alignments> getPdbResidueByUniprotId(String uniprotId) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);

        Set<String> uniprotAccSet = new HashSet<String>();
        for (Uniprot uniprot : uniprotList) {
            uniprotAccSet.add(uniprot.getUniprotAccession());
        }

        List<Alignments> outlist = new ArrayList<Alignments>();
        Iterator<String> it = uniprotAccSet.iterator();
        while (it.hasNext()) {
            outlist.addAll(getPdbResidueByUniprotAccession(it.next()));
        }

        return outlist;

    }

    // P53_HUMAN 100
    public List<Alignments> getPdbResidueByUniprotId(String uniprotId, String position) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);

        Set<String> uniprotAccSet = new HashSet<String>();
        for (Uniprot uniprot : uniprotList) {
            uniprotAccSet.add(uniprot.getUniprotAccession());
        }

        List<Alignments> outlist = new ArrayList<Alignments>();
        Iterator<String> it = uniprotAccSet.iterator();
        while (it.hasNext()) {
            outlist.addAll(getPdbResidueByUniprotAccession(it.next(), position));
        }

        return outlist;

    }

    // P53_HUMAN 99,100
    public List<Alignments> getPdbResidueByUniprotId(String uniprotId, List<String> positionList) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);

        Set<String> uniprotAccSet = new HashSet<String>();
        for (Uniprot uniprot : uniprotList) {
            uniprotAccSet.add(uniprot.getUniprotAccession());
        }

        List<Alignments> outlist = new ArrayList<Alignments>();
        Iterator<String> it = uniprotAccSet.iterator();
        while (it.hasNext()) {
            outlist.addAll(getPdbResidueByUniprotAccession(it.next(), positionList));
        }

        return outlist;

    }

    // P04637_9
    public List<Alignments> getPdbResidueByUniprotAccessionIso(String uniprotAccession, String isoform) {

        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotAccessionIso(uniprotAccession + "_" + isoform);
        if (uniprotlist.size() == 1) {
            return seqController.getPdbResidueBySeqId(uniprotlist.get(0).getSeqId());
        } else {
            return new ArrayList<Alignments>();
        }
    }

    // P04637_9 100
    public List<Alignments> getPdbResidueByUniprotAccessionIso(String uniprotAccession, String isoform,
            String position) {

        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotAccessionIso(uniprotAccession + "_" + isoform);
        if (uniprotlist.size() == 1) {
            return seqController.getPdbResidueBySeqId(uniprotlist.get(0).getSeqId(), position);
        } else {
            return new ArrayList<Alignments>();
        }
    }

    // P04637_9 99,100
    public List<Alignments> getPdbResidueByUniprotAccessionIso(String uniprotAccession, String isoform,
            List<String> positionList) {

        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotAccessionIso(uniprotAccession + "_" + isoform);
        if (uniprotlist.size() == 1) {
            return seqController.getPdbResidueBySeqId(uniprotlist.get(0).getSeqId(), positionList);
        } else {
            return new ArrayList<Alignments>();
        }
    }

    // P53_HUMAN_9
    public List<Alignments> getPdbResidueByUniprotIdIso(String uniprotId, String isoform) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);

        Set<String> uniprotAccSet = new HashSet<String>();
        for (Uniprot uniprot : uniprotList) {
            uniprotAccSet.add(uniprot.getUniprotAccession());
        }

        List<Alignments> outlist = new ArrayList<Alignments>();
        Iterator<String> it = uniprotAccSet.iterator();
        while (it.hasNext()) {
            outlist.addAll(getPdbResidueByUniprotAccessionIso(it.next(), isoform));
        }

        return outlist;
    }

    // P53_HUMAN_9 100
    public List<Alignments> getPdbResidueByUniprotIdIso(String uniprotId, String isoform, String position) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);

        Set<String> uniprotAccSet = new HashSet<String>();
        for (Uniprot uniprot : uniprotList) {
            uniprotAccSet.add(uniprot.getUniprotAccession());
        }

        List<Alignments> outlist = new ArrayList<Alignments>();
        Iterator<String> it = uniprotAccSet.iterator();
        while (it.hasNext()) {
            outlist.addAll(getPdbResidueByUniprotAccessionIso(it.next(), isoform, position));
        }

        return outlist;
    }

    // P53_HUMAN_9 99,100
    public List<Alignments> getPdbResidueByUniprotIdIso(String uniprotId, String isoform,
            List<String> positionList) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);

        Set<String> uniprotAccSet = new HashSet<String>();
        for (Uniprot uniprot : uniprotList) {
            uniprotAccSet.add(uniprot.getUniprotAccession());
        }

        List<Alignments> outlist = new ArrayList<Alignments>();
        Iterator<String> it = uniprotAccSet.iterator();
        while (it.hasNext()) {
            outlist.addAll(getPdbResidueByUniprotAccessionIso(it.next(), isoform, positionList));
        }

        return outlist;
    }

}
