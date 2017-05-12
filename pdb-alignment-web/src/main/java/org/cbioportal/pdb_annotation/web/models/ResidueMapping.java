package org.cbioportal.pdb_annotation.web.models;

public class ResidueMapping {
    
    private int queryPosition;
    
    private String queryAminoAcid;
    
    private int pdbPosition;

    private String pdbAminoAcid;

    public int getQueryPosition() {
        return queryPosition;
    }

    public void setQueryPosition(int queryPosition) {
        this.queryPosition = queryPosition;
    }

    public String getQueryAminoAcid() {
        return queryAminoAcid;
    }

    public void setQueryAminoAcid(String queryAminoAcid) {
        this.queryAminoAcid = queryAminoAcid;
    }

    public int getPdbPosition() {
        return pdbPosition;
    }

    public void setPdbPosition(int pdbPosition) {
        this.pdbPosition = pdbPosition;
    }

    public String getPdbAminoAcid() {
        return pdbAminoAcid;
    }

    public void setPdbAminoAcid(String pdbAminoAcid) {
        this.pdbAminoAcid = pdbAminoAcid;
    }

    
    

}
