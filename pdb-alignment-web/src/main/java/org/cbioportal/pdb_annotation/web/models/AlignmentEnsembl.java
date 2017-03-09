package org.cbioportal.pdb_annotation.web.models;

import java.util.List;

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
    
    
    
}
