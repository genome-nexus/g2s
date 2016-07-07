package org.cbioportal.pdb_annotation.util;

import java.io.File;
import java.util.*;

import org.apache.commons.io.FileUtils;

public class ReadConfig {
	
    public static String makeblastdb;
    public static String blastp;
    public static String workspace;
    public static String resource_dir;
    public static String tmpdir;
    public static String pdb_seqres_download_file;
    public static String pdb_seqres_fasta_file;
    public static String ensembl_download_file;
    public static String ensembl_fasta_file;
    public static String sql_insert_file;
    public static String blast_para_evalue;
    public static String blast_para_threads;
    public static String ensembl_input_interval;
    public static String sql_insert_output_interval;
    public static String mysql;
    public static String username;
    public static String password;
    public static String db_schema;
    public static String db_schema_script;
    public static String db_input_script;
    
    public static String pdbwholeSource;
    public static String ensemblwholeSource;


	
	public ReadConfig(){
		try{
			HashMap hm = new HashMap();
			File file = new File("src/main/resources/application.properties");
			List<String> list =FileUtils.readLines(file);
			for(String str:list){
				if(!str.startsWith("#") && str.length()!=0 && !str.isEmpty()){
					//System.out.println(str);
					String[] array =str.split("=");
					hm.put(array[0], array[1]);
				}
			}
			if(hm.containsKey("makeblastdb")){
				this.makeblastdb= hm.get("makeblastdb").toString();				
			}
			if(hm.containsKey("blastp")){
				this.blastp= hm.get("blastp").toString();				
			}
			if(hm.containsKey("workspace")){
				this.workspace= hm.get("workspace").toString();				
			}
			if(hm.containsKey("resource_dir")){
				this.resource_dir= hm.get("resource_dir").toString();				
			}
			if(hm.containsKey("tmpdir")){
				this.tmpdir= hm.get("tmpdir").toString();				
			}
			if(hm.containsKey("pdb_seqres_download_file")){
				this.pdb_seqres_download_file= hm.get("pdb_seqres_download_file").toString();				
			}
			if(hm.containsKey("pdb_seqres_fasta_file")){
				this.pdb_seqres_fasta_file= hm.get("pdb_seqres_fasta_file").toString();				
			}
			if(hm.containsKey("ensembl_download_file")){
				this.ensembl_download_file= hm.get("ensembl_download_file").toString();				
			}
			if(hm.containsKey("ensembl_fasta_file")){
				this.ensembl_fasta_file= hm.get("ensembl_fasta_file").toString();				
			}
			if(hm.containsKey("sql_insert_file")){
				this.sql_insert_file= hm.get("sql_insert_file").toString();				
			}
			if(hm.containsKey("blast_para_evalue")){
				this.blast_para_evalue= hm.get("blast_para_evalue").toString();				
			}
			if(hm.containsKey("blast_para_threads")){
				this.blast_para_threads= hm.get("blast_para_threads").toString();				
			}
			if(hm.containsKey("ensembl_input_interval")){
				this.ensembl_input_interval= hm.get("ensembl_input_interval").toString();				
			}
			
			if(hm.containsKey("sql_insert_output_interval")){
				this.sql_insert_output_interval= hm.get("sql_insert_output_interval").toString();				
			}
			if(hm.containsKey("mysql")){
				this.mysql= hm.get("mysql").toString();				
			}
			if(hm.containsKey("username")){
				this.username= hm.get("username").toString();				
			}
			if(hm.containsKey("password")){
				this.password= hm.get("password").toString();				
			}
			if(hm.containsKey("db_schema")){
				this.db_schema= hm.get("db_schema").toString();				
			}
			if(hm.containsKey("db_schema_script")){
				this.db_schema_script= hm.get("db_schema_script").toString();				
			}
			if(hm.containsKey("db_input_script")){
				this.db_input_script= hm.get("db_input_script").toString();				
			}
			if(hm.containsKey("pdb.wholeSource")){
				this.pdbwholeSource= hm.get("pdb.wholeSource").toString();				
			}
			if(hm.containsKey("ensembl.wholeSource")){
				this.ensemblwholeSource= hm.get("ensembl.wholeSource").toString();				
			}
			
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}

}
