package org.cbioportal.pdb_annotation.domain;

/**
 * @author Selcuk Onur Sumer
 */
public class AlignmentSummary
{
    private String uniprotId;
    private Integer alignmentCount;

    public AlignmentSummary()
    {
        this(null);
    }

    public AlignmentSummary(String uniprotId)
    {
        this.uniprotId = uniprotId;
    }

    public String getUniprotId()
    {
        return uniprotId;
    }

    public void setUniprotId(String uniprotId)
    {
        this.uniprotId = uniprotId;
    }

    public Integer getAlignmentCount()
    {
        return alignmentCount;
    }

    public void setAlignmentCount(Integer alignmentCount)
    {
        this.alignmentCount = alignmentCount;
    }
}
