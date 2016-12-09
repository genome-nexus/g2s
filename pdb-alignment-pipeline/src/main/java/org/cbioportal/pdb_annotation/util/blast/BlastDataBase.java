package org.cbioportal.pdb_annotation.util.blast;

public class BlastDataBase {
    public String name;
    public String dbName;
    public String resultfileName;
    public String dbphr;
    public String dbpin;
    public String dbpsq;

    public BlastDataBase(String subject) {
        this.name = subject.replaceAll("\\..*","");         // Remove file extension
        this.name = this.name.replaceAll(".fasta","");
        this.dbName = this.name + ".db";
        this.resultfileName = this.name + ".xml";
        this.dbphr = this.dbName + ".phr";
        this.dbpin = this.dbName + ".pin";
        this.dbpsq = this.dbName + ".psq";
    }
}
