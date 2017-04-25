package org.cbioportal.pdb_annotation.web.models;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Left here for later usage, for one ensembl input, alignments output
 * @author wangjue
 *
 */
public class AlignmentEnsembl {
    
    //Primary KEY: EnsemblId
    private String EnsemblId;
    
    private List<Alignment> alignments;

    public String getEnsemblId() {
        return EnsemblId;
    }

    public void setEnsemblId(String ensemblId) {
        EnsemblId = ensemblId;
    }

    public List<Alignment> getAlignments() {
        return alignments;
    }

    public void setAlignments(List<Alignment> alignments) {
        this.alignments = alignments;
    }
    
    /**
     * The usage
    
    @RequestMapping(value = "/EnsemblStructureMappingEnsemblId/{ensemblId:.+}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation("Get PDB Alignments by EnsemblId")
    public List<AlignmentEnsembl> getPdbAlignmentByEnsemblId(
            @ApiParam(required = true, value = "Input Ensembl Id e.g. ENSP00000484409.1 or ENSP00000484409 ") @PathVariable String ensemblId) {

        List<Ensembl> ensembllist = ensemblRepository.findByEnsemblIdStartingWith(ensemblId);
        List<AlignmentEnsembl> outlist = new ArrayList<AlignmentEnsembl>();
        for(Ensembl ensembl:ensembllist){
            AlignmentEnsembl ae = new AlignmentEnsembl();
            ae.setEnsemblId(ensembl.getEnsemblid());
            ae.setAlignments(alignmentRepository.findBySeqId(ensembl.getSeqId()));
            outlist.add(ae);
        }
        return outlist;
    }
    
     */
    
    
}
