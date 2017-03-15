package org.cbioportal.pdb_annotation.web.models;

public class BlastStatistics {
    
  //BLAST statistics
    // Blast Version and Results;
    private String blast_version;

    private String blast_reference;

    // Results related statistics;
    private String blast_dblen;

    private String blast_dbnum;

    private String blast_effspace;

    private String blast_entropy;

    private String blast_hsplen;

    private String blast_kappa;

    private String blast_lambda;

    public String getBlast_version() {
        return blast_version;
    }

    public void setBlast_version(String blast_version) {
        this.blast_version = blast_version;
    }

    public String getBlast_reference() {
        return blast_reference;
    }

    public void setBlast_reference(String blast_reference) {
        this.blast_reference = blast_reference;
    }

    public String getBlast_dblen() {
        return blast_dblen;
    }

    public void setBlast_dblen(String blast_dblen) {
        this.blast_dblen = blast_dblen;
    }

    public String getBlast_dbnum() {
        return blast_dbnum;
    }

    public void setBlast_dbnum(String blast_dbnum) {
        this.blast_dbnum = blast_dbnum;
    }

    public String getBlast_effspace() {
        return blast_effspace;
    }

    public void setBlast_effspace(String blast_effspace) {
        this.blast_effspace = blast_effspace;
    }

    public String getBlast_entropy() {
        return blast_entropy;
    }

    public void setBlast_entropy(String blast_entropy) {
        this.blast_entropy = blast_entropy;
    }

    public String getBlast_hsplen() {
        return blast_hsplen;
    }

    public void setBlast_hsplen(String blast_hsplen) {
        this.blast_hsplen = blast_hsplen;
    }

    public String getBlast_kappa() {
        return blast_kappa;
    }

    public void setBlast_kappa(String blast_kappa) {
        this.blast_kappa = blast_kappa;
    }

    public String getBlast_lambda() {
        return blast_lambda;
    }

    public void setBlast_lambda(String blast_lambda) {
        this.blast_lambda = blast_lambda;
    }
    
    

}
