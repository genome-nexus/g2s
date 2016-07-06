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
	private String pdbno;
	
	@Column(name = "PDB_ID")
	private String pdbid;
	 
	@Column(name = "CHAIN")
	private String chain;
	
	@Column(name = "DBREF")
	private String dbref;
	
	/**
	 * Construction Function
	 */
	public Pdb() { }

	public Pdb(String pdbno) { 
	    this.pdbno = pdbno;
	}

	
	//Get and Set
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

	public String getDbref() {
		return dbref;
	}

	public void setDbref(String dbref) {
		this.dbref = dbref;
	}
	

}
