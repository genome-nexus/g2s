package org.cbioportal.pdb_annotation.web.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 
 * @author Juexin Wang
 *
 */
@Entity
@Table(name = "uniprot_entry")
public class Uniprot {
    @Id
    @Column(name = "UNIPROT_ID_ISO")
    private String uniprotAccessionIso;

    @Column(name = "UNIPROT_ID")
    private String uniprotAccession;

    // Careful: It may confuse, but Name is really ID, as UniProt ID
    // (`EGFR_HUMAN`) as uniProt Acc (`P00533`)
    @Column(name = "NAME")
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

    public Uniprot(String uniprotAccessionIso) {
        this.uniprotAccessionIso = uniprotAccessionIso;
    }

    // ------------------------
    // Methods
    // ------------------------
    public String getUniprotAccessionIso() {
        return uniprotAccessionIso;
    }

    public void setUniprotAccessionIso(String uniprotAccessionIso) {
        this.uniprotAccessionIso = uniprotAccessionIso;
    }

    public String getUniprotAccession() {
        return uniprotAccession;
    }

    public void setUniprotAccession(String uniprotAccession) {
        this.uniprotAccession = uniprotAccession;
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

    public String getUniprotId() {
        return uniprotId;
    }

    public void setUniprotId(String uniprotId) {
        this.uniprotId = uniprotId;
    }

}
