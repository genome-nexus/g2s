package org.cbioportal.pdb_annotation.web.models.api;

import java.util.*;
import org.cbioportal.pdb_annotation.web.controllers.GenomeAlignmentController;


/**
 *
 * Read application.properties by singleton design pattern
 *
 * @author Juexin Wang
 *
 */
public class ReadConfig {

    private static ReadConfig rcObj;

    public static String gnApiUrl;

    public static boolean isPositiveInteger(String str) {
        return str.matches("\\d+"); // match a number with positive integer.
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?"); // match a number with optional
                                                // '-' and decimal.
    }

    public static boolean isFolder(String str) {
        return str.matches("/.+/"); // match a folder start with / and end with
                                    // /
    }

    // TODO:
    // Check value of application.properties
    public void checkValue(String inStr) {

    }

    private ReadConfig() {

        try {
            Properties prop = new Properties();
            prop.load(GenomeAlignmentController.class.getClassLoader().getResourceAsStream("application.properties"));

            // Set all constants
            ReadConfig.gnApiUrl = prop.getProperty("gn.api.url").trim();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Get and set Methods
     */
    
    
    public static ReadConfig getInstance() {
        if (rcObj == null) {
            rcObj = new ReadConfig();
        }
        return rcObj;
    }

    public static ReadConfig getRcObj() {
        return rcObj;
    }

    public static String getGnApiUrl() {
        return gnApiUrl;
    }

    public static void setGnApiUrl(String gnApiUrl) {
        ReadConfig.gnApiUrl = gnApiUrl;
    }

}
