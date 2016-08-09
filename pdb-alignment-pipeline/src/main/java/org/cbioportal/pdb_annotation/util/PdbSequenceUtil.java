package org.cbioportal.pdb_annotation.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.PDBHeader;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureIO;

public class PdbSequenceUtil {
	
	public String readPDB2Results (String pdbFileName){
		String outstr = "";
		try {
			Structure s = StructureIO.getStructure(pdbFileName);
			PDBHeader ph = s.getPDBHeader();
			String molClassification = "mol:na";
			if(s.getPDBHeader().getClassification().toLowerCase().contains("protein")){
				molClassification = "mol:protein";
			}
			for ( Chain c : s.getChains()) {	
				outstr = outstr+">"+s.getPDBCode().toLowerCase()+"_"+c.getChainID()+" "+molClassification+" length:"+c.getAtomSequence().toString().length()+" "+ph.getClassification()+"\n"+c.getAtomSequence()+"\n";
	        }
	       } catch (Exception ex) {
	           ex.printStackTrace();
	       }
		return outstr;
	}
	
	public void initSequencefromAll(String dirname, String outfilename) {
		try {

			File dir = new File(dirname);
			System.out.println("Getting all files in " + dir.getCanonicalPath() + " including those in subdirectories");
			List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			int count = 1;
			

	    	List outlist = new ArrayList();
			for (File file : files) {
				//System.out.println(count + "\t" + file.getCanonicalPath());
				String[] array =  file.getCanonicalPath().split("/");
				String gzfilename = array[array.length-1];
				String decompressedFile = "/home/wangjue/gsoc/"+gzfilename.substring(0, 11);
				unGunzipFile(file.getCanonicalPath(), decompressedFile);
								
				outlist.add(readPDB2Results(decompressedFile));				
				FileUtils.forceDelete(new File(decompressedFile));
				count++;
			}
			
			FileUtils.writeLines(new File(outfilename), outlist, "");
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void checkSequencefromAll(String dirname) {
		try {

			File dir = new File(dirname);
			System.out.println("Getting all files in " + dir.getCanonicalPath() + " including those in subdirectories");
			List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
			int count = 1;
			HashMap<Integer,Integer> conHm = new HashMap();
			HashMap<Integer,Integer> segHm = new HashMap();
			HashMap<String,Integer> startHm = new HashMap();
			startHm.put("start1", 0);
			startHm.put("startnot1", 0);

	    	List outlist = new ArrayList();
			for (File file : files) {
				//System.out.println(count + "\t" + file.getCanonicalPath());
				String[] array =  file.getCanonicalPath().split("/");
				String gzfilename = array[array.length-1];
				String decompressedFile = "/home/wangjue/gsoc/"+gzfilename.substring(0, 11);
				unGunzipFile(file.getCanonicalPath(), decompressedFile);
				//Check whether get segmented
			    checkContinuous(decompressedFile, conHm, segHm, startHm);				
				outlist.add(readPDB2Results(decompressedFile));				
				FileUtils.forceDelete(new File(decompressedFile));
				count++;
			}
			
			FileUtils.writeLines(new File("/home/wangjue/gsoc/new_seq_res.txt"), outlist, "");
			
			System.out.println("Segmentation Number of each chain:");
			Iterator it = conHm.keySet().iterator();
			while(it.hasNext()){
				int key = Integer.parseInt(it.next().toString());
				System.out.println(key+"\t"+conHm.get(key));								
			}
			
			System.out.println("Length of segmentation:");
			it = segHm.keySet().iterator();
			while(it.hasNext()){
				int key = Integer.parseInt(it.next().toString());
				System.out.println(key+"\t"+segHm.get(key));								
			}
			
			System.out.println("For single segmentation, how many are from residue 1");
			System.out.println("Start from 1:\t"+startHm.get("start1"));
			System.out.println("Start not from 1:\t"+startHm.get("startnot1"));
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public void unGunzipFile(String compressedFile, String decompressedFile) {
		byte[] buffer = new byte[1024];
		try {
			FileInputStream fileIn = new FileInputStream(compressedFile);
			GZIPInputStream gZIPInputStream = new GZIPInputStream(fileIn);
			FileOutputStream fileOutputStream = new FileOutputStream(decompressedFile);
			int bytes_read;
			while ((bytes_read = gZIPInputStream.read(buffer)) > 0) {
				fileOutputStream.write(buffer, 0, bytes_read);
			}
			gZIPInputStream.close();
			fileOutputStream.close();
			//System.out.println("The file was decompressed successfully!");
			
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
	
	public HashMap<Integer, Integer> checkContinuous(String filename, HashMap<Integer, Integer> conHm, HashMap<Integer,Integer> segHm, HashMap<String,Integer> startHm){
		try{
			List<String> list=FileUtils.readLines(new File(filename));
			HashMap<String, Integer> hm = new HashMap();
			ArrayList<String> al = new ArrayList();
			for(String str: list){
				if(str.startsWith("DBREF") && str.substring(5, 6).equals(" ")){
					String[] array = str.split("\\s+");
					if(hm.containsKey(array[2]) && array.length == 10){
						hm.put(array[2], Integer.parseInt(hm.get(array[2]).toString())+1);
					}else{
						hm.put(array[2], 1);
					}
					al.add(str);
				}
			}
			
			for(String str: al){
				String[] array = str.split("\\s+");
				if(hm.get(array[2]) != 1){
					int length= Integer.parseInt(array[4])-Integer.parseInt(array[3])+1;
					if(segHm.containsKey(length)){
						segHm.put(length, segHm.get(length)+1);
					}else{
						segHm.put(length, 1);
					}
				}else{
					if(array[3].matches("-?\\d+(\\.\\d+)?")){
						if(array[3].equals("1") ){
							startHm.put("start1", startHm.get("start1")+1);
						}else {
							startHm.put("startnot1", startHm.get("startnot1")+1);
						}						
					}
					
				}
			}
			
			Iterator it = hm.keySet().iterator();
			while(it.hasNext()){
				String key = it.next().toString();
				int value = hm.get(key);
				//System.out.println(key+"\t"+value);
				if(conHm.containsKey(value)){
					int tmp = conHm.get(value);
					tmp++;
					conHm.put(value, tmp);
					//System.out.println("*\t"+value+"\t"+tmp);
				}else{
					conHm.put(value, 1);
					//System.out.println("*\t"+value+"\t"+1);
				}				
			}
			hm= null;
		}catch(Exception ex){
			ex.printStackTrace();
		}
		
		return conHm;
	}

}
