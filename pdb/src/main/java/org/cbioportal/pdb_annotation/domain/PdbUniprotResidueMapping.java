package org.cbioportal.pdb_annotation.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @author Selcuk Onur Sumer
 */
@Entity
@Table(name="pdb_uniprot_residue_mapping")
@IdClass(PdbUniprotResidueMappingPK.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PdbUniprotResidueMapping implements Serializable
{
    @Id
    @Column(name = "alignment_id")
    private long alignmentId;

//    @ManyToOne
//    @Id
//    @JoinColumn(name="alignment_id", referencedColumnName="alignment_id")
//    private PdbUniprotAlignment alignment;

    @Column(name = "pdb_position")
    private Long pdbPosition;

    @Column(name = "pdb_insertion_code")
    private String pdbInsertionCode;

    @Id
    @Column(name = "uniprot_position")
    private long uniprotPosition;

    @Column(name = "`match`") // match is a reserved word, need to escape!
    private String match;

    protected PdbUniprotResidueMapping() {}

    public PdbUniprotResidueMapping(long alignmentId, long uniprotPosition)
    {
        this.alignmentId = alignmentId;
        this.uniprotPosition = uniprotPosition;
    }

    public PdbUniprotResidueMapping(long alignmentId,
        Long pdbPosition,
        String pdbInsertionCode,
        long uniprotPosition,
        String match)
    {
        this.alignmentId = alignmentId;
        this.pdbPosition = pdbPosition;
        this.pdbInsertionCode = pdbInsertionCode;
        this.uniprotPosition = uniprotPosition;
        this.match = match;
    }

    public long getAlignmentId()
    {
        return alignmentId;
    }

    public void setAlignmentId(long alignmentId)
    {
        this.alignmentId = alignmentId;
    }


//    public PdbUniprotAlignment getAlignment()
//    {
//        return alignment;
//    }
//
//    public void setAlignment(PdbUniprotAlignment alignment)
//    {
//        this.alignment = alignment;
//    }

    public Long getPdbPosition()
    {
        return pdbPosition;
    }

    public void setPdbPosition(Long pdbPosition)
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
