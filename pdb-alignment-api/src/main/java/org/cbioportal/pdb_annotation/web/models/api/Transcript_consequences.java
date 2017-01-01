package org.cbioportal.pdb_annotation.web.models.api;

import java.util.List;

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
    private String amino_acids;
    private String biotype;
    private long cdna_end;
    private long cdna_start;
    private long cds_end;
    private long cds_start;
    private String codons;
    private List<String> consequence_terms;
    private String gene_id;
    private String gene_symbol;
    private String gene_symbol_source;
    private long hgnc_id;
    private String impact;
    private String polyphen_prediction;
    private double polyphen_score;
    private int protein_end;
    private String protein_id; // ENSP
    private int protein_start;
    private String sift_prediction;
    private double sift_score;
    private int strand;
    private String transcript_id;
    private String variant_allele;

    public String getAmino_acids() {
        return amino_acids;
    }

    public String getProtein_id() {
        return protein_id;
    }

    public void setProtein_id(String protein_id) {
        this.protein_id = protein_id;
    }

    public void setAmino_acids(String amino_acids) {
        this.amino_acids = amino_acids;
    }

    public String getBiotype() {
        return biotype;
    }

    public void setBiotype(String biotype) {
        this.biotype = biotype;
    }

    public long getCdna_end() {
        return cdna_end;
    }

    public void setCdna_end(long cdna_end) {
        this.cdna_end = cdna_end;
    }

    public long getCdna_start() {
        return cdna_start;
    }

    public void setCdna_start(long cdna_start) {
        this.cdna_start = cdna_start;
    }

    public long getCds_end() {
        return cds_end;
    }

    public void setCds_end(long cds_end) {
        this.cds_end = cds_end;
    }

    public long getCds_start() {
        return cds_start;
    }

    public void setCds_start(long cds_start) {
        this.cds_start = cds_start;
    }

    public String getCodons() {
        return codons;
    }

    public void setCodons(String codons) {
        this.codons = codons;
    }

    public List<String> getConsequence_terms() {
        return consequence_terms;
    }

    public void setConsequence_terms(List<String> consequence_terms) {
        this.consequence_terms = consequence_terms;
    }

    public String getGene_id() {
        return gene_id;
    }

    public void setGene_id(String gene_id) {
        this.gene_id = gene_id;
    }

    public String getGene_symbol() {
        return gene_symbol;
    }

    public void setGene_symbol(String gene_symbol) {
        this.gene_symbol = gene_symbol;
    }

    public String getGene_symbol_source() {
        return gene_symbol_source;
    }

    public void setGene_symbol_source(String gene_symbol_source) {
        this.gene_symbol_source = gene_symbol_source;
    }

    public long getHgnc_id() {
        return hgnc_id;
    }

    public void setHgnc_id(long hgnc_id) {
        this.hgnc_id = hgnc_id;
    }

    public String getImpact() {
        return impact;
    }

    public void setImpact(String impact) {
        this.impact = impact;
    }

    public String getPolyphen_prediction() {
        return polyphen_prediction;
    }

    public void setPolyphen_prediction(String polyphen_prediction) {
        this.polyphen_prediction = polyphen_prediction;
    }

    public double getPolyphen_score() {
        return polyphen_score;
    }

    public void setPolyphen_score(double polyphen_score) {
        this.polyphen_score = polyphen_score;
    }

    public int getProtein_end() {
        return protein_end;
    }

    public void setProtein_end(int protein_end) {
        this.protein_end = protein_end;
    }

    public int getProtein_start() {
        return protein_start;
    }

    public void setProtein_start(int protein_start) {
        this.protein_start = protein_start;
    }

    public String getSift_prediction() {
        return sift_prediction;
    }

    public void setSift_prediction(String sift_prediction) {
        this.sift_prediction = sift_prediction;
    }

    public double getSift_score() {
        return sift_score;
    }

    public void setSift_score(double sift_score) {
        this.sift_score = sift_score;
    }

    public int getStrand() {
        return strand;
    }

    public void setStrand(int strand) {
        this.strand = strand;
    }

    public String getTranscript_id() {
        return transcript_id;
    }

    public void setTranscript_id(String transcript_id) {
        this.transcript_id = transcript_id;
    }

    public String getVariant_allele() {
        return variant_allele;
    }

    public void setVariant_allele(String variant_allele) {
        this.variant_allele = variant_allele;
    }

    @Override
    public String toString() {
        return "Transcript_consequences [amino_acids=" + amino_acids + ", biotype=" + biotype + ", cdna_end=" + cdna_end
                + ", cdna_start=" + cdna_start + ", cds_end=" + cds_end + ", cds_start=" + cds_start + ", codons="
                + codons + ", consequence_terms=" + consequence_terms + ", gene_id=" + gene_id + ", gene_symbol="
                + gene_symbol + ", gene_symbol_source=" + gene_symbol_source + ", hgnc_id=" + hgnc_id + ", impact="
                + impact + ", polyphen_prediction=" + polyphen_prediction + ", polyphen_score=" + polyphen_score
                + ", protein_end=" + protein_end + ", protein_id=" + protein_id + ", protein_start=" + protein_start
                + ", sift_prediction=" + sift_prediction + ", sift_score=" + sift_score + ", strand=" + strand
                + ", transcript_id=" + transcript_id + ", variant_allele=" + variant_allele + "]";
    }

}
