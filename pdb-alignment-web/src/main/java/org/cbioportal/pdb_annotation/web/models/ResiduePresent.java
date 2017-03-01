package org.cbioportal.pdb_annotation.web.models;

public class ResiduePresent {
    
    private int inputNum;
    
    private String inputName;
    
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

    public int getInputNum() {
        return inputNum;
    }

    public void setInputNum(int inputNum) {
        this.inputNum = inputNum;
    }

    public String getInputName() {
        return inputName;
    }

    public void setInputName(String inputName) {
        this.inputName = inputName;
    }
    

}
