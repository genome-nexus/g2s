package org.cbioportal.pdb_annotation.web.models;

import java.util.List;

public class ProteinSequenceAlignment {

    private ProteinSequenceParam param;

    private BlastStatistics blastStat;

    private List<CompleteAlignment> alignment;

    public ProteinSequenceParam getParam() {
        return param;
    }

    public void setParam(ProteinSequenceParam param) {
        this.param = param;
    }

    public BlastStatistics getBlastStat() {
        return blastStat;
    }

    public void setBlastStat(BlastStatistics blastStat) {
        this.blastStat = blastStat;
    }

    public List<CompleteAlignment> getAlignment() {
        return alignment;
    }

    public void setAlignment(List<CompleteAlignment> alignment) {
        this.alignment = alignment;
    }

}
