package org.cbioportal.pdb_annotation.web.models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ensembl_entry")
public class Ensembl {
	// ------------------------
	// PRIVATE FIELDS
	// ------------------------
	@Id 
	@Column(name = "ENSEMBL_ID") 
	private String ensemblid;
	
	@Column(name = "ENSEMBL_GENE") 
	private String ensemblgene;
	
	@Column(name = "ENSEMBL_TRANSCRIPT") 
	private String ensembltranscript;
	
	/**
	 * Construction Function
	 */
	public Ensembl() { }

	public Ensembl(String ensemblid) { 
	    this.ensemblid = ensemblid;
	}

	
	// Get and set
	public String getEnsemblid() {
		return ensemblid;
	}

	public void setEnsemblid(String ensemblid) {
		this.ensemblid = ensemblid;
	}

	public String getEnsemblgene() {
		return ensemblgene;
	}

	public void setEnsemblgene(String ensemblgene) {
		this.ensemblgene = ensemblgene;
	}

	public String getEnsembltranscript() {
		return ensembltranscript;
	}

	public void setEnsembltranscript(String ensembltranscript) {
		this.ensembltranscript = ensembltranscript;
	}
	
	
	
	

}
