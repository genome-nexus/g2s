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
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.log4j.Logger;
import org.biojava.nbio.structure.Chain;
import org.biojava.nbio.structure.Structure;
import org.biojava.nbio.structure.StructureIO;

/**
 * Read PDB local file Utils
 * 
 * @author Juexin Wang
 *
 */
public class PdbSequenceUtil {
    final static Logger log = Logger.getLogger(PdbSequenceUtil.class);

    /**
     * read PDB file and generate the results to a String
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
            }
            for (Chain c : s.getChains()) {
                outstr = outstr + ">" + s.getPDBCode().toLowerCase() + "_" + c.getChainID() + " " + molClassification
                        + " length:" + c.getAtomSequence().toString().length() + " " + s.getDBRefs().get(0).getSeqBegin() + " " + s.getDBRefs().get(0).getSeqEnd() + "\n"
                        + c.getAtomSequence() + "\n";
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
