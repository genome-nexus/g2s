package org.cbioportal.pdb_annotation.web.models;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class Inputsequence {
    
    private String id;
    
    @NotNull
    //@Size(min=1, max=10000)
    //@Pattern(regexp="(^[>].*[\\n|\\r]+[RrHhKkDdEeSsTtNnQqCcUuGgPpAaVvIiLlMmFfYyWw]+[\\n|\\r]*$)|(^[RrHhKkDdEeSsTtNnQqCcUuGgPpAaVvIiLlMmFfYyWw]+[\\n|\\r]*$)",message="Fasta format error, please check and resubmit!")
    @Pattern(regexp="(^[>].*[\\n|\\r]+[RrHhKkDdEeSsTtNnQqCcUuGgPpAaVvIiLlMmFfYyWw\\n\\r]+$)|(^[RrHhKkDdEeSsTtNnQqCcUuGgPpAaVvIiLlMmFfYyWw\\n\\r]+$)",message="incorrect")
    private String sequence;
    
    //@Digits(fraction = 0, integer = 5)
    private String residueNum;
       
    private String residueName;
    
    //Parameters are list here:
    //default 1e-10
    private String evalue;
    
    //2,3,6
    //default 3
    private int word_size;
    
    @Digits(fraction = 0, integer = 5)
    @Min(1)
    //default 11
    private int gapopen;
    
    @Digits(fraction = 0, integer = 5)
    @Min(1)
    //default 1
    private int gapextend;
    
    //default BLOSUM62
    private String matrix;
    
    //0,1,2,3 default 2
    private int comp_based_stats;
    
    //default 11
    //Neighboring words threshold
    @Min(0)
    private int threshold;
    
    //default 40
    //Window for multiple hits
    @Min(0)
    private int window_size;
    
    /**
     * 
     * Getter and setter
     * 
     */
    
    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public int getWindow_size() {
        return window_size;
    }

    public void setWindow_size(int window_size) {
        this.window_size = window_size;
    }

    public String getId() {
        return id;
    }
    
    public String getEvalue() {
        return evalue;
    }
    public void setEvalue(String evalue) {
        this.evalue = evalue;
    }
    public int getWord_size() {
        return word_size;
    }
    public void setWord_size(int word_size) {
        this.word_size = word_size;
    }
    public int getGapopen() {
        return gapopen;
    }
    public void setGapopen(int gapopen) {
        this.gapopen = gapopen;
    }
    public int getGapextend() {
        return gapextend;
    }
    public void setGapextend(int gapextend) {
        this.gapextend = gapextend;
    }
    public String getMatrix() {
        return matrix;
    }
    public void setMatrix(String matrix) {
        this.matrix = matrix;
    }
    public int getComp_based_stats() {
        return comp_based_stats;
    }
    public void setComp_based_stats(int comp_based_stats) {
        this.comp_based_stats = comp_based_stats;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    public String getSequence() {
        return sequence;
    }
    public void setSequence(String sequence) {
        this.sequence = sequence;
    }
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
