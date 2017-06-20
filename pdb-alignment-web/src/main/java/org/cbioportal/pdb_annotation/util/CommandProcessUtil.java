package org.cbioportal.pdb_annotation.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import org.cbioportal.pdb_annotation.web.models.InputSequence;

/**
 * Command Utils for dealing with the processes invoked by JAVA
 *
 * @author Juexin Wang
 *
 */
public class CommandProcessUtil {
    final static Logger log = Logger.getLogger(CommandProcessUtil.class);

    /**
     * output process Errors from process
     *
     * @param process
     */
    private void outputProcessError(Process process, int shellReturnCode, String commandName) {
        try {
            if (shellReturnCode != 0) {
                log.error("[Process] Process Error in " + commandName + ":" + process.toString());
                String errorInfo = "";
                InputStream error = process.getErrorStream();
                boolean done = false;
                while (!done) {
                    int buf = error.read();
                    if (buf == -1)
                        break;
                    errorInfo = errorInfo + (char) buf;
                }
                log.error("[Process] Error: " + errorInfo);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Check the parameters of the command
     *
     * @param commandName
     * @param paralist
     */
    private void checkCommandParam(String commandName, ArrayList<String> paralist) {
        boolean checkFlag = true;
        switch (commandName) {
        case "wget":
            if (paralist.size() != 2) {
                checkFlag = false;
            }
            break;
        case "gunzip":
            if (paralist.size() != 2) {
                checkFlag = false;
            }
            break;
        case "gzip":
            if (paralist.size() != 1) {
                checkFlag = false;
            }
            break;
        case "makeblastdb":
            if (paralist.size() != 2) {
                checkFlag = false;
            }
            break;
        case "blastp":
            if (paralist.size() != 3) {
                checkFlag = false;
            }
            break;
        case "mysql":
            if (paralist.size() != 1) {
                checkFlag = false;
            }
            break;
        case "rsync":
            if (paralist.size() != 1) {
                checkFlag = false;
            }
            break;
        case "rm":
            if (paralist.size() != 1) {
                checkFlag = false;
            }
            break;
        default:
            log.error("[SHELL] Command " + commandName + " does not support now");
            break;
        }
        if (!checkFlag) {
            log.error("[SHELL] Fatal Error: Parameters for " + commandName
                    + " does not make sense, please check. Now the program is exit");
            System.exit(0);
        }
    }

    /**
     * main entrance of running command in shell
     *
     * @param commandName
     * @param paralist
     * @param inputsequence
     * @return
     */
    public int runCommand(String commandName, ArrayList<String> paralist, InputSequence inputsequence) {
        int shellReturnCode = 0;
        try {
            checkCommandParam(commandName, paralist);
            ProcessBuilder pb = null;
            switch (commandName) {
            case "blastp":
                log.info("[BLAST] Running blastp command query " + paralist.get(0) + "...");
                pb = new ProcessBuilder(
                        makeBlastPCommand(paralist.get(0), paralist.get(1), paralist.get(2), inputsequence));
                break;
            case "rm":
                log.info("[SHELL] Running rm command at" + paralist.get(0) + "...");
                pb = new ProcessBuilder(makdeRmCommand(paralist.get(0)));
                break;
            default:
                log.error("[SHELL] Command " + commandName + " does not support now");
                break;
            }
            Process pc = pb.start();
            pc.waitFor();
            shellReturnCode = pc.exitValue();
            outputProcessError(pc, shellReturnCode, commandName);
            log.info("[SHELL] Command " + commandName + " completed");
        } catch (Exception ex) {
            log.error("[SHELL] Fatal Error: Could not Successfully process command, exit the program now");
            log.error(ex.getMessage());
            ex.printStackTrace();
            System.exit(0);
        }
        return shellReturnCode;
    }

    /**
     * Helper Function for building the following command : blastp -db
     * pdb_seqres.db -query Homo_sapiens.GRCh38.pep.all.fa -word_size 11 -evalue
     * 1e-60 -num_threads 6 -outfmt 5 -out pdb_seqres.xml
     *
     * @return A List of command arguments for the processbuilder
     */
    private List<String> makeBlastPCommand(String queryFilename, String outFilename, String dbFilename,
            InputSequence inputsequence) {
        List<String> list = new ArrayList<String>();
        list.add(ReadConfig.blastp);
        list.add("-db");
        list.add(dbFilename);
        list.add("-query");
        list.add(queryFilename);

        // parameters from the form
        list.add("-evalue");
        list.add(inputsequence.getEvalue());
        list.add("-word_size");
        list.add(inputsequence.getWord_size());
        list.add("-gapopen");
        list.add(inputsequence.getGapopen());
        list.add("-gapextend");
        list.add(inputsequence.getGapextend());
        list.add("-matrix");
        list.add(inputsequence.getMatrix());
        list.add("-comp_based_stats");
        list.add(inputsequence.getComp_based_stats());
        list.add("-threshold");
        list.add(inputsequence.getThreshold());
        list.add("-window_size");
        list.add(inputsequence.getWindow_size());
        list.add("-num_threads");
        list.add(ReadConfig.blastParaThreads);
        list.add("-outfmt");
        list.add("5");
        list.add("-out");
        list.add(outFilename);
        return list;
    }

    /**
     * generate rm command
     * 
     * @param inFilename
     * @return
     */
    private List<String> makdeRmCommand(String inFilename) {
        List<String> list = new ArrayList<String>();
        list.add("rm");
        list.add("-fr");
        list.add(inFilename);
        return list;
    }

}
