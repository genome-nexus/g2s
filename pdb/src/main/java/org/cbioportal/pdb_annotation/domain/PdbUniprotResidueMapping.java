package org.cbioportal.pdb_annotation.domain;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * @author Selcuk Onur Sumer
 */
@Entity
public class PdbUniprotResidueMapping
{
	@Column(name="alignment_id")
	private long alignmentId;

	@Column(name="pdb_position")
	private long pdbPosition;

	@Column(name="pdb_insertion_code")
	private String pdbInsertionCode;

	@Column(name="uniprot_position")
	private long uniprotPosition;

	@Column(name="match")
	private String match;

	protected PdbUniprotResidueMapping() {}

	public long getAlignmentId()
	{
		return alignmentId;
	}

	public void setAlignmentId(long alignmentId)
	{
		this.alignmentId = alignmentId;
	}

	public long getPdbPosition()
	{
		return pdbPosition;
	}

	public void setPdbPosition(long pdbPosition)
	{
		this.pdbPosition = pdbPosition;
	}

	public String getPdbInsertionCode()
	{
		return pdbInsertionCode;
	}

	public void setPdbInsertionCode(String pdbInsertionCode)
	{
		this.pdbInsertionCode = pdbInsertionCode;
	}

	public long getUniprotPosition()
	{
		return uniprotPosition;
	}

	public void setUniprotPosition(long uniprotPosition)
	{
		this.uniprotPosition = uniprotPosition;
	}

	public String getMatch()
	{
		return match;
	}

	public void setMatch(String match)
	{
		this.match = match;
	}
}
