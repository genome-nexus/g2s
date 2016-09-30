package org.cbioportal.pdb_annotation.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.log4j.Logger;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.DBRef;
import org.biojava.nbio.structure.Group;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureIO;
import org.cbioportal.pdb_annotation.util.pdb.SegmentRecord;

/**
 * Read PDB local file Utils
 * 
 * @author Juexin Wang
 *
 */
public class PdbSequenceUtil {
    final static Logger log = Logger.getLogger(PdbSequenceUtil.class);

    public int convertNumber(String inputStr){
        Pattern pattern = Pattern.compile("([A-Za-z])+");
        Matcher matcher = pattern.matcher(inputStr);

        if (matcher.find()) {
            Scanner in = new Scanner(inputStr).useDelimiter("[^0-9]+");
            int integer = in.nextInt();
            return(integer);
        }else{
            return (Integer.parseInt(inputStr));            
        }
    }
    
    public boolean containsLetter(String str) {       
        for (int i = 0; i < str.length(); i++) {
            if (Character.isLetter(str.charAt(i)))
                return true;
        }
        return false;
    }
    
    
    public String removeCharAt(String s, int i) {
        StringBuffer buf = new StringBuffer(s.length() -1);
        buf.append(s.substring(0, i)).append(s.substring(i+1));
        return buf.toString();
    }
    
    SegmentRecord generateNewSegmentRecord (int start, int end, int aaCount, Chain c, ArrayList<Integer> insertionCodeAl){
        SegmentRecord sr = new SegmentRecord();
        sr.setSegmentStart(start);
        sr.setSegmentEnd(end);
        //System.out.println("###"+aaCount+"\t"+numPrior+"\t"+start);
        //
        if(insertionCodeAl.size()==0){
            sr.setAaSequence(c.getAtomSequence().substring(aaCount, aaCount+end-start+1));                             
        }else{
            String preStr = c.getAtomSequence().substring(aaCount, aaCount+end-start+1+insertionCodeAl.size());
            
            for(int j=insertionCodeAl.size()-1;j>=0;j--){
                //System.out.println("@@@"+preStr+"\t"+j+"\t"+insertionCodeAl.get(j));
                preStr = removeCharAt(preStr, insertionCodeAl.get(j));   
            }
            //System.out.println("@@@"+preStr);
            sr.setAaSequence(preStr); 
        }
        return sr;
    }
    
    /**
     * read PDB file and generate the results to a String
     * 
     * Known bug for Insertion code of PDB:
     * 1. For 1HAG_E, there are 1H, 1G, 1F, 1A,...1, the function will choose the first one: 1H, not 1 as it should be
     * 
     * @param pdbFileName
     * @return
     */
    public String readPDB2Results(String pdbFileName) {
        String outstr = "";
        try {
            
            Structure s = StructureIO.getStructure(pdbFileName);
            String molClassification = "mol:protein";
            if (s.getPDBHeader().getClassification().equals("DNA-RNA HYBRID")
                    || s.getPDBHeader().getClassification().equals("DNA/RNA HYBRID")
                    || s.getPDBHeader().getClassification().equals("DNA BINDING PROTEIN/DNA")
                    || s.getPDBHeader().getClassification().equals("COMPLEX (AMINOACYL-TRNA SYNTHASE/TRNA)")
                    || s.getPDBHeader().getClassification().equals("TRANSCRIPTION/DNA")
                    || s.getPDBHeader().getClassification().equals("HYDROLASE/DNA")
                    || s.getPDBHeader().getClassification().equals("HYDROLASE/RNA")
                    || s.getPDBHeader().getClassification().equals("GENE REGULATION/RNA")
                    || s.getPDBHeader().getClassification().equals("DNA")
                    || s.getPDBHeader().getClassification().equals("RNA")) {
                molClassification = "mol:na";
                //Directly use only protein
                return "";
            }
            for (Chain c : s.getChains()) {
                try{
                //System.out.println("*"+s.getPDBCode().toLowerCase()+"*"+c.getChainID());
                /*
                System.out.println(s.getDBRefs().size());
                for(DBRef dbref:s.getDBRefs()){
                    System.out.println(dbref.getChainId()+"\t"+dbref.getSeqBegin()+"\t"+dbref.getSeqEnd()+"\t"+dbref.getDbSeqBegin()+"\t"+dbref.getDbSeqEnd());
                }
                */               
                
                ArrayList<SegmentRecord> srAl = new ArrayList<SegmentRecord>();
                List<Group> groups = c.getAtomGroups();
                int aaCount = 0;
                if(c.getAtomSequence().length()<1){
                    //log.warn(s.getPDBCode()+"_"+c.getChainID()+": No Chain for AtomSequence<1");
                    continue;
                }
                //One possible bug for biojava, e.g. 3igv_A, the end of chain should be 269Y, not Hetatm 1K
                //Set endCheck to amend this
                boolean endCheck = false;
                
                //PDB is garbled, e.g. 1IAO
                boolean garbledCheck = false;

                /*
                //Test
                for(Group group: c.getSeqResGroups()){
                    System.out.print(group.getChemComp().getOne_letter_code());                   
                }               
                
                System.out.println();                
                System.out.println(c.getSeqResGroups().size());
                System.out.println();
                
                for(Group group: c.getAtomGroups()){
                    System.out.print(group.getChemComp().getOne_letter_code());                   
                }
                
                System.out.println();
                System.out.println("c.getAtomGroups().size():\t"+c.getAtomGroups().size());                
                
                System.out.println(c.getAtomLength());
                System.out.println(c.getAtomSequence());
                System.out.println("c.getAtomSequence().length():\t"+c.getAtomSequence().length());
                
                //int tcount =0;
                //for(Group group: c.getAtomGroups()){
                //    System.out.println(tcount+"\t"+group.getResidueNumber()+"&\t"+group); 
                //    tcount++;
                //}
                
                for(int k=0;k<c.getAtomSequence().length();k++){
                    System.out.println(k+"\t"+c.getAtomGroup(k).getResidueNumber()+"&\t"+c.getAtomGroup(k)); 
                }
                //Test End
                */
                
                
                
                int start = convertNumber(groups.get(0).getResidueNumber().toString());
                int end;
                int seqLength = c.getAtomSequence().length();
                //Check error for biojava bugs in 3h1r : getAtomSequence().length is 226, it should be 224
                if(c.getAtomSequence().length()>c.getAtomLength()){
                    end = convertNumber(groups.get(c.getAtomLength()-1).getResidueNumber().toString()); 
                    seqLength = c.getAtomLength();
                }else{
                    end = convertNumber(groups.get(c.getAtomSequence().length()-1).getResidueNumber().toString()); 
                }
                
                 
                if(end<start){
                    //log.warn(s.getPDBCode()+"_"+c.getChainID()+": One additional Hetatm for End<=Start");
                    endCheck = true;
                }
                
                                
                if(endCheck){
                    seqLength = seqLength-1;
                    end = convertNumber(groups.get(seqLength-1).getResidueNumber().toString());     
                }
                                
                String headstr = ">" + s.getPDBCode().toLowerCase() + "_" + c.getChainID() + " " + molClassification
                        + " length:" + seqLength ;
                String tmpstr= "";
                
                ArrayList<Integer> insertionCodeAl = new ArrayList<Integer>();
                
                for(int i=1;i<seqLength;i++){
                    int numPresent = convertNumber(groups.get(i).getResidueNumber().toString());                   
                    int numPrior = convertNumber(groups.get(i-1).getResidueNumber().toString());
                    //System.out.println(numPrior+" "+groups.get(i-1).getResidueNumber().toString()+" "+groups.get(i-1).getChemComp().getOne_letter_code()+"\t"+numPresent+" "+groups.get(i).getResidueNumber().toString()+" "+groups.get(i).getChemComp().getOne_letter_code());
                    if((numPresent-numPrior)>1 ){            
                            
                            SegmentRecord sr = generateNewSegmentRecord (start, numPrior, aaCount, c, insertionCodeAl);                                                                                   
                            if(numPrior-start>=5){
                                srAl.add(sr); 
                            }                            
                            aaCount = aaCount+numPrior-start+1+insertionCodeAl.size();                                            
                            start = numPresent; 
                            insertionCodeAl = new ArrayList<Integer>();
                    }
                    // In case of insertion code of residue, such as 1dpo_A, residue 221 and 221A, we only choose the first one
                    else if((numPresent-numPrior)==0){
                        insertionCodeAl.add(i-aaCount);
                    }
                    //TODO: potential improvemetns on these < 0, 1yra for example
                    else if ((numPresent-numPrior)<0 ){
                        //if contains Letter, as 1nsa_A, we ignore first 7A-95A
                        if(containsLetter(groups.get(i-1).getResidueNumber().toString()) && !containsLetter(groups.get(i).getResidueNumber().toString())){
                            //log.warn(s.getPDBCode()+"_"+c.getChainID()+": Have Fragments in decreasing order of Insertion codes, ignoring Former Insertion codes fragments");
                            aaCount = aaCount+numPrior-start+1+insertionCodeAl.size();
                            start = convertNumber(groups.get(i).getResidueNumber().toString()); 
                            insertionCodeAl = new ArrayList<Integer>();                          
                        }
                        // if contains Letter, as 1iao_B, 6-94, 94A, 95-188, 1T, 2T. we ignore the following 1T, return the fragment immediately
                        else if(!containsLetter(groups.get(i-1).getResidueNumber().toString()) && containsLetter(groups.get(i).getResidueNumber().toString())){
                            log.warn(s.getPDBCode()+"_"+c.getChainID()+": Have Fragments in decreasing order of Insertion codes, ignoring Later Insertion codes fragments");
                            end = convertNumber(groups.get(i-1).getResidueNumber().toString()); 
                            insertionCodeAl = new ArrayList<Integer>();
                            break;
                        }
                        //if does not contains Letter, then that is error here
                        else{
                            log.warn(s.getPDBCode()+"_"+c.getChainID()+": Garbled! Have Fragments in decreasing order");                            
                            garbledCheck = true;
                            break;
                        }   
                    }                   
                }
                
                if(garbledCheck){
                    garbledCheck = false;
                    continue;
                }
                
                SegmentRecord sr = generateNewSegmentRecord (start, end, aaCount, c, insertionCodeAl);  
                insertionCodeAl = new ArrayList<Integer>();
                if(end-start>=9){
                    srAl.add(sr); 
                }

                
                //Check inner linker
                    ArrayList<SegmentRecord> srAlCopy = new ArrayList<SegmentRecord>();
                    if(srAl.size()>0){
                        srAlCopy.add(srAl.get(0));
                    }else{
                        //log.warn(s.getPDBCode()+"_"+c.getChainID()+": Fragments length is not long enough");
                        continue;
                    }
                    
                    
                    for(int i=1; i<srAl.size(); i++){
                        if(srAl.get(i).getSegmentStart()-srAlCopy.get(srAlCopy.size()-1).getSegmentEnd()-1<=10){
                            int linkNum = srAl.get(i).getSegmentStart()-srAlCopy.get(srAlCopy.size()-1).getSegmentEnd()-1;
                            sr = srAlCopy.get(srAlCopy.size()-1);
                            sr.setSegmentEnd(srAl.get(i).getSegmentEnd());
                            String linkStr = "";
                            for(int j=0; j<linkNum; j++){
                                linkStr = linkStr + "X";
                            }
                            sr.setAaSequence(sr.getAaSequence()+linkStr+srAl.get(i).getAaSequence());
                        }else{
                            srAlCopy.add(srAl.get(i));
                        }
                    }               
                
                int segCount = 1;

                for(SegmentRecord srr:srAlCopy){                    
                    //System.out.println(headerStr + " " + segCount + " " + srr.getSegmentStart() + " " + srr.getSegmentEnd() + "\n" + srr.getAaSequence() + "\n");
                    tmpstr = tmpstr + headstr + " " + segCount + " " + srr.getSegmentStart() + " " + srr.getSegmentEnd() + "\n" + srr.getAaSequence() + "\n";
                    segCount++;                                                      
                }
                
                
                
                /*
                 * Use DBREF, but so many errors there
                int startCheck = Integer.parseInt(c.getAtomGroup(0).getResidueNumber().toString());
                if(s.getDBRefs().size()!=0){

                    int segCount = 1;
                    int aaCount = 0;
                    String tmpChainId = "";
                    for(DBRef dbref:s.getDBRefs()){
                        if(dbref.getChainId().equals(c.getChainID())){
                            int startPos = dbref.getSeqBegin();
                            if(segCount == 1){
                                if(startCheck != dbref.getSeqBegin()){
                                    log.info("Error DBREF in "+s.getPDBCode()+"_"+c.getChainID());
                                    startPos = startCheck;
                                    
                                }
                            }
                        if(!dbref.getChainId().equals(tmpChainId)){
                            aaCount = 0;
                        }
                        tmpChainId = dbref.getChainId();
                        int segLength = dbref.getSeqEnd() - startPos+1;
                        System.out.println(c.getAtomSequence().length());
                        System.out.println(headerStr + " " + segCount + " " + startPos + " " + dbref.getSeqEnd() + " " + dbref.getDbSeqBegin() + " " + dbref.getDbSeqEnd() + "\n" + c.getAtomSequence().substring(aaCount, aaCount+segLength) + "\n");
                        tmpstr = tmpstr + headerStr + " " + segCount + " " + startPos + " " + dbref.getSeqEnd() + " " + dbref.getDbSeqBegin() + " " + dbref.getDbSeqEnd() + "\n" + c.getAtomSequence().substring(aaCount, aaCount+segLength) + "\n";
                        segCount++;
                        aaCount = aaCount+segLength;
                        }                      
                    }
                    
                }else{
                    //In case of "DBREF1" records
                    List<String> list = FileUtils.readLines(new File(pdbFileName));
                    ArrayList<String> al = new ArrayList<String>();
                    String dbStr = "";
                    int dbStrCount = 0;
                    for (String str : list) {
                        if (str.startsWith("DBREF") ) {
                            String[] array = str.split("\\s+");
                            
                            if(dbStrCount == 0){
                                dbStr = array[0]+" "+array[1]+" "+array[2];
                            }                           
                            
                            for(int i=3;i<array.length;i++){
                                dbStr = dbStr+" "+array[i];
                                dbStrCount++;
                            }
                            if(dbStrCount == 7){
                                al.add(dbStr);
                                dbStr = "";
                                dbStrCount = 0;
                            }                            
                        }
                    }
                    
                    //In case of "DBREF" records
                    if(al.size() == 0){
                        log.error("No DBREF in "+s.getPDBCode().toLowerCase()+"_"+c.getChainID());;
                        System.exit(0);
                    }
                    
                    int segCount = 1;
                    int aaCount = 0;

                    for (String str : al) {                       
                        String[] array = str.split("\\s+");                       
                        int segLength = Integer.parseInt(array[4]) - Integer.parseInt(array[3]);
                        System.out.println(headerStr + " " + segCount + " " + array[3] + " " + array[4] + " " + array[8] + " " + array[9] + "\n" + c.getAtomSequence().substring(aaCount, aaCount+segLength) + "\n");
                        tmpstr = tmpstr + headerStr + " " + segCount + " " + array[3] + " " + array[4] + " " + array[8] + " " + array[9] + "\n" + c.getAtomSequence().substring(aaCount, aaCount+segLength) + "\n";
                        segCount++;
                        aaCount = aaCount+segLength;
                        
                    } 
                }
                */
                outstr = outstr + tmpstr;
                }catch(Exception ex){
                    log.error(s.getPDBCode()+"_"+c.getChainID()+": Exception");
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {          
            ex.printStackTrace();
        }
        return outstr;
    }

    /**
     * get sequences from local copy of PDB
     * 
     * @param dirname
     * @param outfilename
     */
    public void initSequencefromAll(String dirname, String outfilename) {
        try {

            // clone the whole PDB locally
            log.info("[PDB] Cloning the local copy of all the PDB ... ");
            CommandProcessUtil cu = new CommandProcessUtil();
            ArrayList<String> paralist = new ArrayList<String>();
            paralist.add(dirname);
            cu.runCommand("rsync", paralist);
            //runCommand( "rsync -rlpt -v -z --delete --port=33444 rsync.rcsb.org::ftp_data/structures/divided/pdb/ "+dirname);			
            log.info("[PDB] The cloing is done");
            File dir = new File(dirname);
            log.info("[PDB] Parsing all PDB files in " + dir.getCanonicalPath() + " and output PDB sequences to " + outfilename + "...");
            List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);			
            List<String> outlist = new ArrayList<String>();
            
            for (File file : files) {
                String[] array = file.getCanonicalPath().split("/");
                String gzfilename = array[array.length - 1];
                String decompressedFile = ReadConfig.tmpdir + gzfilename.substring(0, 11);
                unGunzipFile(file.getCanonicalPath(), decompressedFile);
                outlist.add(readPDB2Results(decompressedFile));
                FileUtils.forceDelete(new File(decompressedFile));
                
            }
            log.info("[PDB] Write PDB sequences ...");
            FileUtils.writeLines(new File(outfilename), outlist, "");
            log.info("[PDB] PDB sequences generation done");
            log.info("[PDB] Deleting the local copy of whole PDB ... ");
            paralist = new ArrayList<String>();
            paralist.add(dirname);
            cu.runCommand("rm", paralist);		
            log.info("[PDB] The local copy of whole PDB has deleted");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Get sequences from defined folder, part of all the PDB
     * 
     * @param dirname
     * @param outfilename
     */
    public void initSequencefromFolder(String dirname, String outfilename){
        
        try {         
            log.info("[PDB] Use part of all PDB");
            
            File dir = new File(dirname);
            System.out.println("Getting all files in " + dir.getCanonicalPath() + " including those in subdirectories");
            List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            List<String> outlist = new ArrayList<String>();
            int fileCount = 0;
            for (File file : files) {
                String[] array = file.getCanonicalPath().split("/");
                String gzfilename = array[array.length - 1];
                String decompressedFile = ReadConfig.tmpdir + gzfilename.substring(0, 11);
                unGunzipFile(file.getCanonicalPath(), decompressedFile);
                outlist.add(readPDB2Results(decompressedFile));
                FileUtils.forceDelete(new File(decompressedFile));
                if(fileCount%1000==0){
                    //System.out.println(fileCount);
                }
                fileCount++;
            }
            log.info("[PDB] Write PDB sequences ...");
            FileUtils.writeLines(new File(outfilename), outlist, "");
            log.info("[PDB] PDB sequences generation done");
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
    }

    /**
     * Travel all the PDB local copy to get statistics on segments
     * 
     * @param dirname
     */
    public void checkSequencefromAll(String dirname) {
        try {

            File dir = new File(dirname);
            System.out.println("Getting all files in " + dir.getCanonicalPath() + " including those in subdirectories");
            List<File> files = (List<File>) FileUtils.listFiles(dir, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
            HashMap<Integer, Integer> conHm = new HashMap<Integer, Integer>();
            HashMap<Integer, Integer> segHm = new HashMap<Integer, Integer>();
            HashMap<String, Integer> startHm = new HashMap<String, Integer>();
            startHm.put("start1", 0);
            startHm.put("startnot1", 0);

            for (File file : files) {
                // System.out.println(count + "\t" + file.getCanonicalPath());
                String[] array = file.getCanonicalPath().split("/");
                String gzfilename = array[array.length - 1];
                String decompressedFile = ReadConfig.tmpdir + gzfilename.substring(0, 11);
                unGunzipFile(file.getCanonicalPath(), decompressedFile);
                // Check whether get segmented
                checkContinuous(decompressedFile, conHm, segHm, startHm);
                FileUtils.forceDelete(new File(decompressedFile));
            }

            System.out.println("Segmentation Number of each chain:");
            Iterator<Integer> it = conHm.keySet().iterator();
            while (it.hasNext()) {
                int key = Integer.parseInt(it.next().toString());
                System.out.println(key + "\t" + conHm.get(key));
            }

            System.out.println("Length of segmentation:");
            it = segHm.keySet().iterator();
            while (it.hasNext()) {
                int key = Integer.parseInt(it.next().toString());
                System.out.println(key + "\t" + segHm.get(key));
            }

            System.out.println("For single segmentation, how many are from residue 1");
            System.out.println("Start from 1:\t" + startHm.get("start1"));
            System.out.println("Start not from 1:\t" + startHm.get("startnot1"));

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    /**
     * unGunzip .gz file to plain file
     * 
     * @param compressedFile
     * @param decompressedFile
     */
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
            // System.out.println("The file was decompressed successfully!");

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Statistics: Used for traveling all the PDB repository, and get statistics
     * 
     * @param filename
     * @param conHm
     * @param segHm
     * @param startHm
     * @return
     */
    public void checkContinuous(String filename, HashMap<Integer, Integer> conHm, HashMap<Integer, Integer> segHm,
            HashMap<String, Integer> startHm) {
        try {
            List<String> list = FileUtils.readLines(new File(filename));
            HashMap<String, Integer> hm = new HashMap<String, Integer>();
            ArrayList<String> al = new ArrayList<String>();
            for (String str : list) {
                if (str.startsWith("DBREF") && str.substring(5, 6).equals(" ")) {
                    String[] array = str.split("\\s+");
                    if (hm.containsKey(array[2]) && array.length == 10) {
                        hm.put(array[2], Integer.parseInt(hm.get(array[2]).toString()) + 1);
                    } else {
                        hm.put(array[2], 1);
                    }
                    al.add(str);
                }
            }

            for (String str : al) {
                String[] array = str.split("\\s+");
                if (hm.get(array[2]) != 1) {
                    int length = Integer.parseInt(array[4]) - Integer.parseInt(array[3]) + 1;
                    if (segHm.containsKey(length)) {
                        segHm.put(length, segHm.get(length) + 1);
                    } else {
                        segHm.put(length, 1);
                    }
                } else {
                    if (array[3].matches("-?\\d+(\\.\\d+)?")) {
                        if (array[3].equals("1")) {
                            startHm.put("start1", startHm.get("start1") + 1);
                        } else {
                            startHm.put("startnot1", startHm.get("startnot1") + 1);
                        }
                    }

                }
            }

            Iterator<String> it = hm.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next().toString();
                int value = hm.get(key);
                // System.out.println(key+"\t"+value);
                if (conHm.containsKey(value)) {
                    int tmp = conHm.get(value);
                    tmp++;
                    conHm.put(value, tmp);
                    // System.out.println("*\t"+value+"\t"+tmp);
                } else {
                    conHm.put(value, 1);
                    // System.out.println("*\t"+value+"\t"+1);
                }
            }
            hm = null;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

}
