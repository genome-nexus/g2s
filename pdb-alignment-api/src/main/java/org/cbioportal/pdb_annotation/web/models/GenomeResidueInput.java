package org.cbioportal.pdb_annotation.web.models;

public class GenomeResidueInput {
    private Residue residue;
    private Ensembl ensembl;

    public Residue getResidue() {
        return residue;
    }

    public void setResidue(Residue residue) {
        this.residue = residue;
    }

    public Ensembl getEnsembl() {
        return ensembl;
    }

    public void setEnsembl(Ensembl ensembl) {
        this.ensembl = ensembl;
    }

}
