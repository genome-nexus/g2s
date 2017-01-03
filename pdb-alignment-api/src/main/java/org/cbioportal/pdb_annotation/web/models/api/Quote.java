package org.cbioportal.pdb_annotation.web.models.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Main Model Model for ensembl API return in JSON format
 * 
 * e.g.
 * http://grch37.rest.ensembl.org/vep/human/hgvs/X:g.66937331T%3EA?content-type=
 * application/json&protein=1
 * 
 * @author Juexin Wang
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Quote {

    private String allele_string;
    private String assembly_name;
    private List<Colocated_variants> colocated_variants;
    private long end;
    private String id;
    private String input;
    private String most_severe_consequence;
    private String seq_region_name;
    private long start;
    private int strand;
    private List<Transcript_consequences> transcript_consequences;

    public Quote() {
    }

    public String getAllele_string() {
        return allele_string;
    }

    public void setAllele_string(String allele_string) {
        this.allele_string = allele_string;
    }

    public String getAssembly_name() {
        return assembly_name;
    }

    public void setAssembly_name(String assembly_name) {
        this.assembly_name = assembly_name;
    }

    public List<Colocated_variants> getColocated_variants() {
        return colocated_variants;
    }

    public void setColocated_variants(List<Colocated_variants> colocated_variants) {
        this.colocated_variants = colocated_variants;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public String getMost_severe_consequence() {
        return most_severe_consequence;
    }

    public void setMost_severe_consequence(String most_severe_consequence) {
        this.most_severe_consequence = most_severe_consequence;
    }

    public String getSeq_region_name() {
        return seq_region_name;
    }

    public void setSeq_region_name(String seq_region_name) {
        this.seq_region_name = seq_region_name;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public int getStrand() {
        return strand;
    }

    public void setStrand(int strand) {
        this.strand = strand;
    }

    public List<Transcript_consequences> getTranscript_consequences() {
        return transcript_consequences;
    }

    public void setTranscript_consequences(List<Transcript_consequences> transcript_consequences) {
        this.transcript_consequences = transcript_consequences;
    }

    @Override
    public String toString() {
        return "Quote [allele_string=" + allele_string + ", assembly_name=" + assembly_name + ", colocated_variants="
                + colocated_variants + ", end=" + end + ", id=" + id + ", input=" + input + ", most_severe_consequence="
                + most_severe_consequence + ", seq_region_name=" + seq_region_name + ", start=" + start + ", strand="
                + strand + ", transcript_consequences=" + transcript_consequences + "]";
    }

}
