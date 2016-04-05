package org.cbioportal.pdb_annotation.domain;

import java.io.Serializable;

/**
 * @author Selcuk Onur Sumer
 */
public class PdbUniprotResidueMappingPK implements Serializable
{
    private long alignmentId;
    private long uniprotPosition;

    public PdbUniprotResidueMappingPK() {}

    public PdbUniprotResidueMappingPK(long alignmentId, long uniprotPosition)
    {
        this.alignmentId = alignmentId;
        this.uniprotPosition = uniprotPosition;
    }

    public boolean equals(Object object) {
        if (object instanceof PdbUniprotResidueMappingPK) {
            PdbUniprotResidueMappingPK pk = (PdbUniprotResidueMappingPK)object;
            return alignmentId == pk.alignmentId && uniprotPosition == pk.uniprotPosition;
        } else {
            return false;
        }
    }

    public int hashCode() {
        return (alignmentId + "_" +uniprotPosition).hashCode();
    }
}
