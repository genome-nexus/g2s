package org.cbioportal.pdb_annotation.util;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

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
                    if (buf == -1) break;
                    errorInfo = errorInfo + (char)buf;
                }
                log.error("[Process] Error: " + errorInfo);
            }
        }
        catch (Exception ex) {
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
        switch(commandName) {
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
            log.error("[SHELL] Fatal Error: Parameters for " + commandName + " does not make sense, please check. Now the program is exit");
            System.exit(0);
        }
    }

    /**
     * main entrance of running command in shell
     *
     * @param commandName
     * @param paralist
     * @return
     */
    public int runCommand(String commandName, ArrayList<String> paralist) {
        int shellReturnCode=0;
        try {
            checkCommandParam(commandName, paralist);
            ProcessBuilder pb = null;
            switch (commandName) {
            case "wget":
                log.info("[SHELL] Download file " + paralist.get(0) + " to " + paralist.get(1) + " ...");
                pb = new ProcessBuilder(makeDownloadCommand(paralist.get(0), paralist.get(1)));
                break;
            case "gunzip":
                if (!paralist.get(0).endsWith(".gz")) {
                    return 0;
                }
                log.info("[SHELL] Gunzip file from " + paralist.get(0) + " to " + paralist.get(1) + " ...");
                pb = new ProcessBuilder(makeGunzipCommand(paralist.get(0)));
                pb.redirectOutput(ProcessBuilder.Redirect.to(new File(paralist.get(1))));
                break;
            case "gzip":
                log.info("[SHELL] Gzip file from " + paralist.get(0) + " ...");
                pb = new ProcessBuilder(makeGzipCommand(paralist.get(0)));
                break;
            case "makeblastdb":
                log.info("[BLAST] Running makeblastdb command...");
                pb = new ProcessBuilder(makeBlastDBCommand(paralist.get(0), paralist.get(1)));
                break;
            case "blastp":
                log.info("[BLAST] Running blastp command query " + paralist.get(0) + "...");
                pb = new ProcessBuilder(makeBlastPCommand(paralist.get(0), paralist.get(1), paralist.get(2)));
                break;
            case "mysql":
                log.info("[MYSQL] Running mysql command insert " + paralist.get(0) + "...");
                pb = new ProcessBuilder(makeDBCommand());
                pb.redirectInput(ProcessBuilder.Redirect.from(new File(paralist.get(0))));
                break;
            case "rsync":
                log.info("[SHELL] Running rsync command and clone whole PDB to" + paralist.get(0) + "...");
                pb = new ProcessBuilder("rsync -rlpt -v -z --delete --port=33444 rsync.rcsb.org::ftp_data/structures/divided/pdb/ " + paralist.get(0));
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
     * generate wget command
     *
     * @param inFilename
     * @param outFilename
     * @return
     */
    public List<String> makeDownloadCommand(String inFilename, String outFilename) {
        List<String> list = new ArrayList<String>();
        list.add("wget");
        list.add("-O");
        list.add(outFilename);
        list.add(inFilename);
        return list;
    }

    /**
     * generate gunzip command
     *
     * @param inputname
     * @return
     */
    private List<String> makeGunzipCommand(String inFilename) {
        List<String> list = new ArrayList<String>();
        list.add("gunzip");
        list.add("-c");
        list.add("-d");
        list.add(inFilename);
        return list;
    }

    /**
     * generate gzip command
     *
     * @param inFilename
     * @return
     */
    private List<String> makeGzipCommand(String inFilename) {
        List<String> list = new ArrayList<String>();
        list.add("gzip");
        list.add(inFilename);
        return list;
    }

    /**
     * Helper Function for building the makeblastdb command: makeblastdb -in
     * Homo_sapiens.GRCh38.pep.all.fa -dbtype prot -out pdb_seqres.db
     *
     * @return A List containing the commands to execute the makeblastdb
     *         function
     */
    private List<String> makeBlastDBCommand(String inFilename, String outFilename) {
        List<String> list = new ArrayList<String>();
        list.add(ReadConfig.makeblastdb);
        list.add("-in");
        list.add(inFilename);
        list.add("-dbtype");
        list.add("prot");
        list.add("-out");
        list.add(outFilename);
        return list;
    }

    /**
     * Helper Function for building the following command :
     * blastp -db pdb_seqres.db -query Homo_sapiens.GRCh38.pep.all.fa -word_size 11 -evalue  1e-60 -num_threads 6 -outfmt 5 -out pdb_seqres.xml
     *
     * @return A List of command arguments for the processbuilder
     */
    private List<String> makeBlastPCommand(String queryFilename, String outFilename, String dbFilename) {
        List<String> list = new ArrayList<String>();
        list.add(ReadConfig.blastp);
        list.add("-db");
        list.add(dbFilename);
        list.add("-query");
        list.add(queryFilename);
        list.add("-word_size");
        list.add(ReadConfig.blastParaWordSize);
        list.add("-evalue");
        list.add(ReadConfig.blastParaEvalue);
        list.add("-num_threads");
        list.add(ReadConfig.blastParaThreads);
        list.add("-outfmt");
        list.add("5");
        list.add("-out");
        list.add(outFilename);
        return list;
    }

    /**
     * generate mysql command
     *
     * @return
     */
    private List<String> makeDBCommand() {
        List<String> list = new ArrayList<String>();
        list.add(ReadConfig.mysql);
        list.add("--max_allowed_packet="+ReadConfig.mysqlMaxAllowedPacket);
        list.add("-u");
        list.add(ReadConfig.username);
        list.add("--password=" + ReadConfig.password);
        list.add(ReadConfig.dbName);
        return list;
    }

    private List<String> makdeRmCommand(String inFilename) {
        List<String> list = new ArrayList<String>();
        list.add("rm");
        list.add("-fr");
        list.add(inFilename);
        return list;
    }
}
