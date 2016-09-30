package org.cbioportal.pdb_annotation.util.pdb;

public class SegmentRecord {
    int segmentStart;
    int segmentEnd;
    String aaSequence;
    public int getSegmentStart() {
        return segmentStart;
    }
    public void setSegmentStart(int segmentStart) {
        this.segmentStart = segmentStart;
    }
    public int getSegmentEnd() {
        return segmentEnd;
    }
    public void setSegmentEnd(int segmentEnd) {
        this.segmentEnd = segmentEnd;
    }
    public String getAaSequence() {
        return aaSequence;
    }
    public void setAaSequence(String aaSequence) {
        this.aaSequence = aaSequence;
    }
    
    
    
}
