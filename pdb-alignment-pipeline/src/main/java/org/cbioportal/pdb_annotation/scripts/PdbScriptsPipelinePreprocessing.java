package org.cbioportal.pdb_annotation.scripts;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.io.FastaReaderHelper;
import org.biojava.nbio.core.sequence.io.FastaWriterHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Preprocessing the input PDB and Ensembl files, both in init and update pipeline
 * 
 * @author Juexin Wang
 *
 */
@Component
public class PdbScriptsPipelinePreprocessing {
	
	
    public String ensembl_input_interval;
	
	public int ensembl_file_count;
	
	public PdbScriptsPipelinePreprocessing(){
		this.ensembl_file_count = -1;	
	}
	
	public PdbScriptsPipelinePreprocessing(String ensembl_input_interval){
		this.ensembl_file_count = -1;	
		this.ensembl_input_interval = ensembl_input_interval;
	}
	
	
	
	public int getEnsembl_file_count() {
		return ensembl_file_count;
	}



	public void setEnsembl_file_count(int ensembl_file_count) {
		this.ensembl_file_count = ensembl_file_count;
	}



	/**
	 * Preprocess the PDB sequences download from PDB
	 * (ftp://ftp.rcsb.org/pub/pdb/derived_data/pdb_seqres.txt.gz) Only choose
	 * protein entries of PDB
	 * 
	 * @param infileName:
	 *            downloaded gunzip file
	 * @param outfileName:
	 *            input for makeblastdb
	 * @return Success/Failure
	 */
	public boolean preprocessPDBsequences(String infileName, String outfileName) {
		try {
			System.out.println("[Preprocessing] Preprocessing PDB sequences... ");
			LinkedHashMap<String, ProteinSequence> a = FastaReaderHelper.readFastaProteinSequence(new File(infileName));
			// FastaReaderHelper.readFastaDNASequence for DNA sequences

			StringBuffer sb = new StringBuffer();

			for (Entry<String, ProteinSequence> entry : a.entrySet()) {
				String[] tmp = entry.getValue().getOriginalHeader().toString().split("\\s+");
				if (tmp[1].equals("mol:protein")) {
					// System.out.println( entry.getValue().getOriginalHeader()
					// + "\t" + entry.getValue().getSequenceAsString() );
					sb.append(">" + entry.getValue().getOriginalHeader() + "\n" + entry.getValue().getSequenceAsString()
							+ "\n");
				}
			}
			// standard fasta output, one line has 80 coloumns
			// FastaWriterHelper.writeProteinSequence(new File(outfileName), c);

			// no-standard fasta output, one line contains all AA
			FileWriter fw = new FileWriter(new File(outfileName));
			fw.write(sb.toString());
			fw.close();
		} catch (Exception ex) {
			System.err.println("[Preprocessing] Fatal Error: Could not Successfully Preprocessing PDB sequences");
			ex.printStackTrace();
		}
		return true;
	}
	
	/**
	 * preprocess PDB sequence update
	 * @param infileName
	 * @param outfileName
	 * @return
	 */
	public boolean preprocessPDBsequencesUpdate(String infileName, String outfileName) {
		try {
			System.out.println("[Preprocessing] Preprocessing PDB sequences... ");
			LinkedHashMap<String, ProteinSequence> a = FastaReaderHelper.readFastaProteinSequence(new File(infileName));
			// FastaReaderHelper.readFastaDNASequence for DNA sequences

			StringBuffer sb = new StringBuffer();

			for (Entry<String, ProteinSequence> entry : a.entrySet()) {
				//System.out.println(entry.getValue().getOriginalHeader().toString());
				String[] tmp = entry.getValue().getOriginalHeader().toString().split("\\|");
				String outstr = tmp[0].replaceAll(":", "_");
				//System.out.println(tmp[0]+"\t##\t"+outstr);
				sb.append(">" + outstr + "\n" + entry.getValue().getSequenceAsString()
							+ "\n");
				
			}

			// no-standard fasta output, one line contains all AA
			FileWriter fw = new FileWriter(new File(outfileName));
			fw.write(sb.toString());
			fw.close();
		} catch (Exception ex) {
			System.err.println("[Preprocessing] Fatal Error: Could not Successfully Preprocessing PDB sequences");
			ex.printStackTrace();
		}
		return true;
	}

	/**
	 * Preprocess the Gene sequences download from Ensembl
	 * (ftp://ftp.ensembl.org/pub/release-84/fasta/homo_sapiens/pep/Homo_sapiens.GRCh38.pep.all.fa.gz) 
	 * This function is designed to split the original FASTA file into several small files. Each small files contains
	 * Constants.ensembl_input_interval lines The purpose of doing this is
	 * saving memory in next step
	 * 
	 * @param infilename:
	 *            downloaded file
	 * @param outfilename:
	 *            processed file
	 * @return count
	 */
	public int preprocessGENEsequences(String infilename, String outfilename) {
		// count of all generated small files
		int filecount = 0;
		try {
			System.out.println("[Preprocessing] Preprocessing Ensembl sequences... ");

			LinkedHashMap<String, ProteinSequence> a = FastaReaderHelper.readFastaProteinSequence(new File(infilename));
			// FastaReaderHelper.readFastaDNASequence for DNA sequences

			Collection<ProteinSequence> c = new ArrayList<ProteinSequence>();
			// line count of the original FASTA file
			int count = 0;
			int ensembl_input_interval_int = Integer.parseInt(this.ensembl_input_interval);

			
			for (Entry<String, ProteinSequence> entry : a.entrySet()) {
				c.add(entry.getValue());
				if (count % ensembl_input_interval_int == ensembl_input_interval_int - 1) {
					FastaWriterHelper
							.writeProteinSequence(new File(outfilename + "." + new Integer(filecount).toString()), c);
					c.clear();
					filecount++;
				}
				count++;
			}
			FastaWriterHelper.writeProteinSequence(new File(outfilename + "." + new Integer(filecount++).toString()),
					c);
			setEnsembl_file_count(filecount);
		} catch (Exception ex) {
			System.err.println("[Preprocessing] Fatal Error: Could not Successfully Preprocessing PDB sequences");
			ex.printStackTrace();
		}
		return filecount;
	}

}
