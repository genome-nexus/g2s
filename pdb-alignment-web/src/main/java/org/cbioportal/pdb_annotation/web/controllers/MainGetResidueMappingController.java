package org.cbioportal.pdb_annotation.web.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.cbioportal.pdb_annotation.web.domain.AlignmentRepository;
import org.cbioportal.pdb_annotation.web.domain.EnsemblRepository;
import org.cbioportal.pdb_annotation.web.domain.GeneSequenceRepository;
import org.cbioportal.pdb_annotation.web.domain.UniprotRepository;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.Ensembl;
import org.cbioportal.pdb_annotation.web.models.GenomeResidue;
import org.cbioportal.pdb_annotation.web.models.GenomeResidueInput;
import org.cbioportal.pdb_annotation.web.models.Residue;
import org.cbioportal.pdb_annotation.web.models.ResidueEnsembl;
import org.cbioportal.pdb_annotation.web.models.ResiduePresent;
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
 * Main controller getResidueMapping: Get ResidueMapping
 * 
 * @author Juexin Wang
 *
 */
@RestController // shorthand for @Controller, @ResponseBody
@CrossOrigin(origins = "*") // allow all cross-domain requests
@Api(tags = "GetResidueMapping", description = "ensembl/uniprot/hgvs/sequence")
@RequestMapping(value = "/g2s/")
public class MainGetResidueMappingController {

    final static Logger log = Logger.getLogger(MainGetResidueMappingController.class);

    // getResidueMapping/{id_type}:{id}, `id_type` can be `ensembl`, `uniprot`,
    // `hgvs`
    // getResidueMapping/{id_type}:{id}/pdb:{pdb_id}_{chain_id}

    @Autowired
    private GeneSequenceRepository geneSequenceRepository;
    @Autowired
    private UniprotRepository uniprotRepository;
    @Autowired
    private SeqIdAlignmentController seqController;
    @Autowired
    private EnsemblRepository ensemblRepository;
    @Autowired
    private AlignmentRepository alignmentRepository;

    @RequestMapping(value = "/getResidueMapping/{id_type}:{id:.+}/{position}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Residue Mapping")
    public List<Residue> getResidueMapping(
            @ApiParam(required = true, value = "Input id_type: ensembl; uniprot; uniprot_isoform; hgvs; hgvs38") @PathVariable String id_type,
            @ApiParam(required = true, value = "Input id e.g. ensembl:ENSP00000484409.1/ENSG00000141510.16/ENST00000504290.5; "
                    + "uniprot:P04637/P53_HUMAN; "
                    + "uniprot_isoform:P04637_9/P53_HUMAN_9; "
                    + "hgvs:17_79478130C; "
                    + "hgvs38:17_7676594T") @PathVariable String id,
            @ApiParam(required = true, value = "Input Residue Position e.g. 100; Anynumber for hgvs") @PathVariable String position) {

        List<Residue> outList = new ArrayList<Residue>();
        if (id_type.equals("ensembl")) {
            if (id.startsWith("ENSP")) {// EnsemblID:
                // ENSP00000484409.1/ENSP00000484409
                List<Ensembl> ensembllist = ensemblRepository.findByEnsemblIdStartingWith(id);
                for (Ensembl ensembl : ensembllist) {
                    outList.addAll(seqController.getPdbResidueBySeqId(ensembl.getSeqId(), position));
                }
            } else if (id.startsWith("ENSG")) {// EnsemblGene:
                // ENSG00000141510.16
                List<Ensembl> ensembllist = ensemblRepository.findByEnsemblGene(id);
                if (ensembllist.size() >= 1) {
                    for (Ensembl en : ensembllist) {
                        outList.addAll(seqController.getPdbResidueBySeqId(en.getSeqId(), position));
                    }
                }
            } else if (id.startsWith("ENST")) {// EnsemblTranscript:
                // ENST00000504290.5
                List<Ensembl> ensembllist = ensemblRepository.findByEnsemblTranscript(id);
                if (ensembllist.size() >= 1) {
                    for (Ensembl en : ensembllist) {
                        outList.addAll(seqController.getPdbResidueBySeqId(en.getSeqId(), position));
                    }
                }
            } else {
                log.info("Error in Input. id_type:Ensembl id: " + id + " position:" + position);
            }

        } else if (id_type.equals("uniprot")) {
            if (id.length() == 6 && id.split("_").length != 2) {// Accession:
                // P04637
                outList.addAll(getPdbResidueByUniprotAccession(id, position));

            } else if (id.split("_").length == 2) {// ID: P53_HUMAN
                outList.addAll(getPdbResidueByUniprotId(id, position));
            } else {
                log.info("Error in Input. id_type:Uniprot id: " + id + " position:" + position);
            }

        } else if (id_type.equals("uniprot_isoform")) {
            if (id.split("_").length == 2 && id.split("_")[0].length() == 6) {// Accession:
                // P04637
                outList.addAll(getPdbResidueByUniprotAccessionIso(id.split("_")[0], id.split("_")[1], position));

            } else if (id.split("_").length == 3) {// ID: P53_HUMAN
                outList.addAll(getPdbResidueByUniprotIdIso(id.split("_")[0]+"_"+id.split("_")[1], id.split("_")[2], position));

            } else {
                log.info("Error in Input. id_type:Uniprot_isoform id: " + id);
            }
        } else if (id_type.equals("hgvs")) {
            // http://annotation.genomenexus.org/hgvs/CHROMSOME:g.POSITIONORIGINAL%3EMUTATION?isoformOverrideSource=uniprot&summary=summary

            String genomeVersion = "GRCH37";

            String chromosomeNum = id.split("_")[0];
            String tmp = id.split("_")[1];
            long pos = Long.parseLong(tmp.substring(0, tmp.length() - 1));
            String nucleotideType = tmp.substring(tmp.length() - 1, tmp.length());
            
            System.out.println(chromosomeNum+" "+pos+" "+nucleotideType+" "+genomeVersion);
            outList.addAll(getPdbResidueByEnsemblIdGenome(chromosomeNum, pos, nucleotideType, genomeVersion));

        } else if (id_type.equals("hgvs38")) {
            // http://rest.ensembl.org/vep/human/hgvs/CHROMSOME:g.POSITIONORIGINAL%3EMUTATION?content-type=application/json&protein=1
            String genomeVersion = "GRCH38";

            String chromosomeNum = id.split("_")[0];
            String tmp = id.split("_")[1];
            long pos = Long.parseLong(tmp.substring(0, tmp.length() - 1));
            String nucleotideType = tmp.substring(tmp.length() - 1, tmp.length());
            System.out.println(chromosomeNum+" "+pos+" "+nucleotideType+" "+genomeVersion);
            outList.addAll(getPdbResidueByEnsemblIdGenome(chromosomeNum, pos, nucleotideType, genomeVersion));

        } else {
            log.info("Error in Input. id_type:" + id_type + " id: " + id + " position:" + position);
        }
        return outList;
    }

    @RequestMapping(value = "/getResidueMapping/{id_type}:{id:.+}/{position}/pdb:{pdb_id}_{chain_id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get Residue Mapping by id, PDBId and Chain")
    public List<Residue> getResidueMappingByPDB(
            @ApiParam(required = true, value = "Input id_type: ensembl; uniprot; uniprot_isoform; hgvs; hgvs38") @PathVariable String id_type,
            @ApiParam(required = true, value = "Input id e.g. ensembl:ENSP00000484409.1/ENSG00000141510.16/ENST00000504290.5; "
                    + "uniprot:P04637/P53_HUMAN; "
                    + "uniprot_isoform:P04637_9/P53_HUMAN_9; "
                    + "hgvs:17_79478130C; "
                    + "hgvs38:17_7676594T") @PathVariable String id,
            @ApiParam(required = true, value = "Input Residue Position e.g. 100; Anynumber for hgvs") @PathVariable String position,
            @ApiParam(required = true, value = "Input PDB Id e.g. 2fej") @PathVariable String pdb_id,
            @ApiParam(required = true, value = "Input Chain e.g. A") @PathVariable String chain_id) {

        ArrayList<Residue> outList = new ArrayList<Residue>();
        if (id_type.equals("ensembl")) {

            if (id.startsWith("ENSP")) {// EnsemblID:
                // ENSP00000484409.1/ENSP00000484409

                List<Ensembl> ensembllist = ensemblRepository.findByEnsemblIdStartingWith(id);
                for (Ensembl ensembl : ensembllist) {

                    List<Residue> list = seqController.getPdbResidueBySeqId(ensembl.getSeqId(), position);
                    List<Residue> alilist = new ArrayList<Residue>();

                    for (Residue ali : list) {
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
                    List<Residue> list = seqController.getPdbResidueBySeqId(ensembllist.get(0).getSeqId(), position);

                    for (Residue ali : list) {
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
                    List<Residue> list = seqController.getPdbResidueBySeqId(ensembllist.get(0).getSeqId(), position);

                    for (Residue ali : list) {
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

                List<Uniprot> uniprotList = uniprotRepository.findByUniprotAccession(id);
                List<Residue> list = new ArrayList<Residue>();
                for (Uniprot entry : uniprotList) {
                    list.addAll(seqController.getPdbResidueBySeqId(entry.getSeqId(), position));
                }
                for (Residue re : list) {
                    String pd = re.getPdbId().toLowerCase();
                    String ch = re.getChain().toLowerCase();
                    if (pd.equals(pdb_id.toLowerCase()) && ch.equals(chain_id.toLowerCase())) {
                        outList.add(re);
                    }
                }

            } else if (id.split("_").length == 2) {// ID: P53_HUMAN

                List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(id);

                Set<String> uniprotAccSet = new HashSet<String>();
                for (Uniprot uniprot : uniprotList) {
                    uniprotAccSet.add(uniprot.getUniprotAccession());
                }

                List<Residue> outlist = new ArrayList<Residue>();
                Iterator<String> it = uniprotAccSet.iterator();
                while (it.hasNext()) {
                    outlist.addAll(getPdbResidueByUniprotAccession(it.next(), position));
                }

                for (Residue residue : outlist) {
                    String pd = residue.getPdbId().toLowerCase();
                    String ch = residue.getChain().toLowerCase();
                    if (pd.equals(pdb_id.toLowerCase()) && ch.equals(chain_id.toLowerCase())) {
                        outList.add(residue);
                    }
                }

            } else {
                log.info("Error in Input. id_type:Uniprot id: " + id + " By PDB:" + pdb_id + " id:" + chain_id);
            }

        } else if (id_type.equals("uniprot_isoform")) {
            if (id.split("_").length == 2 && id.split("_")[0].length() == 6) {// Accession:
                // P04637
                List<Residue> list = getPdbResidueByUniprotAccessionIso(id.split("_")[0], id.split("_")[1], position);
                for (Residue re : list) {
                    String pd = re.getPdbId().toLowerCase();
                    String ch = re.getChain().toLowerCase();
                    if (pd.equals(pdb_id.toLowerCase()) && ch.equals(chain_id.toLowerCase())) {
                        outList.add(re);
                    }
                }

            } else if (id.split("_").length == 3) {// ID: P53_HUMAN
                List<Residue> list = getPdbResidueByUniprotIdIso(id.split("_")[0]+"_"+id.split("_")[1], id.split("_")[2], position);
                for (Residue re : list) {
                    String pd = re.getPdbId().toLowerCase();
                    String ch = re.getChain().toLowerCase();
                    if (pd.equals(pdb_id.toLowerCase()) && ch.equals(chain_id.toLowerCase())) {
                        outList.add(re);
                    }
                }

            } else {
                log.info("Error in Input. id_type:Uniprot_isoform id: " + id);
            }

        } else if (id_type.equals("hgvs")) {
            String genomeVersion = "GRCH37";

            String chromosomeNum = id.split("_")[0];
            String tmp = id.split("_")[1];
            long pos = Long.parseLong(tmp.substring(0, tmp.length() - 1));
            String nucleotideType = tmp.substring(tmp.length() - 1, tmp.length() );

            List<Residue> tmpList = getPdbResidueByEnsemblIdGenome(chromosomeNum, pos, nucleotideType, genomeVersion);

            for (Residue residue : tmpList) {
                String pd = residue.getPdbId().toLowerCase();
                String ch = residue.getChain().toLowerCase();
                if (pd.equals(pdb_id.toLowerCase()) && ch.equals(chain_id.toLowerCase())) {
                    outList.add(residue);
                }
            }

        } else if (id_type.equals("hgvs38")) {
            String genomeVersion = "GRCH38";

            String chromosomeNum = id.split("_")[0];
            String tmp = id.split("_")[1];
            long pos = Long.parseLong(tmp.substring(0, tmp.length() - 1));
            String nucleotideType = tmp.substring(tmp.length() - 1, tmp.length());
            List<Residue> tmpList = getPdbResidueByEnsemblIdGenome(chromosomeNum, pos, nucleotideType, genomeVersion);

            for (Residue residue : tmpList) {
                String pd = residue.getPdbId().toLowerCase();
                String ch = residue.getChain().toLowerCase();
                if (pd.equals(pdb_id.toLowerCase()) && ch.equals(chain_id.toLowerCase())) {
                    outList.add(residue);
                }
            }

        } else {
            log.info("Error in Input. id_type:" + id_type + " id: " + id + " position:" + position + " PDB:" + pdb_id
                    + " ChainID:" + chain_id);
        }
        return outList;
    }
    
    
    // Query by AlignmentId, get all the Residue Mapping
    @RequestMapping(value = "/ResidueMappingFromAlignmentId/{alignmentId:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get All Residue Mapping by AlignmentId")
    public List<ResiduePresent> getResidueMappingByAlignmentId(
            @ApiParam(required = true, value = "Input AlignmentId e.g. 883556") @PathVariable int alignmentId) {

        Alignment ali = alignmentRepository.findByAlignmentId(alignmentId);
        List<ResiduePresent> residueList = new ArrayList<ResiduePresent>();

        for (int i = ali.getSeqFrom(); i <= ali.getSeqTo(); i++) {
            ResiduePresent residue = new ResiduePresent();
            residue.setInputNum(i);
            residue.setInputName(ali.getSeqAlign().substring(i - ali.getSeqFrom(), i - ali.getSeqFrom() + 1));
            residue.setResidueNum(Integer.parseInt(ali.getSegStart()) - 1 + ali.getPdbFrom() + (i - ali.getSeqFrom()));
            residue.setResidueName(ali.getPdbAlign().substring(i - ali.getSeqFrom(), i - ali.getSeqFrom() + 1));
            residueList.add(residue);
        }
        return residueList;
    }
    

    // Implementation of API etPdbResidueByEnsemblIdGenome
    private List<Residue> getPdbResidueByEnsemblIdGenome(String chromosomeNum, long position, String nucleotideType,
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
            List<Residue> outlist = new ArrayList<Residue>();
            for (GenomeResidueInput gr : grlistValid) {
                // System.out.println("Out:\t" + gr.getEnsembl().getSeqId() +
                // "\t:"
                // + Integer.toString(gr.getResidue().getResidueNum()));
                List<Residue> list = seqController.getPdbResidueBySeqId(gr.getEnsembl().getSeqId(),
                        Integer.toString(gr.getResidue().getResidueNum()));
                outlist.addAll(list);
            }
            return outlist;
        } else {
            return null;
        }
    }

    // P04637, 100
    public List<Residue> getPdbResidueByUniprotAccession(String uniprotAccession, String position) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotAccession(uniprotAccession);
        ArrayList<Residue> outList = new ArrayList<Residue>();
        for (Uniprot entry : uniprotList) {
            outList.addAll(seqController.getPdbResidueBySeqId(entry.getSeqId(), position));
        }
        return outList;
    }

    // P53_HUMAN 100
    public List<Residue> getPdbResidueByUniprotId(String uniprotId, String position) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);

        Set<String> uniprotAccSet = new HashSet<String>();
        for (Uniprot uniprot : uniprotList) {
            uniprotAccSet.add(uniprot.getUniprotAccession());
        }

        List<Residue> outlist = new ArrayList<Residue>();
        Iterator<String> it = uniprotAccSet.iterator();
        while (it.hasNext()) {
            outlist.addAll(getPdbResidueByUniprotAccession(it.next(), position));
        }

        return outlist;

    }

    // P04637_9 100
    public List<Residue> getPdbResidueByUniprotAccessionIso(String uniprotAccession, String isoform, String position) {

        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotAccessionIso(uniprotAccession + "_" + isoform);
        if (uniprotlist.size() == 1) {
            return seqController.getPdbResidueBySeqId(uniprotlist.get(0).getSeqId(), position);
        } else {
            return new ArrayList<Residue>();
        }
    }

    // P53_HUMAN_9 100
    public List<Residue> getPdbResidueByUniprotIdIso(String uniprotId, String isoform, String position) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);

        Set<String> uniprotAccSet = new HashSet<String>();
        for (Uniprot uniprot : uniprotList) {
            uniprotAccSet.add(uniprot.getUniprotAccession());
        }

        List<Residue> outlist = new ArrayList<Residue>();
        Iterator<String> it = uniprotAccSet.iterator();
        while (it.hasNext()) {
            outlist.addAll(getPdbResidueByUniprotAccessionIso(it.next(), isoform, position));
        }

        return outlist;
    }
}
