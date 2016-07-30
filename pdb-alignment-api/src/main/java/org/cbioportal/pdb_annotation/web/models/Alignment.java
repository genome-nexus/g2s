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

    public int getAlignmentid() {
        return alignmentId;
    }

    public void setAlignmentid(int alignmentid) {
        this.alignmentId = alignmentid;
    }

    public String getPdbno() {
        return pdbNo;
    }

    public void setPdbno(String pdbno) {
        this.pdbNo = pdbno;
    }

    public String getPdbid() {
        return pdbId;
    }

    public void setPdbid(String pdbid) {
        this.pdbId = pdbid;
    }

    public String getChain() {
        return chain;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }

    public String getEnsemblid() {
        return ensemblId;
    }

    public void setEnsemblid(String ensemblid) {
        this.ensemblId = ensemblid;
    }

    public int getPdbfrom() {
        return pdbFrom;
    }

    public void setPdbfrom(int pdbfrom) {
        this.pdbFrom = pdbfrom;
    }

    public int getPdbto() {
        return pdbTo;
    }

    public void setPdbto(int pdbto) {
        this.pdbTo = pdbto;
    }

    public int getEnsemblfrom() {
        return ensemblFrom;
    }

    public void setEnsemblfrom(int ensemblfrom) {
        this.ensemblFrom = ensemblfrom;
    }

    public int getEnsemblto() {
        return ensemblTo;
    }

    public void setEnsemblto(int ensemblto) {
        this.ensemblTo = ensemblto;
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
}
