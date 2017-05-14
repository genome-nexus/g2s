package org.cbioportal.pdb_annotation.web.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "update_record")
public class Statistics {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private int id;

    @Column(name = "UPDATE_DATE")
    private String updateDate;

    @Column(name = "SEG_NUM")
    private int segNum;

    @Column(name = "PDB_NUM")
    private int pdbNum;

    @Column(name = "ALIGNMENT_NUM")
    private int alignmentNum;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(String updateDate) {
        this.updateDate = updateDate;
    }

    public int getSegNum() {
        return segNum;
    }

    public void setSegNum(int segNum) {
        this.segNum = segNum;
    }

    public int getPdbNum() {
        return pdbNum;
    }

    public void setPdbNum(int pdbNum) {
        this.pdbNum = pdbNum;
    }

    public int getAlignmentNum() {
        return alignmentNum;
    }

    public void setAlignmentNum(int alignmentNum) {
        this.alignmentNum = alignmentNum;
    }

}
