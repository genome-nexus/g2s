package org.cbioportal.pdb_annotation.util;

/**
 * Static Contents
 * @author Juexin Wang
 *
 */
public final class Constants {

    // Defaults
    public static String makeblastdb = "makeblastdb";
    public static String blastp = "blastp";
    public static String workspace = "/home/wangjue/gsoc/";
    public static String resource_dir= "/home/wangjue/workspace/pdb-annotation/pdb/src/main/resources/";
    public static String tmpdir = "/tmp/";
    public static String pdb_seqres_download_file = "pdb_seqres_50000.txt";
    public static String pdb_seqres_fasta_file = "pdb_seqres_50000.fasta";
    public static String ensembl_download_file = "Homo_sapiens.GRCh38.pep.all_10000.fa";
    public static String ensembl_fasta_file = "Homo_sapiens.GRCh38.pep.all_10000.fa";
    public static String sql_insert_file = "insert.sql";
    
    // Parameters of blast
    public static String blast_para_evalue="1e-60";
    public static String blast_para_threads="6";
    
    // intervals to split the input files
    public static int ensembl_input_interval = 3000;
    public static int sql_insert_output_interval = 10000;
    
    // mysql
    public static String mysql = "mysql";
    public static String username="cbio";
    public static String password="cbio";
    public static String db_schema = "pdb";
    public static String db_schema_script = "pdb.sql";
    public static String db_input_script = "insert.sql";

}
