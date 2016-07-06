package org.cbioportal.pdb_annotation.web.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

@Entity
@Table(name = "pdb_ensembl_alignment")
public class Alignment {
	  // ------------------------
	  // PRIVATE FIELDS
	  // ------------------------
	 @Id  
	 @GeneratedValue(strategy = GenerationType.AUTO)
	 @Column(name = "ALIGNMENT_ID") 
	 private int alignmentid;	
	 
	 @Column(name = "PDB_NO")
	  private String pdbno;
	 
	 @Column(name = "PDB_ID")
	  private String pdbid;
	 
	 @Column(name = "CHAIN")
	  private String chain;
	  
	  @Column(name = "ENSEMBL_ID")
	  private String ensemblid;
	  
	  @Column(name = "PDB_FROM")
	  private int pdbfrom;
	  
	  @Column(name = "PDB_TO")
	  private int pdbto;
	  
	  @Column(name = "ENSEMBL_FROM")
	  private int ensemblfrom;
	  
	  @Column(name = "ENSEMBL_TO")
	  private int ensemblto;
	  
	  @Column(name = "EVALUE")
	  private String evalue;
	  
	  @Column(name = "BITSCORE")
	  private float bitscore;
	  
	  @Column(name = "IDENTITY")
	  private float identity;
	  
	  @Column(name = "IDENTP")
	  private float identp;
	  
	  /*
	  @Column(name = "ENSEMBL_ALIGN")
	  private String ensemblalign;
	  
	  @Column(name = "PDB_ALIGN")
	  private String pdbalign;
	  
	  @Column(name = "MIDLINE_ALIGN")
	  private String midlinealign;
	  */
	  

	  // ------------------------
	  // PUBLIC METHODS
	  // ------------------------
	  

	  public int getAlignmentid() {
		return alignmentid;
	}

	public void setAlignmentid(int alignmentid) {
		this.alignmentid = alignmentid;
	}

	public String getPdbno() {
		return pdbno;
	}

	public void setPdbno(String pdbno) {
		this.pdbno = pdbno;
	}

	public String getPdbid() {
		return pdbid;
	}

	public void setPdbid(String pdbid) {
		this.pdbid = pdbid;
	}

	public String getChain() {
		return chain;
	}

	public void setChain(String chain) {
		this.chain = chain;
	}

	public String getEnsemblid() {
		return ensemblid;
	}

	public void setEnsemblid(String ensemblid) {
		this.ensemblid = ensemblid;
	}

	public int getPdbfrom() {
		return pdbfrom;
	}

	public void setPdbfrom(int pdbfrom) {
		this.pdbfrom = pdbfrom;
	}

	public int getPdbto() {
		return pdbto;
	}

	public void setPdbto(int pdbto) {
		this.pdbto = pdbto;
	}

	public int getEnsemblfrom() {
		return ensemblfrom;
	}

	public void setEnsemblfrom(int ensemblfrom) {
		this.ensemblfrom = ensemblfrom;
	}

	public int getEnsemblto() {
		return ensemblto;
	}

	public void setEnsemblto(int ensemblto) {
		this.ensemblto = ensemblto;
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

	/*
	public String getEnsemblalign() {
		return ensemblalign;
	}

	public void setEnsemblalign(String ensemblalign) {
		this.ensemblalign = ensemblalign;
	}

	public String getPdbalign() {
		return pdbalign;
	}

	public void setPdbalgin(String pdbalign) {
		this.pdbalign = pdbalign;
	}

	public String getMidlinealign() {
		return midlinealign;
	}

	public void setMidlinealign(String midlinealign) {
		this.midlinealign = midlinealign;
	}
	*/
	
	/**
	 * Construction Function
	 */
	public Alignment() { }

	public Alignment(int alignmentid) { 
	    this.alignmentid = alignmentid;
	  }
	  
}
