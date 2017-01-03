package org.cbioportal.pdb_annotation.web.models;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class Inputsequence {

    private String id;

    @NotNull
    // @Size(min=1, max=10000)
    // @Pattern(regexp="(^[>].*[\\n|\\r]+[RrHhKkDdEeSsTtNnQqCcUuGgPpAaVvIiLlMmFfYyWw\\n\\r]+$)|(^[RrHhKkDdEeSsTtNnQqCcUuGgPpAaVvIiLlMmFfYyWw\\n\\r]+$)",message="Fasta
    // format error, please check and resubmit!")
    @Pattern(regexp = "(^[>].*[\\n|\\r]+[RrHhKkDdEeSsTtNnQqCcUuGgPpAaVvIiLlMmFfYyWw\\s\\n\\r\\t]+$)|(^[RrHhKkDdEeSsTtNnQqCcUuGgPpAaVvIiLlMmFfYyWw\\s\\n\\t\\r]+$)", message = "Fasta Format Error, please Check and Resubmit!")
    private String sequence;

    private String residueNum;

    private String residueName;

    // Parameters are list here:
    // default 1e-10
    @Pattern(regexp = "(^((\\d+.?\\d*)[Ee]{1}(-?\\d+))$)|(^(\\d|[1-9]\\d+)(\\.\\d+)?$)", message = "Parameter Error in Evalue: Non-negative Scientific Notation or Non-negative digits")
    private String evalue;

    // 2,3,6
    // default 3
    @Pattern(regexp = "^[1-9]|([1-9]\\d+)", message = "Parameter Error in Word_Size: Should be >=1 Integer")
    private String word_size;

    // default 11
    @Pattern(regexp="^\\d+$",message="Parameter Error in Gapopen: Should be Non-negative Integer")
    private String gapopen;

    // default 1
    @Pattern(regexp="^\\d+$",message="Parameter Error in Gapextend: Should be Non-negative Integer")
    private String gapextend;

    // default BLOSUM62
    private String matrix;

    // 0,1,2,3 default 2
    @Pattern(regexp="^(3)|(2)|(1)|(0)$",message="Parameter Error in Comp_based_stats: Should be >=1 Integer")
    private String comp_based_stats;

    // default 11
    // Neighboring words threshold
    @Pattern(regexp="^[1-9]|([1-9]\\d+)",message="Parameter Error in Threshold: Should be >=1 Integer")
    private String threshold;

    // default 40
    // Window for multiple hits
    @Pattern(regexp="^[1-9]|([1-9]\\d+)",message="Parameter Error in Window_size: Should be >=1 Integer")
    private String window_size;

    // Time
    private String timenow;

    /**
     * 
     * Getter and setter
     * 
     */

    public String getTimenow() {
        return timenow;
    }

    public void setTimenow(String timenow) {
        this.timenow = timenow;
    }

    public String getThreshold() {
        return threshold;
    }

    public void setThreshold(String threshold) {
        this.threshold = threshold;
    }

    public String getWindow_size() {
        return window_size;
    }

    public void setWindow_size(String window_size) {
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

    public String getWord_size() {
        return word_size;
    }

    public void setWord_size(String word_size) {
        this.word_size = word_size;
    }

    public String getGapopen() {
        return gapopen;
    }

    public void setGapopen(String gapopen) {
        this.gapopen = gapopen;
    }

    public String getGapextend() {
        return gapextend;
    }

    public void setGapextend(String gapextend) {
        this.gapextend = gapextend;
    }

    public String getMatrix() {
        return matrix;
    }

    public void setMatrix(String matrix) {
        this.matrix = matrix;
    }

    public String getComp_based_stats() {
        return comp_based_stats;
    }

    public void setComp_based_stats(String comp_based_stats) {
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
