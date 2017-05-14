package org.cbioportal.pdb_annotation.web.models;

import java.util.List;

/**
 * Used for API usage
 * 
 * @author wangjue
 *
 */
public class ProteinSequenceResidue {

    private ProteinSequenceParamResidue param;

    private BlastStatistics blastStat;

    private List<CompleteResidue> residues;

    public ProteinSequenceParamResidue getParam() {
        return param;
    }

    public void setParam(ProteinSequenceParamResidue param) {
        this.param = param;
    }

    public BlastStatistics getBlastStat() {
        return blastStat;
    }

    public void setBlastStat(BlastStatistics blastStat) {
        this.blastStat = blastStat;
    }

    public List<CompleteResidue> getResidues() {
        return residues;
    }

    public void setResidues(List<CompleteResidue> residues) {
        this.residues = residues;
    }

}
