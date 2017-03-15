package org.cbioportal.pdb_annotation.web.controllers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.cbioportal.pdb_annotation.web.domain.UniprotRepository;
import org.cbioportal.pdb_annotation.web.models.Alignment;
import org.cbioportal.pdb_annotation.web.models.Residue;
import org.cbioportal.pdb_annotation.web.models.Uniprot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
*
* Controller of the API: Input Uniprot Id
* As Uniprot has either Id and Accession, we choose to seperate ID and Accession, here is UniprotId endpoints
*
* @author Juexin Wang
*
*/
@RestController // shorthand for @Controller, @ResponseBody
@CrossOrigin(origins = "*") // allow all cross-domain requests
@Api(tags = "UniprotId", description = "Swissprot")
@RequestMapping(value = "/g2s/")
public class UniprotIdAlignmentController {
    @Autowired
    private UniprotRepository uniprotRepository;
    @Autowired
    private UniprotAccessionAlignmentController uniprotController;
    
    
    //Another group of end points, get uniprot id
    // Not Accession, but id
    @RequestMapping(value = "/UniprotIsoformStructureMappingUniprotId/{uniprotId}/{isoform}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by UniprotId and Isofrom")
    public List<Alignment> getPdbAlignmentByUniprotIdIso(
            @ApiParam(required = true, value = "Input Uniprot Id e.g. P53_HUMAN") @PathVariable String uniprotId,
            @ApiParam(required = true, value = "Input Isoform e.g. 9") @PathVariable String isoform) {
        
        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);
        
        Set<String> uniprotAccSet = new HashSet<String>();
        for(Uniprot uniprot:uniprotList){
            uniprotAccSet.add(uniprot.getUniprotAccession());
        }
        
        List<Alignment> outlist = new ArrayList<Alignment>();
        Iterator<String> it=uniprotAccSet.iterator();
        while(it.hasNext()){
            outlist.addAll(uniprotController.getPdbAlignmentByUniprotAccessionIso(it.next(), isoform));
        }
        
        return outlist;
        
    }

    
    @RequestMapping(value = "/UniprotIsoformRecognitionUniprotId/{uniprotId}/{isoform}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Whether Isoform of UniprotId Exists")
    public boolean getExistedUniprotIdIsoinAlignment(
            @ApiParam(required = true, value = "Input Uniprot Id e.g. P53_HUMAN") @PathVariable String uniprotId,
            @ApiParam(required = true, value = "Input Isoform e.g. 9") @PathVariable String isoform) {
        
        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);
        
        Set<String> uniprotAccSet = new HashSet<String>();
        for(Uniprot uniprot:uniprotList){
            uniprotAccSet.add(uniprot.getUniprotAccession());
        }
        
        Iterator<String> it=uniprotAccSet.iterator();
        while(it.hasNext()){
            if(uniprotController.getExistedUniprotAccessionIsoinAlignment(it.next(), isoform)){
                return true;
            }
        }
        return false;
        
    }

    @RequestMapping(value = "/UniprotIsoformResidueMappingUniprotId/{uniprotId}/{isoform}/{position}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get Residue Mapping by UniprotId, Isofrom and Residue Position")
    public List<Residue> getPdbResidueByUniprotIdIso(
            @ApiParam(required = true, value = "Input Uniprot Id e.g. P53_HUMAN") @PathVariable String uniprotId,
            @ApiParam(required = true, value = "Input Isoform e.g. 9") @PathVariable String isoform,
            @ApiParam(required = true, value = "Input Residue Position e.g. 100") @PathVariable String position) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);
        
        Set<String> uniprotAccSet = new HashSet<String>();
        for(Uniprot uniprot:uniprotList){
            uniprotAccSet.add(uniprot.getUniprotAccession());
        }
        
        List<Residue> outlist = new ArrayList<Residue>();
        Iterator<String> it=uniprotAccSet.iterator();
        while(it.hasNext()){
            outlist.addAll(uniprotController.getPdbResidueByUniprotAccessionIso(it.next(), isoform, position));
        }
        
        return outlist;
    }
    
    @RequestMapping(value = "/UniprotIsoformPdbStructureMappingUniprotId/{uniprotId:.+}/{isoform}/{pdbId}/{chain}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by UniprotId, Isoform, PdbId and Chain")
    public List<Alignment> getPdbAlignmentByUniprotIdIso(
            @ApiParam(required = true, value = "Input Uniprot Id e.g. P53_HUMAN") @PathVariable String uniprotId,
            @ApiParam(required = true, value = "Input Isoform e.g. 9") @PathVariable String isoform,
            @ApiParam(required = true, value = "Input PDB Id e.g. 2fej") @PathVariable String pdbId,
            @ApiParam(required = true, value = "Input Chain e.g. A") @PathVariable String chain) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);
        
        Set<String> uniprotAccSet = new HashSet<String>();
        for(Uniprot uniprot:uniprotList){
            uniprotAccSet.add(uniprot.getUniprotAccession());
        }
        
        List<Alignment> outlist = new ArrayList<Alignment>();
        Iterator<String> it=uniprotAccSet.iterator();
        while(it.hasNext()){
            outlist.addAll(uniprotController.getPdbAlignmentByUniprotAccessionIso(it.next(), isoform, pdbId, chain));
        }
        
        return outlist;      
    }

    // Query from UniprotId
    @RequestMapping(value = "/UniprotStructureMappingUniprotId/{uniprotId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by UniprotId")
    public List<Alignment> getPdbAlignmentByUniprotId(
            @ApiParam(required = true, value = "Input Uniprot Id e.g. P53_HUMAN") @PathVariable String uniprotId) {
        
        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);
        
        Set<String> uniprotAccSet = new HashSet<String>();
        for(Uniprot uniprot:uniprotList){
            uniprotAccSet.add(uniprot.getUniprotAccession());
        }
        
        List<Alignment> outlist = new ArrayList<Alignment>();
        Iterator<String> it=uniprotAccSet.iterator();
        while(it.hasNext()){
            outlist.addAll(uniprotController.getPdbAlignmentByUniprotAccession(it.next()));
        }
        
        return outlist;  
    }

    @RequestMapping(value = "/UniprotRecognitionUniprotId/{uniprotId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Whether UniprotId Exists")
    public boolean getExistedUniprotIdAlignment(
            @ApiParam(required = true, value = "Input Uniprot Id e.g. P53_HUMAN") @PathVariable String uniprotId) {
        
        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);
        
        Set<String> uniprotAccSet = new HashSet<String>();
        for(Uniprot uniprot:uniprotList){
            uniprotAccSet.add(uniprot.getUniprotAccession());
        }
        
        Iterator<String> it=uniprotAccSet.iterator();
        while(it.hasNext()){
            if(uniprotController.getExistedUniprotAccessioninAlignment(it.next())){
                return true;
            }
        }
        return false;
    }

    @RequestMapping(value = "/UniprotResidueMappingUniprotId/{uniprotId}/{position}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get Residue Mapping by UniprotId and Residue Position")
    public List<Residue> getPdbResidueByUniprotId(
            @ApiParam(required = true, value = "Input Uniprot Id e.g. P53_HUMAN") @PathVariable String uniprotId,
            @ApiParam(required = true, value = "Input Residue Position e.g. 100") @PathVariable String position) {

        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);
        
        Set<String> uniprotAccSet = new HashSet<String>();
        for(Uniprot uniprot:uniprotList){
            uniprotAccSet.add(uniprot.getUniprotAccession());
        }
        
        List<Residue> outlist = new ArrayList<Residue>();
        Iterator<String> it=uniprotAccSet.iterator();
        while(it.hasNext()){
            outlist.addAll(uniprotController.getPdbResidueByUniprotAccession(it.next(),position));
        }
        
        return outlist; 
        
    }
    
    @RequestMapping(value = "/UniprotPdbStructureMappingUniprotId/{uniprotId:.+}/{pdbId}/{chain}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by UniprotId, PdbId and Chain")
    public List<Alignment> getPdbAlignmentByUniprotId(
            @ApiParam(required = true, value = "Input Uniprot Id e.g. P53_HUMAN") @PathVariable String uniprotId,
            @ApiParam(required = true, value = "Input PDB Id e.g. 2fej") @PathVariable String pdbId,
            @ApiParam(required = true, value = "Input Chain e.g. A") @PathVariable String chain) {

        
        List<Uniprot> uniprotList = uniprotRepository.findByUniprotId(uniprotId);
        
        Set<String> uniprotAccSet = new HashSet<String>();
        for(Uniprot uniprot:uniprotList){
            uniprotAccSet.add(uniprot.getUniprotAccession());
        }
        
        List<Alignment> outlist = new ArrayList<Alignment>();
        Iterator<String> it=uniprotAccSet.iterator();
        while(it.hasNext()){
            outlist.addAll(uniprotController.getPdbAlignmentByUniprotAccession(it.next(), pdbId, chain));
        }
        
        return outlist;       
    }

}
