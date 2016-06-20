package org.cbioportal.pdb_annotation.util;

public final class Constants {

    // Defaults
    public static String makeblastdb = "makeblastdb";
    public static String blastp = "blastp";
    public static String MFOLD = "/usr/local/bin/mfold";
    //point to the Python executable, not Biopython
    public static String python = "python";

    public static String workingdirectory = System.getProperty("user.dir") + System.getProperty("file.separator");

    public static String targets = "";
    public static String parameters = "";
    public static String switches = "";

}
