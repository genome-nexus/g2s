package org.cbioportal.pdb_annotation.web.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Basic Return Objects: Alignments
 * 
 * @author Juexin Wang
 *
 */

@Entity
@Table(name = "pdb_seq_alignment")
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

    @Column(name = "SEG_START")
    private String segStart;

    @Column(name = "SEQ_ID")
    private String seqId;

    @Column(name = "PDB_FROM")
    private int pdbFrom;

    @Column(name = "PDB_TO")
    private int pdbTo;

    @Column(name = "SEQ_FROM")
    private int seqFrom;

    @Column(name = "SEQ_TO")
    private int seqTo;

    @Column(name = "EVALUE")
    private String evalue;

    @Column(name = "BITSCORE")
    private float bitscore;

    @Column(name = "IDENTITY")
    private float identity;

    @Column(name = "IDENTP")
    private float identityPositive;

    @Column(name = "SEQ_ALIGN")
    private String seqAlign;

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

    public String getSegStart() {
        return segStart;
    }

    public void setSegStart(String segStart) {
        this.segStart = segStart;
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

    public String getSeqId() {
        return seqId;
    }

    public void setSeqId(String seqId) {
        this.seqId = seqId;
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

    public int getSeqFrom() {
        return seqFrom;
    }

    public void setSeqFrom(int seqFrom) {
        this.seqFrom = seqFrom;
    }

    public int getSeqTo() {
        return seqTo;
    }

    public void setSeqTo(int seqTo) {
        this.seqTo = seqTo;
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

    public float getIdentityPositive() {
        return identityPositive;
    }

    public void setIdentityPositive(float identityPositive) {
        this.identityPositive = identityPositive;
    }

    public String getSeqAlign() {
        return seqAlign;
    }

    public void setSeqAlign(String seqAlign) {
        this.seqAlign = seqAlign;
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
