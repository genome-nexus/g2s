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

	public String getMakeblastdb() {
		return makeblastdb;
	}

	public void setMakeblastdb(String makeblastdb) {
		this.makeblastdb = makeblastdb;
	}

	public String getBlastp() {
		return blastp;
	}

	public void setBlastp(String blastp) {
		this.blastp = blastp;
	}

	public String getWorkspace() {
		return workspace;
	}

	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}

	public String getResource_dir() {
		return resource_dir;
	}

	public void setResource_dir(String resource_dir) {
		this.resource_dir = resource_dir;
	}

	public String getTmpdir() {
		return tmpdir;
	}

	public void setTmpdir(String tmpdir) {
		this.tmpdir = tmpdir;
	}

	public String getPdb_seqres_download_file() {
		return pdb_seqres_download_file;
	}

	public void setPdb_seqres_download_file(String pdb_seqres_download_file) {
		this.pdb_seqres_download_file = pdb_seqres_download_file;
	}

	public String getPdb_seqres_fasta_file() {
		return pdb_seqres_fasta_file;
	}

	public void setPdb_seqres_fasta_file(String pdb_seqres_fasta_file) {
		this.pdb_seqres_fasta_file = pdb_seqres_fasta_file;
	}

	public String getEnsembl_download_file() {
		return ensembl_download_file;
	}

	public void setEnsembl_download_file(String ensembl_download_file) {
		this.ensembl_download_file = ensembl_download_file;
	}

	public String getEnsembl_fasta_file() {
		return ensembl_fasta_file;
	}

	public void setEnsembl_fasta_file(String ensembl_fasta_file) {
		this.ensembl_fasta_file = ensembl_fasta_file;
	}

	public String getSql_insert_file() {
		return sql_insert_file;
	}

	public void setSql_insert_file(String sql_insert_file) {
		this.sql_insert_file = sql_insert_file;
	}

	public String getBlast_para_evalue() {
		return blast_para_evalue;
	}

	public void setBlast_para_evalue(String blast_para_evalue) {
		this.blast_para_evalue = blast_para_evalue;
	}

	public String getBlast_para_threads() {
		return blast_para_threads;
	}

	public void setBlast_para_threads(String blast_para_threads) {
		this.blast_para_threads = blast_para_threads;
	}

	public String getEnsembl_input_interval() {
		return ensembl_input_interval;
	}

	public void setEnsembl_input_interval(String ensembl_input_interval) {
		this.ensembl_input_interval = ensembl_input_interval;
	}

	public String getSql_insert_output_interval() {
		return sql_insert_output_interval;
	}

	public void setSql_insert_output_interval(String sql_insert_output_interval) {
		this.sql_insert_output_interval = sql_insert_output_interval;
	}

	public String getMysql() {
		return mysql;
	}

	public void setMysql(String mysql) {
		this.mysql = mysql;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getDb_schema() {
		return db_schema;
	}

	public void setDb_schema(String db_schema) {
		this.db_schema = db_schema;
	}

	public String getDb_schema_script() {
		return db_schema_script;
	}

	public void setDb_schema_script(String db_schema_script) {
		this.db_schema_script = db_schema_script;
	}

	public String getDb_input_script() {
		return db_input_script;
	}

	public void setDb_input_script(String db_input_script) {
		this.db_input_script = db_input_script;
	}
	
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
			
			
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
	}

}
