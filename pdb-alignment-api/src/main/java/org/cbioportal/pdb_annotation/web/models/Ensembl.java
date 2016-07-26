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
@Table(name = "ensembl_entry")
public class Ensembl {
	// ------------------------
	// PRIVATE FIELDS
	// ------------------------
	@Id 
	@Column(name = "ENSEMBL_ID") 
	private String ensemblId;
	
	@Column(name = "ENSEMBL_GENE") 
	private String ensemblGene;
	
	@Column(name = "ENSEMBL_TRANSCRIPT") 
	private String ensemblTranscript;
	
	/**
	 * Construction Function
	 */
	public Ensembl() { }

	public Ensembl(String ensemblid) { 
	    this.ensemblId = ensemblid;
	}

	
	// Get and set
	public String getEnsemblid() {
		return ensemblId;
	}

	public void setEnsemblid(String ensemblid) {
		this.ensemblId = ensemblid;
	}

	public String getEnsemblgene() {
		return ensemblGene;
	}

	public void setEnsemblgene(String ensemblgene) {
		this.ensemblGene = ensemblgene;
	}

	public String getEnsembltranscript() {
		return ensemblTranscript;
	}

	public void setEnsembltranscript(String ensembltranscript) {
		this.ensemblTranscript = ensembltranscript;
	}	

}
