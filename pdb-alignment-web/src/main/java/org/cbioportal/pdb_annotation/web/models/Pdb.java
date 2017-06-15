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
@Table(name = "pdb_entry")
public class Pdb {
    @Id
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

    // ------------------------
    // Constructors
    // ------------------------

    public Pdb() {
    }

    public Pdb(String pdbno) {
        this.pdbNo = pdbno;
    }

    // ------------------------
    // Methods
    // ------------------------

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

    public String getSegStart() {
        return segStart;
    }

    public void setSegStart(String segStart) {
        this.segStart = segStart;
    }

}
