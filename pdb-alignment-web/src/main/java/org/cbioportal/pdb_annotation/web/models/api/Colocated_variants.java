package org.cbioportal.pdb_annotation.web.models.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Model for ensembl API return in JSON format
 * 
 * e.g.
 * http://grch37.rest.ensembl.org/vep/human/hgvs/X:g.66937331T%3EA?content-type=
 * application/json&protein=1
 * 
 * @author Juexin Wang
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Colocated_variants {
    private String allele_string;
    private long end;
    private String id;
    private int phenotype_or_disease;
    private String seq_region_name;
    private int somatic;
    private long start;
    private int strand;

    public String getAllele_string() {
        return allele_string;
    }

    public void setAllele_string(String allele_string) {
        this.allele_string = allele_string;
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

    public int getPhenotype_or_disease() {
        return phenotype_or_disease;
    }

    public void setPhenotype_or_disease(int phenotype_or_disease) {
        this.phenotype_or_disease = phenotype_or_disease;
    }

    public String getSeq_region_name() {
        return seq_region_name;
    }

    public void setSeq_region_name(String seq_region_name) {
        this.seq_region_name = seq_region_name;
    }

    public int getSomatic() {
        return somatic;
    }

    public void setSomatic(int somatic) {
        this.somatic = somatic;
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

    @Override
    public String toString() {
        return "Colocated_variants [allele_string=" + allele_string + ", end=" + end + ", id=" + id
                + ", phenotype_or_disease=" + phenotype_or_disease + ", seq_region_name=" + seq_region_name
                + ", somatic=" + somatic + ", start=" + start + ", strand=" + strand + "]";
    }

}
