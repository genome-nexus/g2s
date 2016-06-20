package org.cbioportal.pdb_annotation.util;
import java.io.*;

public class TextFile{

	/**
	 * Read in a given text file to a string
	 * @param	file	file path of the desire input file
	 * @return			the contents of the specified file
	 */
	public static String read(String file) throws IOException {

		BufferedReader reader = new BufferedReader( new FileReader(file));
		String line  = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");
		while( ( line = reader.readLine() ) != null ) {
			stringBuilder.append( line );
			stringBuilder.append( ls );
		}
		reader.close();
		return stringBuilder.toString();
	}

	/**
	 * Given a string, filename and directory, this function will write the string to file
	 * 
	 * @param directory		The directory of the given file
	 * @param fileName		The name of the given file
	 * @param fileContents	The contents of the given file
	 * @throws IOException
	 */
	public static void write(String directory,String fileName, String fileContents) throws IOException{
		BufferedWriter out = new BufferedWriter(new FileWriter(directory+fileName));
		out.write(fileContents);
		out.close();
	}

	/**
	 * @param filePath		Given the path of the file (directory+filename), it will be deleted.
	 * @throws IllegalArgumentException
	 */
	public static void delete(String filePath) throws IllegalArgumentException{

		File ff = new File(filePath);
		boolean success = ff.delete();
		if (!success) {
			System.err.println("Failure to delete File, Check if file path is correct and if file is referenced");
			throw new IllegalArgumentException("Delete: deletion failed");
		}

	}

	/**
	 * A function that returns all the lines in a file in an array of type string
	 * 
	 * @param filePath		The filePath of the desired text file (directory+name)
	 * @return				Returns array of type string with lines as elements
	 * @throws IOException
	 */
	public static String[] getLinesAsArray(String filePath) throws IOException{
		String contents = read(filePath);
		String[] lines = contents.split("\n");
		return lines;
	}

	/**
	 * Simple splitter - Simply takes a string and splits into chunks of specified size
	 * 
	 * @param text
	 * @param size
	 * @return
	 */
	public static String[] splitEqually(String text, int size){

		String[] ret = new String[(text.length() + size - 1) / size];

		int ii= 0;
		for (int start = 0; start < text.length(); start += size, ii++) {
			ret[ii]= (text.substring(start, Math.min(text.length(), start + size)));
		}
		return ret;
	}


}
