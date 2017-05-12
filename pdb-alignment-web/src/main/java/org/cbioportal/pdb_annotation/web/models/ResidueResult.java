package org.cbioportal.pdb_annotation.web.models;

import java.util.List;

public class ResidueResult {
    
    private Alignment alignment;
    
    private List<ResidueMapping> residueMapping;

    public Alignment getAlignment() {
        return alignment;
    }

    public void setAlignment(Alignment alignment) {
        this.alignment = alignment;
    }

    public List<ResidueMapping> getResidueMapping() {
        return residueMapping;
    }

    public void setResidueMapping(List<ResidueMapping> residueMapping) {
        this.residueMapping = residueMapping;
    }

}
