package org.cbioportal.pdb_annotation.web.models;

import java.util.List;

public class Alignments extends Alignment{

    private List<ResidueMapping> residueMapping;

    public List<ResidueMapping> getResidueMapping() {
        return residueMapping;
    }

    public void setResidueMapping(List<ResidueMapping> residueMapping) {
        this.residueMapping = residueMapping;
    }

}
