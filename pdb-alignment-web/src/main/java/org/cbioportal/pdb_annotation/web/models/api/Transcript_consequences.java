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
public class Transcript_consequences {

    private String biotype;
    private String gene_id; // ENSG
    private String protein_id; // ENSP
    private int protein_start;
    private String transcript_id;

    /*
     * Only use the useful information
     * 
     * private String amino_acids; private long cdna_end; private long
     * cdna_start; private long cds_end; private long cds_start; private String
     * codons; private List<String> consequence_terms; private String
     * gene_symbol; private String gene_symbol_source; private long hgnc_id;
     * private String impact; private String polyphen_prediction; private double
     * polyphen_score; private int protein_end; private String sift_prediction;
     * private double sift_score; private int strand; private String
     * variant_allele;
     * 
     * 
     * @Override public String toString() { return
     * "Transcript_consequences [amino_acids=" + amino_acids + ", biotype=" +
     * biotype + ", cdna_end=" + cdna_end + ", cdna_start=" + cdna_start +
     * ", cds_end=" + cds_end + ", cds_start=" + cds_start + ", codons=" +
     * codons + ", consequence_terms=" + consequence_terms + ", gene_id=" +
     * gene_id + ", gene_symbol=" + gene_symbol + ", gene_symbol_source=" +
     * gene_symbol_source + ", hgnc_id=" + hgnc_id + ", impact=" + impact +
     * ", polyphen_prediction=" + polyphen_prediction + ", polyphen_score=" +
     * polyphen_score + ", protein_end=" + protein_end + ", protein_id=" +
     * protein_id + ", protein_start=" + protein_start + ", sift_prediction=" +
     * sift_prediction + ", sift_score=" + sift_score + ", strand=" + strand +
     * ", transcript_id=" + transcript_id + ", variant_allele=" + variant_allele
     * + "]"; }
     */

    public String getBiotype() {
        return biotype;
    }

    public void setBiotype(String biotype) {
        this.biotype = biotype;
    }

    public String getGene_id() {
        return gene_id;
    }

    public void setGene_id(String gene_id) {
        this.gene_id = gene_id;
    }

    public String getProtein_id() {
        return protein_id;
    }

    public void setProtein_id(String protein_id) {
        this.protein_id = protein_id;
    }

    public int getProtein_start() {
        return protein_start;
    }

    public void setProtein_start(int protein_start) {
        this.protein_start = protein_start;
    }

    public String getTranscript_id() {
        return transcript_id;
    }

    public void setTranscript_id(String transcript_id) {
        this.transcript_id = transcript_id;
    }

    @Override
    public String toString() {
        return "Transcript_consequences [biotype=" + biotype + ", gene_id=" + gene_id + ", protein_id=" + protein_id
                + ", protein_start=" + protein_start + ", transcript_id=" + transcript_id + "]";
    }

}
