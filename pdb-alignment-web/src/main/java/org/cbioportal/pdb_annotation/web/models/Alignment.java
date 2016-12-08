package org.cbioportal.pdb_annotation.web.models;

public class Alignment {
    
    private int alignmentId;

    private String pdbNo;

    private String pdbId;

    private String chain;
    
    private String pdbSeg;

    private String seqId;

    private int pdbFrom;

    private int pdbTo;

    private int seqFrom;

    private int seqTo;

    private double evalue;

    private double bitscore;

    private double identity;

    private double identp;

    private String seqAlign;

    private String pdbAlign;

    private String midlineAlign;

    // ------------------------
    // Constructors
    // ------------------------

    public Alignment() {
    }

    public Alignment(int alignmentid) {
        this.alignmentId = alignmentid;
    }
    
    //Set and get

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

    public double getEvalue() {
        return evalue;
    }

    public void setEvalue(double evalue) {
        this.evalue = evalue;
    }

    public double getBitscore() {
        return bitscore;
    }

    public void setBitscore(double bitscore) {
        this.bitscore = bitscore;
    }

    public double getIdentity() {
        return identity;
    }

    public void setIdentity(double identity) {
        this.identity = identity;
    }

    public double getIdentp() {
        return identp;
    }

    public void setIdentp(double identp) {
        this.identp = identp;
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

}
