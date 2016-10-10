package org.cbioportal.pdb_annotation.web.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Juexin Wang
 *
 */

@Entity
@Table(name = "pdb_ensembl_alignment")
public class Alignment {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ALIGNMENT_ID")
    private int alignmentId;

    @Column(name = "PDB_NO")
    private String pdbNo;

    @Column(name = "PDB_ID")
    private String pdbId;

    @Column(name = "CHAIN")
    private String chain;
    
    @Column(name = "PDB_SEG")
    private String pdbSeg;

    @Column(name = "ENSEMBL_ID")
    private String ensemblId;

    @Column(name = "PDB_FROM")
    private int pdbFrom;

    @Column(name = "PDB_TO")
    private int pdbTo;

    @Column(name = "ENSEMBL_FROM")
    private int ensemblFrom;

    @Column(name = "ENSEMBL_TO")
    private int ensemblTo;

    @Column(name = "EVALUE")
    private String evalue;

    @Column(name = "BITSCORE")
    private float bitscore;

    @Column(name = "IDENTITY")
    private float identity;

    @Column(name = "IDENTP")
    private float identp;

    @Column(name = "ENSEMBL_ALIGN")
    private String ensemblAlign;

    @Column(name = "PDB_ALIGN")
    private String pdbAlign;

    @Column(name = "MIDLINE_ALIGN")
    private String midlineAlign;
    
    @Column(name = "UPDATE_DATE")
    private String updateDate;

    // ------------------------
    // Constructors
    // ------------------------

    public Alignment() {
    }

    public Alignment(int alignmentid) {
        this.alignmentId = alignmentid;
    }

    // ------------------------
    // Methods
    // ------------------------

    public int getAlignmentId() {
        return alignmentId;
    }

    public void setAlignmentId(int alignmentId) {
        this.alignmentId = alignmentId;
    }

    public String getPdbNo() {
        return pdbNo;
    }

    public void setPdbNo(String pdbNo) {
        this.pdbNo = pdbNo;
    }

    public String getPdbId() {
        return pdbId;
    }

    public void setPdbId(String pdbId) {
        this.pdbId = pdbId;
    }

    public String getChain() {
        return chain;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }

    public String getPdbSeg() {
        return pdbSeg;
    }

    public void setPdbSeg(String pdbSeg) {
        this.pdbSeg = pdbSeg;
    }

    public String getEnsemblId() {
        return ensemblId;
    }

    public void setEnsemblId(String ensemblId) {
        this.ensemblId = ensemblId;
    }

    public int getPdbFrom() {
        return pdbFrom;
    }

    public void setPdbFrom(int pdbFrom) {
        this.pdbFrom = pdbFrom;
    }

    public int getPdbTo() {
        return pdbTo;
    }

    public void setPdbTo(int pdbTo) {
        this.pdbTo = pdbTo;
    }

    public int getEnsemblFrom() {
        return ensemblFrom;
    }

    public void setEnsemblFrom(int ensemblFrom) {
        this.ensemblFrom = ensemblFrom;
    }

    public int getEnsemblTo() {
        return ensemblTo;
    }

    public void setEnsemblTo(int ensemblTo) {
        this.ensemblTo = ensemblTo;
    }

    public String getEvalue() {
        return evalue;
    }

    public void setEvalue(String evalue) {
        this.evalue = evalue;
    }

    public float getBitscore() {
        return bitscore;
    }

    public void setBitscore(float bitscore) {
        this.bitscore = bitscore;
    }

    public float getIdentity() {
        return identity;
    }

    public void setIdentity(float identity) {
        this.identity = identity;
    }

    public float getIdentp() {
        return identp;
    }

    public void setIdentp(float identp) {
        this.identp = identp;
    }

    public String getEnsemblAlign() {
        return ensemblAlign;
    }

    public void setEnsemblAlign(String ensemblAlign) {
        this.ensemblAlign = ensemblAlign;
    }

    public String getPdbAlign() {
        return pdbAlign;
    }

    public void setPdbAlign(String pdbAlign) {
        this.pdbAlign = pdbAlign;
    }

    public String getMidlineAlign() {
        return midlineAlign;
    }

    public void setMidlineAlign(String midlineAlign) {
        this.midlineAlign = midlineAlign;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

}
