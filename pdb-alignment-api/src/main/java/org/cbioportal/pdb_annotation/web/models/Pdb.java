package org.cbioportal.pdb_annotation.web.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "pdb_entry")
public class Pdb {
	// ------------------------
	// PRIVATE FIELDS
	// ------------------------
	@Id 
	@Column(name = "PDB_NO")
	private String pdbNo;
	
	@Column(name = "PDB_ID")
	private String pdbId;
	 
	@Column(name = "CHAIN")
	private String chain;
	
	@Column(name = "DBREF")
	private String dbref;
	
	/**
	 * Construction Function
	 */
	public Pdb() { }

	public Pdb(String pdbno) { 
	    this.pdbNo = pdbno;
	}

	
	//Get and Set
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

	public String getDbref() {
		return dbref;
	}

	public void setDbref(String dbref) {
		this.dbref = dbref;
	}
	

}
