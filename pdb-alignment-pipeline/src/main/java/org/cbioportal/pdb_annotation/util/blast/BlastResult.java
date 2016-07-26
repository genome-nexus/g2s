package org.cbioportal.pdb_annotation.util.blast;

public class BlastResult{

	public Integer oligoID;
	public String qseqid;
	public String sseqid;
	public Double ident;
	public Double identp;
	public Double  evalue;
	public Double bitscore;
	public Integer qStart;
	public Integer qEnd;
	public Integer sStart;
	public Integer sEnd;	
	public String ensembl_align;
	public String pdb_align;
	public String midline_align;
	

	public BlastResult(Integer id) { 
		this.oligoID = id;
		this.qseqid = "";
		this.sseqid = "";
		this.ident = -1.0;
		this.identp = -1.0;
		this.bitscore = -1.0;
		this.evalue = -1.0;	
		this.qStart = -1;
		this.qEnd= -1;
		this.sStart = -1;
		this.sEnd = -1;		
		this.ensembl_align="";
		this.pdb_align="";
		this.midline_align="";
	}
	
	



	public Integer getOligoID() {
		return oligoID;
	}





	public void setOligoID(Integer oligoID) {
		this.oligoID = oligoID;
	}





	public String getQseqid() {
		return qseqid;
	}





	public void setQseqid(String qseqid) {
		this.qseqid = qseqid;
	}





	public String getSseqid() {
		return sseqid;
	}





	public void setSseqid(String sseqid) {
		this.sseqid = sseqid;
	}





	public Double getIdentp() {
		return identp;
	}





	public void setIdentp(Double identp) {
		this.identp = identp;
	}





	public Double getIdent() {
		return ident;
	}





	public void setIdent(Double ident) {
		this.ident = ident;
	}





	public Integer getqStart() {
		return qStart;
	}





	public void setqStart(Integer qStart) {
		this.qStart = qStart;
	}





	public Integer getqEnd() {
		return qEnd;
	}





	public void setqEnd(Integer qEnd) {
		this.qEnd = qEnd;
	}





	public Integer getsStart() {
		return sStart;
	}





	public void setsStart(Integer sStart) {
		this.sStart = sStart;
	}





	public Integer getsEnd() {
		return sEnd;
	}





	public void setsEnd(Integer sEnd) {
		this.sEnd = sEnd;
	}





	public Double getEvalue() {
		return evalue;
	}





	public void setEvalue(Double evalue) {
		this.evalue = evalue;
	}





	public Double getBitscore() {
		return bitscore;
	}





	public void setBitscore(Double bitscore) {
		this.bitscore = bitscore;
	}





	public String getEnsembl_align() {
		return ensembl_align;
	}





	public void setEnsembl_align(String ensembl_align) {
		this.ensembl_align = ensembl_align;
	}





	public String getPdb_align() {
		return pdb_align;
	}





	public void setPdb_align(String pdb_align) {
		this.pdb_align = pdb_align;
	}





	public String getMidline_align() {
		return midline_align;
	}





	public void setMidline_align(String midline_align) {
		this.midline_align = midline_align;
	}





	@Override
	public String toString(){
		return oligoID.toString()+"\t"+qseqid+"\t"+sseqid+"\t"+ident+"\t"+identp+evalue.toString()+"\t"+
				bitscore.toString()+"\t"+qStart.toString()+"\t"+qEnd.toString()+"\t"+sStart.toString()+"\t"+
				sEnd.toString()+"\t"+ensembl_align.toString()+"\t"+pdb_align.toString()+"\t"+midline_align.toString();
	}
	
}
