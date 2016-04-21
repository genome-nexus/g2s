package org.cbioportal.pdb_annotation.domain;

import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;

/**
 * @author Selcuk Onur Sumer
 */
@Entity
@Table(name="uniprot_id_mapping")
@IdClass(UniprotIdMappingPK.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UniprotIdMapping
{
    @Id
    @Column(name = "entrez_gene_id")
    private long entrezGeneId;

    @Id
    @Column(name = "uniprot_id")
    private String uniprotId;

    @Column(name = "uniprot_acc")
    private String uniprotAcc;

    public UniprotIdMapping() {}

    public UniprotIdMapping(long entrezGeneId, String uniprotId, String uniprotAcc)
    {
        this.entrezGeneId = entrezGeneId;
        this.uniprotId = uniprotId;
        this.uniprotAcc = uniprotAcc;
    }

    public long getEntrezGeneId()
    {
        return entrezGeneId;
    }

    public void setEntrezGeneId(long entrezGeneId)
    {
        this.entrezGeneId = entrezGeneId;
    }

    public String getUniprotId()
    {
        return uniprotId;
    }

    public void setUniprotId(String uniprotId)
    {
        this.uniprotId = uniprotId;
    }

    public String getUniprotAcc()
    {
        return uniprotAcc;
    }

    public void setUniprotAcc(String uniprotAcc)
    {
        this.uniprotAcc = uniprotAcc;
    }
}
