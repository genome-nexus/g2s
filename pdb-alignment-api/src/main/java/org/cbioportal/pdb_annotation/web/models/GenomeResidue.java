package org.cbioportal.pdb_annotation.web.models;

public class GenomeResidue extends Residue{
    
    private Ensembl ensembl;

    public Ensembl getEnsembl() {
        return ensembl;
    }

    public void setEnsembl(Ensembl ensembl) {
        this.ensembl = ensembl;
    }

}
