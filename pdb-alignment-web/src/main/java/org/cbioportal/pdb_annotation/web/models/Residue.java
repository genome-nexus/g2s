package org.cbioportal.pdb_annotation.web.models;

public class Residue extends Alignment{
    
    private int residueNum;
    
    private String residueName;

    public int getResidueNum() {
        return residueNum;
    }

    public void setResidueNum(int residueNum) {
        this.residueNum = residueNum;
    }

    public String getResidueName() {
        return residueName;
    }

    public void setResidueName(String residueName) {
        this.residueName = residueName;
    }

}
