package org.cbioportal.pdb_annotation.web.controllers;

import java.util.ArrayList;
import java.util.List;

import org.cbioportal.pdb_annotation.web.domain.AlignmentRepository;
import org.cbioportal.pdb_annotation.web.domain.EnsemblRepository;
import org.cbioportal.pdb_annotation.web.domain.GeneSequenceRepository;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.Ensembl;
import org.cbioportal.pdb_annotation.web.models.Residue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
 * Controller of the API: Input Ensembl
 *
 * @author Juexin Wang
 *
 */
@RestController // shorthand for @Controller, @ResponseBody
@CrossOrigin(origins = "*") // allow all cross-domain requests
@Api(tags = "Human Ensembl", description = " ")
@RequestMapping(value = "/g2s/")
public class EnsemblIdAlignmentController {

    @Autowired
    private AlignmentRepository alignmentRepository;
    @Autowired
    private GeneSequenceRepository geneSequenceRepository;
    @Autowired
    private EnsemblRepository ensemblRepository;
    @Autowired
    private SeqIdAlignmentController seqController;

    // Query from EnsemblId, As EnsemblId is a unique Id, it only contains one
    // results from seq_id, which is also unique
    @RequestMapping(value = "/EnsemblStructureMappingEnsemblId/{ensemblId:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by EnsemblId")
    public List<Alignment> getPdbAlignmentByEnsemblId(
            @ApiParam(required = true, value = "Input Ensembl Id e.g. ENSP00000484409.1") @PathVariable String ensemblId) {

        System.out.println(ensemblId);
        List<Ensembl> ensembllist = ensemblRepository.findByEnsemblId(ensemblId);
        System.out.println(ensembllist.size());
        if (ensembllist.size() == 1) {
            return alignmentRepository.findBySeqId(ensembllist.get(0).getSeqId());
        } else {
            return null;
        }

    }

    @RequestMapping(value = "/EnsemblRecognitionEnsemblId/{ensemblId:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Whether EnsemblId exists")
    public boolean getExistedEnsemblIdinAlignment(
            @ApiParam(required = true, value = "Input Ensembl Id e.g. ENSP00000484409.1") @PathVariable String ensemblId) {
        List<Ensembl> ensembllist = ensemblRepository.findByEnsemblId(ensemblId);
        if (ensembllist.size() == 1) {
            return geneSequenceRepository.findBySeqId(ensembllist.get(0).getSeqId()).size() != 0;
        } else {
            return false;
        }
    }

    @RequestMapping(value = "/EnsemblResidueMappingEnsemblId/{ensemblId:.+}/{position}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get Residue Mapping by EnsemblId and Residue Number")
    public List<Residue> getPdbResidueByEnsemblId(
            @ApiParam(required = true, value = "Input Ensembl Id e.g. ENSP00000484409.1") @PathVariable String ensemblId,
            @ApiParam(required = true, value = "Input Residue Position e.g. 100") @PathVariable String position) {

        List<Ensembl> ensembllist = ensemblRepository.findByEnsemblId(ensemblId);

        if (ensembllist.size() == 1) {
            return seqController.getPdbResidueBySeqId(ensembllist.get(0).getSeqId(), position);
        } else {
            return null;
        }
    }
    
    @RequestMapping(value = "/EnsemblPdbStructureMappingEnsemblId/{ensemblId:.+}/{pdbId}/{chain}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by EnsemblId, PdbId and Chain")
    public List<Alignment> getPdbAlignmentByEnsemblId(
            @ApiParam(required = true, value = "Input Ensembl Id e.g. ENSP00000484409.1") @PathVariable String ensemblId,
            @ApiParam(required = true, value = "Input PDB Id e.g. 2fej") @PathVariable String pdbId,
            @ApiParam(required = true, value = "Input Chain e.g. A") @PathVariable String chain) {

        List<Ensembl> ensembllist = ensemblRepository.findByEnsemblId(ensemblId);
        if(ensembllist.size() == 1){
            List<Alignment> list =alignmentRepository.findBySeqId(ensembllist.get(0).getSeqId());
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

    // Query from EnsemblGene, these are not unique, so the return results are
    // multiple, different with the former uniqueID
    @RequestMapping(value = "/EnsemblStructureMappingEnsemblGene/{ensemblGene:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by EnsemblGene")
    public List<Alignment> getPdbAlignmentByEnsemblGene(
            @ApiParam(required = true, value = "Input Ensembl Gene e.g. ENSG00000141510.16") @PathVariable String ensemblGene) {
        System.out.println(ensemblGene);
        List<Ensembl> ensembllist = ensemblRepository.findByEnsemblGene(ensemblGene);
        System.out.println(ensembllist.size());
        if (ensembllist.size() >= 1) {
            List<Alignment> outList = new ArrayList<Alignment>();
            for (Ensembl en : ensembllist) {
                outList.addAll(alignmentRepository.findBySeqId(en.getSeqId()));
            }
            return outList;
        } else {
            return null;
        }
    }

    @RequestMapping(value = "/EnsemblRecognitionEnsemblGene/{ensemblGene:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Whether EnsemblGene exists")
    public boolean getExistedEnsemblGeneinAlignment(
            @ApiParam(required = true, value = "Input Ensembl Gene e.g. ENSG00000141510.16") @PathVariable String ensemblGene) {
        List<Ensembl> ensembllist = ensemblRepository.findByEnsemblGene(ensemblGene);
        if (ensembllist.size() >= 1) {
            return geneSequenceRepository.findBySeqId(ensembllist.get(0).getSeqId()).size() != 0;
        } else {
            return false;
        }
    }

    @RequestMapping(value = "/EnsemblResidueMappingEnsemblGene/{ensemblGene:.+}/{position}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get Residue Mapping by EnsemblGene and Residue Number")
    public List<Residue> getPdbResidueByEnsemblGene(
            @ApiParam(required = true, value = "Input Ensembl Gene e.g. ENSG00000141510.16") @PathVariable String ensemblGene,
            @ApiParam(required = true, value = "Input Residue Position e.g. 100") @PathVariable String position) {
        List<Ensembl> ensembllist = ensemblRepository.findByEnsemblGene(ensemblGene);
        if (ensembllist.size() >= 1) {
            List<Residue> outList = new ArrayList<Residue>();
            for (Ensembl en : ensembllist) {
                outList.addAll(seqController.getPdbResidueBySeqId(en.getSeqId(), position));
            }
            return outList;
        } else {
            return null;
        }
    }
    
    
    @RequestMapping(value = "/EnsemblPdbStructureMappingEnsemblGene/{ensemblGene:.+}/{pdbId}/{chain}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by EnsemblGene, PdbId and Chain")
    public List<Alignment> getPdbAlignmentByEnsemblGene(
            @ApiParam(required = true, value = "Input Ensembl Gene e.g. ENSG00000141510.16") @PathVariable String ensemblGene,
            @ApiParam(required = true, value = "Input PDB Id e.g. 2fej") @PathVariable String pdbId,
            @ApiParam(required = true, value = "Input Chain e.g. A") @PathVariable String chain) {

        List<Ensembl> ensembllist = ensemblRepository.findByEnsemblGene(ensemblGene);
        if(ensembllist.size()>0){
            List<Alignment> list = alignmentRepository.findBySeqId(ensembllist.get(0).getSeqId());
            List<Alignment> outlist = new ArrayList<Alignment>();
            
            for(Alignment ali:list){
                String pd = ali.getPdbId().toLowerCase();
                String ch = ali.getChain().toLowerCase();
                if(pd.equals(pdbId.toLowerCase()) && ch.equals(chain.toLowerCase())){
                    outlist.add(ali);
                }
            }
            return outlist;
        }
        else{
            return null;
        }
    }

    // Query from EnsemblTranscript, these are not unique, so the return results
    // are multiple, different with the former uniqueID
    @RequestMapping(value = "/EnsemblStructureMappingEnsemblTranscript/{ensemblTranscript:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by EnsemblTranscript")
    public List<Alignment> getPdbAlignmentByEnsemblTranscript(
            @ApiParam(required = true, value = "Input Ensembl Transcript e.g. ENST00000504290.5") @PathVariable String ensemblTranscript) {
        System.out.println(ensemblTranscript);
        List<Ensembl> ensembllist = ensemblRepository.findByEnsemblTranscript(ensemblTranscript);
        System.out.println(ensembllist.size());
        if (ensembllist.size() >= 1) {
            List<Alignment> outList = new ArrayList<Alignment>();
            for (Ensembl en : ensembllist) {
                outList.addAll(alignmentRepository.findBySeqId(en.getSeqId()));
            }
            return outList;
        } else {
            return null;
        }
    }

    @RequestMapping(value = "/EnsemblRecognitionEnsemblTranscript/{ensemblTranscript:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Whether EnsemblTranscript exists")
    public boolean getExistedEnsemblTranscriptinAlignment(
            @ApiParam(required = true, value = "Input Ensembl Transcript e.g. ENST00000504290.5") @PathVariable String ensemblTranscript) {
        List<Ensembl> ensembllist = ensemblRepository.findByEnsemblTranscript(ensemblTranscript);
        if (ensembllist.size() >= 1) {
            return geneSequenceRepository.findBySeqId(ensembllist.get(0).getSeqId()).size() != 0;
        } else {
            return false;
        }
    }

    @RequestMapping(value = "/EnsemblResidueMappingEnsemblTranscript/{ensemblTranscript:.+}/{position}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get Residue Mapping by EnsemblTranscript and Residue Number")
    public List<Residue> getPdbResidueByEnsemblTranscript(
            @ApiParam(required = true, value = "Input Ensembl Transcript e.g. ENST00000504290.5") @PathVariable String ensemblTranscript,
            @ApiParam(required = true, value = "Input Residue Position e.g. 100") @PathVariable String position) {
        List<Ensembl> ensembllist = ensemblRepository.findByEnsemblTranscript(ensemblTranscript);
        if (ensembllist.size() >= 1) {
            List<Residue> outList = new ArrayList<Residue>();
            for (Ensembl en : ensembllist) {
                outList.addAll(seqController.getPdbResidueBySeqId(en.getSeqId(), position));
            }
            return outList;
        } else {
            return null;
        }
    }
    
    @RequestMapping(value = "/EnsemblPdbStructureMappingEnsemblTranscript/{ensemblTranscript:.+}/{pdbId}/{chain}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by EnsemblTranscript, PdbId and Chain")
    public List<Alignment> getPdbAlignmentByEnsemblTranscript(
            @ApiParam(required = true, value = "Input Ensembl Transcript e.g. ENST00000504290.5") @PathVariable String ensemblTranscript,
            @ApiParam(required = true, value = "Input PDB Id e.g. 2fej") @PathVariable String pdbId,
            @ApiParam(required = true, value = "Input Chain e.g. A") @PathVariable String chain) {

        List<Ensembl> ensembllist = ensemblRepository.findByEnsemblTranscript(ensemblTranscript);
        if(ensembllist.size()>0){
            List<Alignment> list = alignmentRepository.findBySeqId(ensembllist.get(0).getSeqId());
            List<Alignment> outlist = new ArrayList<Alignment>();
            
            for(Alignment ali:list){
                String pd = ali.getPdbId().toLowerCase();
                String ch = ali.getChain().toLowerCase();
                if(pd.equals(pdbId.toLowerCase()) && ch.equals(chain.toLowerCase())){
                    outlist.add(ali);
                }
            }
            return outlist;
        }
        else{
            return null;
        }
    }

}
