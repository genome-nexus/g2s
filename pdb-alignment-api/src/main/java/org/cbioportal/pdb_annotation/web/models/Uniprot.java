package org.cbioportal.pdb_annotation.web.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "uniprot_entry")
public class Uniprot {
    @Id
    @Column(name = "UNIPROT_ID_ISO")
    private String uniprotIdIso;
    
    @Column(name = "UNIPROT_ID")
    private String uniprotId;
    
    @Column(name = "ISOFORM")
    private String isoform;
    
    @Column(name = "SEQ_ID")
    private String seqId;
    
    
    // ------------------------
    // Constructors
    // ------------------------

    public Uniprot() {
    }

    public Uniprot(String uniprotIdIso) {
        this.uniprotIdIso = uniprotIdIso;
    }

    
    // ------------------------
    // Methods
    // ------------------------
    public String getUniprotIdIso() {
        return uniprotIdIso;
    }

    public void setUniprotIdIso(String uniprotIdIso) {
        this.uniprotIdIso = uniprotIdIso;
    }

    public String getUniprotId() {
        return uniprotId;
    }

    public void setUniprotId(String uniprotId) {
        this.uniprotId = uniprotId;
    }

    public String getIsoform() {
        return isoform;
    }

    public void setIsoform(String isoform) {
        this.isoform = isoform;
    }

    public String getSeqId() {
        return seqId;
    }

    public void setSeqId(String seqId) {
        this.seqId = seqId;
    }    

}
