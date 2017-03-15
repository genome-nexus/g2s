package org.cbioportal.pdb_annotation.web.models;

public class CompleteResidue extends Residue{
    
    
    private String identityPercentage;
    private String positivePercentage;
    private String gapPercentage;
    private int gap;
    private int length;

    public String getIdentityPercentage() {
        return identityPercentage;
    }
    public void setIdentityPercentage(String identityPercentage) {
        this.identityPercentage = identityPercentage;
    }
    public String getPositivePercentage() {
        return positivePercentage;
    }
    public void setPositivePercentage(String positivePercentage) {
        this.positivePercentage = positivePercentage;
    }
    public String getGapPercentage() {
        return gapPercentage;
    }
    public void setGapPercentage(String gapPercentage) {
        this.gapPercentage = gapPercentage;
    }
    public int getGap() {
        return gap;
    }
    public void setGap(int gap) {
        this.gap = gap;
    }
    public int getLength() {
        return length;
    }
    public void setLength(int length) {
        this.length = length;
    }
    
    

}
