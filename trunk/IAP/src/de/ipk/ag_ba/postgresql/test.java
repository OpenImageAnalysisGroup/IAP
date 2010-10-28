/*************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *
 *************************************************************************/
package de.ipk.ag_ba.postgresql;

import java.io.*;
/**
 * @author entzian
 *
 */
public class test {

	/**
	 * @param args
	 */

	public static void main(String[] args) {

	//create file object
	File file = new File("C://FileIO//ReadString.txt");

	int ch;
	
	StringBuffer strContent = new StringBuffer("");
	
	FileInputStream fin = null;
	
	 
	try
	{

	/*
	#
	  * Create new FileInputStream object. Constructor of FileInputStream throws
	#
	  * FileNotFoundException if the agrument File does not exist.
	#
	  */
	
	 
	
	fin = new FileInputStream(file);
	
	 
	
	/*
	#
	  * To read bytes from stream use,
	#
	  * int read() method of FileInputStream class.
	#
	  *
	#
	  * This method reads a byte from stream. This method returns next byte of data
	#
	  * from file or -1 if the end of the file is reached.
	#
	  *
	#
	  * Read method throws IOException in case of any IO errors.
	#
	  */
	
	 
	
	while( (ch = fin.read()) != -1)
	
	strContent.append((char)ch);
	
	 
	
	/*
	#
	  * To close the FileInputStream, use
	#
	  * void close() method of FileInputStream class.
	#
	  *
	#
	  * close method also throws IOException.
	#
	  */
	
	fin.close();
	
	 
	
	}
	
	catch(FileNotFoundException e)
	
	{
	
	System.out.println("File " + file.getAbsolutePath() +
	
	" could not be found on filesystem");
	
	}
	
	catch(IOException ioe)
	
	{
	
	System.out.println("Exception while reading the file" + ioe);
	
	}
	
	 
	
	System.out.println("File contents :");
	
	System.out.println(strContent);
	
	 
	
	 
	
	/*
	#
	  * Please note that, FileInputStream SHOULD NOT BE USED to read
	#
	  * character data file.
	#
	  * It is meant for reading binary data such as an image file.
	#
	  *
	#
	  * To read character data, FileReader should be used.
	#
	  */
	
	}
	
	}
	
	
}
