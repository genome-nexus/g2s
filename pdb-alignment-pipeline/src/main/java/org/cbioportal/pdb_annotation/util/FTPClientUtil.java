package org.cbioportal.pdb_annotation.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;


/**
 * FTP related Utils
 * 
 * @author Juexin Wang
 *
 */
public class FTPClientUtil
{

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


}

