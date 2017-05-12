package org.cbioportal.pdb_annotation.web.models;

import java.util.List;

public class ProteinSequenceParamResidue extends ProteinSequenceParam{
    
    private List<String> residueNumList;

    private List<String> residueNameList;

    public List<String> getResidueNumList() {
        return residueNumList;
    }

    public void setResidueNumList(List<String> residueNumList) {
        this.residueNumList = residueNumList;
    }

    public List<String> getResidueNameList() {
        return residueNameList;
    }

    public void setResidueNameList(List<String> residueNameList) {
        this.residueNameList = residueNameList;
    }
    
    

}
