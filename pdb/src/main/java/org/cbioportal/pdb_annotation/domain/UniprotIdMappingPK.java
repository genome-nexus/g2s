package org.cbioportal.pdb_annotation.domain;

import java.io.Serializable;

/**
 * @author Selcuk Onur Sumer
 */
public class UniprotIdMappingPK implements Serializable
{
    private long entrezGeneId;
    private String uniprotId;

    public UniprotIdMappingPK() {}

    public UniprotIdMappingPK(long entrezGeneId, String uniprotId)
    {
        this.entrezGeneId = entrezGeneId;
        this.uniprotId = uniprotId;
    }

    public boolean equals(Object object) {
        if (object instanceof UniprotIdMappingPK) {
            UniprotIdMappingPK pk = (UniprotIdMappingPK)object;
            return entrezGeneId == pk.entrezGeneId && uniprotId.equals(pk.uniprotId);
        } else {
            return false;
        }
    }

    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (entrezGeneId ^ (entrezGeneId >>> 32));
        result = prime * result + (int) (uniprotId.hashCode() ^ (uniprotId.hashCode() >>> 32));
        return result;
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
}
