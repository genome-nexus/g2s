package org.cbioportal.pdb_annotation.web.models;

import java.util.List;

public class GenomeResidue extends Ensembl {

    private List<Residue> alignments;

    public List<Residue> getAlignments() {
        return alignments;
    }

    public void setAlignments(List<Residue> alignments) {
        this.alignments = alignments;
    }

}
