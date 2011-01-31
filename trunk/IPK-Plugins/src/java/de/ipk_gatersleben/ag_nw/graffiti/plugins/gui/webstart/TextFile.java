/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
// : com:bruceeckel:util:TextFile.java
// Static functions for reading and writing text files as
// a single string, and treating a file as an ArrayList.
// {Clean: test.txt test2.txt}
// From 'Thinking in Java, 3rd ed.' (c) Bruce Eckel 2002
// www.BruceEckel.com. See copyright notice in CopyRight.txt.
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.database.dbe.ProgressManager;

public class TextFile extends ArrayList<String> {
	private static final long serialVersionUID = 1L;
	
	/**
	 * Tools to read and write files as single strings:
	 * 
	 * @param fileURL
	 * @return
	 * @throws IOException
	 */
	public static String read(URL fileURL) throws IOException {
		StringBuffer sb = new StringBuffer();
		BufferedReader in = new BufferedReader(new InputStreamReader(fileURL.openStream()));
		String s;
		while ((s = in.readLine()) != null) {
			sb.append(s);
			sb.append("\n");
		}
		in.close();
		return sb.toString();
	}
	
	public static String read(InputStream stream, int maxLines) throws IOException {
		StringBuffer sb = new StringBuffer();
		BufferedReader in = new BufferedReader(new InputStreamReader(stream));
		String s;
		long line = 0;
		while ((s = in.readLine()) != null) {
			sb.append(s);
			sb.append("\n");
			line++;
			if (maxLines > 0 && line > maxLines)
				break;
		}
		in.close();
		return sb.toString();
	}
	
	public static String read(Reader reader) throws IOException {
		StringBuffer sb = new StringBuffer();
		BufferedReader in = new BufferedReader(reader);
		String s;
		while ((s = in.readLine()) != null) {
			sb.append(s);
			sb.append("\n");
		}
		in.close();
		return sb.toString();
	}
	
	public static String read(String fileName) throws IOException {
		StringBuffer sb = new StringBuffer();
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		String s;
		while ((s = in.readLine()) != null) {
			sb.append(s);
			sb.append("\n");
		}
		in.close();
		return sb.toString();
	}
	
	public static String read(File f) throws IOException {
		StringBuffer sb = new StringBuffer();
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
		String s;
		while ((s = in.readLine()) != null) {
			sb.append(s);
			sb.append("\n");
		}
		in.close();
		return sb.toString();
	}
	
	public TextFile() {
		
	}
	
	public TextFile(URL fileURL) throws IOException {
		super(Arrays.asList(read(fileURL).split("\n")));
	}
	
	public TextFile(Reader reader) throws IOException {
		super(Arrays.asList(read(reader).split("\n")));
	}
	
	public TextFile(InputStream reader, int maxLines) throws IOException {
		super(Arrays.asList(read(reader, maxLines).split("\n")));
	}
	
	public TextFile(String fileName) throws IOException {
		super(Arrays.asList(read(fileName).split("\n")));
	}
	
	public TextFile(URL fileURL, boolean printFile) throws IOException {
		this(fileURL);
		if (printFile) {
			for (String s : this) {
				System.out.println(fileURL.toExternalForm() + ": " + s);
			}
		}
	}
	
	public void write(String fileName) throws IOException {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
							fileName)));
		for (int i = 0; i < size(); i++)
			out.println(get(i));
		out.close();
	}
	
	public void write(File file) throws IOException {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
		for (int i = 0; i < size(); i++)
			out.println(get(i));
		out.close();
	}
	
	public static void write(String fileName, String text) throws IOException {
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
							fileName)));
		out.print(text);
		out.close();
	}
	
	public static void writeE(String fileName, String text, String encoding) throws IOException {
		Writer out = new BufferedWriter(new OutputStreamWriter(
							new FileOutputStream(fileName), encoding));
		out.write(text);
		out.close();
	}
	
	public void writeE(String fileName, String encoding) throws IOException {
		Writer out = new BufferedWriter(new OutputStreamWriter(
							new FileOutputStream(fileName), encoding));
		for (int i = 0; i < size(); i++)
			out.write(get(i) + System.getProperty("line.separator"));
		out.close();
	}
	
	public void writeE(File file, String encoding) throws IOException {
		Writer out = new BufferedWriter(new OutputStreamWriter(
							new FileOutputStream(file), encoding));
		for (int i = 0; i < size(); i++)
			out.write(get(i) + System.getProperty("line.separator"));
		out.close();
	}
	
	public static void write(String fileName, String text,
						ProgressManager pm) throws IOException {
		pm.setStatus(83, "Create Cache (Writer)");
		PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
							fileName)));
		long sz = text.length() / 1024;
		pm.setStatus(86, "Create Cache (Printing ~" + sz + "KB)");
		out.print(text);
		pm.setStatus(89, "Create Cache (Finishing)");
		out.close();
	}
} // /:~
