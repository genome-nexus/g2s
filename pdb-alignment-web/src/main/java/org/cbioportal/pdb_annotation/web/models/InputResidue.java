package org.cbioportal.pdb_annotation.web.models;

public class InputResidue extends InputAlignment {

    private String residueNum;

    private String residueName;

    public String getResidueNum() {
        return residueNum;
    }

    public void setResidueNum(String residueNum) {
        this.residueNum = residueNum;
    }

    public String getResidueName() {
        return residueName;
    }

    public void setResidueName(String residueName) {
        this.residueName = residueName;
    }

}
