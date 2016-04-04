package org.cbioportal.pdb_annotation.domain;


import javax.persistence.*;

/**
 * @author Selcuk Onur Sumer
 */
@Entity
@Table(name="pdb_uniprot_alignment")
public class PdbUniprotAlignment
{
	@Id
	@Column(name="alignment_id")
	private long alignmentId;

	@Column(name="pdb_id")
	private String pdbId;

	@Column(name="chain")
	private String chain;

	@Column(name="uniprot_id")
	private String uniprotId;

	@Column(name="pdb_from")
	private String pdbFrom;

	@Column(name="pdb_to")
	private String pdbTo;

	@Column(name="uniprot_from")
	private long uniprotFrom;

	@Column(name="uniprot_to")
	private long uniprotTo;

	@Column(name="evalue")
	private double eValue;

	@Column(name="identity")
	private double identity;

	@Column(name="identp")
	private double identityP;

	@Column(name="uniprot_align")
	private String uniprotAlign;

	@Column(name="pdb_align")
	private String pdbAlign;

	@Column(name="midline_align")
	private String midlineAlign;

	protected PdbUniprotAlignment() {}

	public long getAlignmentId()
	{
		return alignmentId;
	}

	public void setAlignmentId(long alignmentId)
	{
		this.alignmentId = alignmentId;
	}

	public String getPdbId()
	{
		return pdbId;
	}

	public void setPdbId(String pdbId)
	{
		this.pdbId = pdbId;
	}

	public String getChain()
	{
		return chain;
	}

	public void setChain(String chain)
	{
		this.chain = chain;
	}

	public String getUniprotId()
	{
		return uniprotId;
	}

	public void setUniprotId(String uniprotId)
	{
		this.uniprotId = uniprotId;
	}

	public String getPdbFrom()
	{
		return pdbFrom;
	}

	public void setPdbFrom(String pdbFrom)
	{
		this.pdbFrom = pdbFrom;
	}

	public String getPdbTo()
	{
		return pdbTo;
	}

	public void setPdbTo(String pdbTo)
	{
		this.pdbTo = pdbTo;
	}

	public long getUniprotFrom()
	{
		return uniprotFrom;
	}

	public void setUniprotFrom(long uniprotFrom)
	{
		this.uniprotFrom = uniprotFrom;
	}

	public long getUniprotTo()
	{
		return uniprotTo;
	}

	public void setUniprotTo(long uniprotTo)
	{
		this.uniprotTo = uniprotTo;
	}

	public double geteValue()
	{
		return eValue;
	}

	public void seteValue(double eValue)
	{
		this.eValue = eValue;
	}

	public double getIdentity()
	{
		return identity;
	}

	public void setIdentity(double identity)
	{
		this.identity = identity;
	}

	public double getIdentityP()
	{
		return identityP;
	}

	public void setIdentityP(double identityP)
	{
		this.identityP = identityP;
	}

	public String getUniprotAlign()
	{
		return uniprotAlign;
	}

	public void setUniprotAlign(String uniprotAlign)
	{
		this.uniprotAlign = uniprotAlign;
	}

	public String getPdbAlign()
	{
		return pdbAlign;
	}

	public void setPdbAlign(String pdbAlign)
	{
		this.pdbAlign = pdbAlign;
	}

	public String getMidlineAlign()
	{
		return midlineAlign;
	}

	public void setMidlineAlign(String midlineAlign)
	{
		this.midlineAlign = midlineAlign;
	}
}
