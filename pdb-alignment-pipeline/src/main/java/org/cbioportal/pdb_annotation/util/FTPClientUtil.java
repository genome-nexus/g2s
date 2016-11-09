package org.cbioportal.pdb_annotation.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.log4j.Logger;


/**
 * FTP related Utils
 * 
 * @author Juexin Wang
 *
 */
public class FTPClientUtil
{
    final static Logger log = Logger.getLogger(FTPClientUtil.class);

    /**
     * read FTP file to list
     *
     * @param urlStr
     * @return
     */
    public List<String> readFTPfile2List(String urlStr) {
        List<String> list = new ArrayList<String>();
        try {
            URL url = new URL(urlStr);
            URLConnection con = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                list.add(inputLine);
            }
            in.close();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return list;
    }

    /**
     * read FTP file to String
     * @param urlStr
     * @return
     */
    public String readFTPfile2Str(String urlStr) {
        String str = "";
        try {
            URL url = new URL(urlStr);
            URLConnection con = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                str = str + inputLine + "\n";
            }
            in.close();
        } catch(Exception ex) {
            ex.printStackTrace();
        }
        return str;
    }
    
    /**
     * Download file from ftp://ftp.site.org
     * 
     * @param infileName
     *                  URL of the infileName
     * @param outfileName
     *                  download file to specific location
     * @return  
     *          download success or not
     */
    public boolean downloadFilefromFTP(String infileName, String outfileName){
        log.info("Start download "+outfileName.substring(outfileName.lastIndexOf("/")+1)+" from FTP.");
        boolean success = false;
        //"ftp.uniprot.org"
        String server = infileName.split("ftp://")[1].split("/")[0];
        int port = 21;
        String user = "anonymous";
        String pass = "";
 
        FTPClient ftpClient = new FTPClient();
        try {
 
            ftpClient.connect(server, port);
            ftpClient.login(user, pass);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
 
            // APPROACH #1: using retrieveFile(String, OutputStream)
            //"/pub/databases/uniprot/current_release/knowledgebase/complete/uniprot_sprot.fasta.gz"
            String remoteFile1 = infileName.split(server)[1];
            File downloadFile1 = new File(outfileName);
            OutputStream outputStream1 = new BufferedOutputStream(new FileOutputStream(downloadFile1));
            success = ftpClient.retrieveFile(remoteFile1, outputStream1);
            outputStream1.close();
 
            if (success) {
                log.info(outfileName.substring(outfileName.lastIndexOf("/")+1)+" has been downloaded successfully from FTP.");
            }          
 
            /*
            // APPROACH #2: using InputStream retrieveFileStream(String)
            String remoteFile2 = "/test/song.mp3";
            File downloadFile2 = new File("D:/Downloads/song.mp3");
            OutputStream outputStream2 = new BufferedOutputStream(new FileOutputStream(downloadFile2));
            InputStream inputStream = ftpClient.retrieveFileStream(remoteFile2);
            byte[] bytesArray = new byte[4096];
            int bytesRead = -1;
            while ((bytesRead = inputStream.read(bytesArray)) != -1) {
                outputStream2.write(bytesArray, 0, bytesRead);
            }
 
            success = ftpClient.completePendingCommand();
            if (success) {
                System.out.println("File #2 has been downloaded successfully.");
            }
            outputStream2.close();
            inputStream.close();
            */
 
        } catch (IOException ex) {
            log.error("Error in downloading from FTP: " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }      
        return success;        
    }


}

