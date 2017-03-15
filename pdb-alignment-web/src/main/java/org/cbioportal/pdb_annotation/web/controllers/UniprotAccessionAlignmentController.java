package org.cbioportal.pdb_annotation.web.controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.cbioportal.pdb_annotation.web.domain.AlignmentRepository;
import org.cbioportal.pdb_annotation.web.domain.GeneSequenceRepository;
import org.cbioportal.pdb_annotation.web.domain.UniprotRepository;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.Ensembl;
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
 * Controller of the API: Input Uniprot Accession
 * As Uniprot has either Id and Accession, we choose to seperate ID and Accession, here is only Accession endpoints
 * We also support ID as seperate endpoints
 *
 * @author Juexin Wang
 *
 */
@RestController // shorthand for @Controller, @ResponseBody
@CrossOrigin(origins = "*") // allow all cross-domain requests
@Api(tags = "UniprotAccession", description = "Swissprot")
@RequestMapping(value = "/g2s/")
public class UniprotAccessionAlignmentController {
    @Autowired
    private AlignmentRepository alignmentRepository;
    @Autowired
    private GeneSequenceRepository geneSequenceRepository;
    @Autowired
    private UniprotRepository uniprotRepository;
    @Autowired
    private SeqIdAlignmentController seqController;

    // Query from UniprotAccessionIso
    @RequestMapping(value = "/UniprotIsoformStructureMappingUniprotAccession/{uniprotAccession}/{isoform}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by UniprotAccession and Isofrom")
    public List<Alignment> getPdbAlignmentByUniprotAccessionIso(
            @ApiParam(required = true, value = "Input Uniprot Accession e.g. P04637") @PathVariable String uniprotAccession,
            @ApiParam(required = true, value = "Input Isoform e.g. 9") @PathVariable String isoform) {
        
        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotAccessionIso(uniprotAccession + "_" + isoform);
        if (uniprotlist.size() == 1) {
            System.out.println(uniprotlist.get(0).getSeqId());
            return alignmentRepository.findBySeqId(uniprotlist.get(0).getSeqId());
        } else {
            return new ArrayList<Alignment>();
        }
    }

    @RequestMapping(value = "/UniprotIsoformRecognitionUniprotAccession/{uniprotAccession}/{isoform}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Whether Isoform of UniprotAccession Exists")
    public boolean getExistedUniprotAccessionIsoinAlignment(
            @ApiParam(required = true, value = "Input Uniprot Accession e.g. P04637") @PathVariable String uniprotAccession,
            @ApiParam(required = true, value = "Input Isoform e.g. 9") @PathVariable String isoform) {

        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotAccessionIso(uniprotAccession + "_" + isoform);      
        if (uniprotlist.size() == 1) {
            return geneSequenceRepository.findBySeqId(uniprotlist.get(0).getSeqId()).size() != 0;
        } else {
            return false;
        }
    }

    @RequestMapping(value = "/UniprotIsoformResidueMappingUniprotAccession/{uniprotAccession}/{isoform}/{position}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get Residue Mapping by UniprotAccession, Isofrom and Residue Position")
    public List<Residue> getPdbResidueByUniprotAccessionIso(
            @ApiParam(required = true, value = "Input Uniprot Accession e.g. P04637") @PathVariable String uniprotAccession,
            @ApiParam(required = true, value = "Input Isoform e.g. 9") @PathVariable String isoform,
            @ApiParam(required = true, value = "Input Residue Position e.g. 100") @PathVariable String position) {

        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotAccessionIso(uniprotAccession + "_" + isoform);
        if (uniprotlist.size() == 1) {
            return seqController.getPdbResidueBySeqId(uniprotlist.get(0).getSeqId(), position);
        } else {
            return new ArrayList<Residue>();
        }
    }
    
    @RequestMapping(value = "/UniprotIsoformPdbStructureMappingUniprotAccession/{uniprotAccession:.+}/{isoform}/{pdbId}/{chain}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by UniprotAccession, Isoform, PdbId and Chain")
    public List<Alignment> getPdbAlignmentByUniprotAccessionIso(
            @ApiParam(required = true, value = "Input Uniprot Accession e.g. P04637") @PathVariable String uniprotAccession,
            @ApiParam(required = true, value = "Input Isoform e.g. 9") @PathVariable String isoform,
            @ApiParam(required = true, value = "Input PDB Id e.g. 2fej") @PathVariable String pdbId,
            @ApiParam(required = true, value = "Input Chain e.g. A") @PathVariable String chain) {

        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotAccessionIso(uniprotAccession + "_" + isoform);
        if(uniprotlist.size() == 1){
            List<Alignment> list =alignmentRepository.findBySeqId(uniprotlist.get(0).getSeqId());
            List<Alignment> outlist = new ArrayList<Alignment>();
            
            for(Alignment ali:list){
                String pd = ali.getPdbId().toLowerCase();
                String ch = ali.getChain().toLowerCase();
                if(pd.equals(pdbId.toLowerCase()) && ch.equals(chain.toLowerCase())){
                    outlist.add(ali);
                }
            }
            return outlist;           
        }else{
            return null;
        }        
    }

    // Query from UniprotId
    @RequestMapping(value = "/UniprotStructureMappingUniprotAccession/{uniprotAccession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by UniprotAccession")
    public List<Alignment> getPdbAlignmentByUniprotAccession(
            @ApiParam(required = true, value = "Input Uniprot Accession e.g. P04637") @PathVariable String uniprotAccession) {
        List<Uniprot> uniprotList = uniprotRepository.findByUniprotAccession(uniprotAccession);
        ArrayList<Alignment> outList = new ArrayList<Alignment>();
        for (Uniprot entry : uniprotList) {
            outList.addAll(alignmentRepository.findBySeqId(entry.getSeqId()));
        }
        return outList;
    }

    @RequestMapping(value = "/UniprotRecognitionUniprotAccession/{uniprotAccession}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Whether UniprotAccession Exists")
    public boolean getExistedUniprotAccessioninAlignment(
            @ApiParam(required = true, value = "Input Uniprot Accession e.g. P04637") @PathVariable String uniprotAccession) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotAccession(uniprotAccession);
        ArrayList<Alignment> outList = new ArrayList<Alignment>();
        for (Uniprot entry : uniprotList) {
            outList.addAll(alignmentRepository.findBySeqId(entry.getSeqId()));
        }
        return outList.size() != 0;
    }

    @RequestMapping(value = "/UniprotResidueMappingUniprotAccession/{uniprotAccession}/{position}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get Residue Mapping by UniprotAccession and Residue Position")
    public List<Residue> getPdbResidueByUniprotAccession(
            @ApiParam(required = true, value = "Input Uniprot Accession e.g. P04637") @PathVariable String uniprotAccession,
            @ApiParam(required = true, value = "Input Residue Position e.g. 100") @PathVariable String position) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotAccession(uniprotAccession);
        ArrayList<Residue> outList = new ArrayList<Residue>();
        for (Uniprot entry : uniprotList) {
            outList.addAll(seqController.getPdbResidueBySeqId(entry.getSeqId(), position));
        }
        return outList;
    }
    
    @RequestMapping(value = "/UniprotPdbStructureMappingUniprotAccession/{uniprotAccession:.+}/{pdbId}/{chain}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by UniprotAccession, PdbId and Chain")
    public List<Alignment> getPdbAlignmentByUniprotAccession(
            @ApiParam(required = true, value = "Input Uniprot Accession e.g. P04637") @PathVariable String uniprotAccession,
            @ApiParam(required = true, value = "Input PDB Id e.g. 2fej") @PathVariable String pdbId,
            @ApiParam(required = true, value = "Input Chain e.g. A") @PathVariable String chain) {

        List<Uniprot> uniprotlist = uniprotRepository.findByUniprotAccession(uniprotAccession);
        if(uniprotlist.size() > 0){
            List<Alignment> list =alignmentRepository.findBySeqId(uniprotlist.get(0).getSeqId());
            List<Alignment> outlist = new ArrayList<Alignment>();
            
            for(Alignment ali:list){
                String pd = ali.getPdbId().toLowerCase();
                String ch = ali.getChain().toLowerCase();
                if(pd.equals(pdbId.toLowerCase()) && ch.equals(chain.toLowerCase())){
                    outlist.add(ali);
                }
            }
            return outlist;           
        }else{
            return null;
        }        
    }
    
}
