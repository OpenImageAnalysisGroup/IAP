/*******************************************************************************
 * Copyright (c) 2001 Christian Klukas
 *******************************************************************************/
package qmwi.kseg.som.diagram;

/**
 * Insert the type's description here.
 * Creation date: (14.12.2001 13:54:34)
 * 
 * @author:
 */
@SuppressWarnings("unchecked")
public class Aus extends java.io.OutputStream {
	public static java.io.BufferedWriter bw = null;
	public java.lang.String string;
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 13:57:31)
	 */
	public Aus() {
		
		super();
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:02:39)
	 */
	public static void a() {
		
		Aus a = new Aus();
		
		Throwable thro = new Throwable();
		
		java.io.PrintStream p = new java.io.PrintStream(a);
		
		thro.printStackTrace(p);
		
		String[] split = a.splitString(a.string, "\n");
		
		String startingMethod = split[3];
		
		startingMethod = startingMethod.substring(1, startingMethod.length() - 1);
		
		output("", "", startingMethod);
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:02:39)
	 */
	public static void a(Object o) {
		
		Aus a = new Aus();
		
		Throwable thro = new Throwable();
		
		java.io.PrintStream p = new java.io.PrintStream(a);
		
		thro.printStackTrace(p);
		
		String[] split = a.splitString(a.string, "\n");
		
		String startingMethod = split[3];
		
		startingMethod = startingMethod.substring(1, startingMethod.length() - 1);
		
		if (o != null) {
			
			output(String.valueOf(o), "", startingMethod);
			
		} else {
			
			output("null", "", startingMethod);
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:02:39)
	 */
	public static void a(String text, double[][] o) {
		
		output("double Feld: " + text, "          ", getStartingMethod());
		
		output("L�nge der 1.Dimension: " + String.valueOf(o.length), "          ", getStartingMethod());
		
		output("--------------------------------------------------", "", getStartingMethod());
		output("--------------------------------------------------", "", getStartingMethod());
		
		for (int i = 0; i < o.length; i++) {
			
			output("L�nge der 1-" + (i + 1) + ". Dimension: " + String.valueOf(o[i].length), "          ", getStartingMethod());
			
			for (int j = 0; j < o[i].length; j++) {
				
				output("Dimension 1-" + (i + 1) + "   Wert " + (j + 1) + ":   ", String.valueOf(o[i][j]), getStartingMethod());
				
			}
			output("--------------------------------------------------", "", getStartingMethod());
			
		}
		
		output("--------------------------------------------------", "", getStartingMethod());
		output("++++++++++++++++++++++++++++++++++++++++++++++++++", "", getStartingMethod());
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:02:39)
	 */
	public static void a(String text, double[] o) {
		
		output("********************************************************", "", getStartingMethod());
		
		output("double Feld: ", text, getStartingMethod());
		
		output("L�nge des Feldes: ", String.valueOf(o.length), getStartingMethod());
		
		for (int i = 0; i < o.length; i++) {
			
			output(i + ". Wert :   ", String.valueOf(o[i]), getStartingMethod());
			
		}
		
		output("********************************************************", "", getStartingMethod());
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:02:39)
	 */
	public static void a(String text, String[] o) {
		
		output("double Feld: ", text, getStartingMethod());
		
		output("L�nge des Feldes: ", String.valueOf(o.length), getStartingMethod());
		
		for (int i = 0; i < o.length; i++) {
			
			output(i + ". Wert :   ", String.valueOf(o[i]), getStartingMethod());
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:02:39)
	 */
	public static void a(String text, char o) {
		
		Aus a = new Aus();
		
		Throwable thro = new Throwable();
		
		java.io.PrintStream p = new java.io.PrintStream(a);
		
		thro.printStackTrace(p);
		
		String[] split = a.splitString(a.string, "\n");
		
		String startingMethod = split[3];
		
		startingMethod = startingMethod.substring(1, startingMethod.length() - 1);
		
		output(text, String.valueOf(o), startingMethod);
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:02:39)
	 */
	public static void a(String text, double o) {
		
		Aus a = new Aus();
		
		Throwable thro = new Throwable();
		
		java.io.PrintStream p = new java.io.PrintStream(a);
		
		thro.printStackTrace(p);
		
		String[] split = a.splitString(a.string, "\n");
		
		String startingMethod = split[3];
		
		startingMethod = startingMethod.substring(1, startingMethod.length() - 1);
		
		output(text, String.valueOf(o), startingMethod);
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:02:39)
	 */
	public static void a(String text, float o) {
		
		Aus a = new Aus();
		
		Throwable thro = new Throwable();
		
		java.io.PrintStream p = new java.io.PrintStream(a);
		
		thro.printStackTrace(p);
		
		String[] split = a.splitString(a.string, "\n");
		
		String startingMethod = split[3];
		
		startingMethod = startingMethod.substring(1, startingMethod.length() - 1);
		
		output(text, String.valueOf(o), startingMethod);
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:02:39)
	 */
	public static void a(String text, int o) {
		
		Aus a = new Aus();
		
		Throwable thro = new Throwable();
		
		java.io.PrintStream p = new java.io.PrintStream(a);
		
		thro.printStackTrace(p);
		
		String[] split = a.splitString(a.string, "\n");
		
		String startingMethod = split[3];
		
		startingMethod = startingMethod.substring(1, startingMethod.length() - 1);
		
		output(text, String.valueOf(o), startingMethod);
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:02:39)
	 */
	public static void a(String text, Object o) {
		
		Aus a = new Aus();
		
		Throwable thro = new Throwable();
		
		java.io.PrintStream p = new java.io.PrintStream(a);
		
		thro.printStackTrace(p);
		
		String[] split = a.splitString(a.string, "\n");
		
		String startingMethod = split[3];
		
		startingMethod = startingMethod.substring(1, startingMethod.length() - 1);
		
		if (o != null) {
			
			output(text, o.toString(), startingMethod);
			
		} else {
			
			output(text, "null", startingMethod);
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:02:39)
	 */
	public static void a(String text, java.util.Vector o) {
		
		output("Vector: " + text, "          ", getStartingMethod());
		
		output("L�nge des Vectors: " + String.valueOf(o.size()), "          ", getStartingMethod());
		
		output("--------------------------------------------------", "", getStartingMethod());
		output("--------------------------------------------------", "", getStartingMethod());
		
		for (int i = 0; i < o.size(); i++) {
			
			output("Element " + (i + 1) + ":   ", o.elementAt(i).toString(), getStartingMethod());
			
		}
		
		output("--------------------------------------------------", "", getStartingMethod());
		output("++++++++++++++++++++++++++++++++++++++++++++++++++", "", getStartingMethod());
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:02:39)
	 */
	public static void a(String text, boolean o) {
		
		Aus a = new Aus();
		
		Throwable thro = new Throwable();
		
		java.io.PrintStream p = new java.io.PrintStream(a);
		
		thro.printStackTrace(p);
		
		String[] split = a.splitString(a.string, "\n");
		
		String startingMethod = split[3];
		
		startingMethod = startingMethod.substring(1, startingMethod.length() - 1);
		
		output(text, String.valueOf(o), startingMethod);
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:02:39)
	 */
	public static void ac() {
		
		Aus a = new Aus();
		
		Throwable thro = new Throwable();
		
		java.io.PrintStream p = new java.io.PrintStream(a);
		
		thro.printStackTrace(p);
		
		String[] split = a.splitString(a.string, "\n");
		
		String startingMethod = split[3];
		
		startingMethod = startingMethod.substring(1, startingMethod.length() - 1);
		
		outputc("", "", startingMethod);
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:02:39)
	 */
	public static void ac(Object o) {
		
		Aus a = new Aus();
		
		Throwable thro = new Throwable();
		
		java.io.PrintStream p = new java.io.PrintStream(a);
		
		thro.printStackTrace(p);
		
		String[] split = a.splitString(a.string, "\n");
		
		String startingMethod = split[3];
		
		startingMethod = startingMethod.substring(1, startingMethod.length() - 1);
		
		if (o != null) {
			
			outputc(String.valueOf(o), "", startingMethod);
			
		} else {
			
			outputc("null", "", startingMethod);
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:02:39)
	 */
	public static void ac(String text, double[][] o) {
		
		outputc("double Feld: " + text, "          ", getStartingMethod());
		
		outputc("L�nge der 1.Dimension: " + String.valueOf(o.length), "          ", getStartingMethod());
		
		outputc("--------------------------------------------------", "", getStartingMethod());
		outputc("--------------------------------------------------", "", getStartingMethod());
		
		for (int i = 0; i < o.length; i++) {
			
			outputc("L�nge der 1-" + (i + 1) + ". Dimension: " + String.valueOf(o[i].length), "          ", getStartingMethod());
			
			for (int j = 0; j < o[i].length; j++) {
				
				outputc("Dimension 1-" + (i + 1) + "   Wert " + (j + 1) + ":   ", String.valueOf(o[i][j]), getStartingMethod());
				
			}
			outputc("--------------------------------------------------", "", getStartingMethod());
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:02:39)
	 */
	public static void ac(String text, char o) {
		
		Aus a = new Aus();
		
		Throwable thro = new Throwable();
		
		java.io.PrintStream p = new java.io.PrintStream(a);
		
		thro.printStackTrace(p);
		
		String[] split = a.splitString(a.string, "\n");
		
		String startingMethod = split[3];
		
		startingMethod = startingMethod.substring(1, startingMethod.length() - 1);
		
		outputc(text, String.valueOf(o), startingMethod);
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:02:39)
	 */
	public static void ac(String text, double o) {
		
		Aus a = new Aus();
		
		Throwable thro = new Throwable();
		
		java.io.PrintStream p = new java.io.PrintStream(a);
		
		thro.printStackTrace(p);
		
		String[] split = a.splitString(a.string, "\n");
		
		String startingMethod = split[3];
		
		startingMethod = startingMethod.substring(1, startingMethod.length() - 1);
		
		outputc(text, String.valueOf(o), startingMethod);
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:02:39)
	 */
	public static void ac(String text, float o) {
		
		Aus a = new Aus();
		
		Throwable thro = new Throwable();
		
		java.io.PrintStream p = new java.io.PrintStream(a);
		
		thro.printStackTrace(p);
		
		String[] split = a.splitString(a.string, "\n");
		
		String startingMethod = split[3];
		
		startingMethod = startingMethod.substring(1, startingMethod.length() - 1);
		
		outputc(text, String.valueOf(o), startingMethod);
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:02:39)
	 */
	public static void ac(String text, int o) {
		
		Aus a = new Aus();
		
		Throwable thro = new Throwable();
		
		java.io.PrintStream p = new java.io.PrintStream(a);
		
		thro.printStackTrace(p);
		
		String[] split = a.splitString(a.string, "\n");
		
		String startingMethod = split[3];
		
		startingMethod = startingMethod.substring(1, startingMethod.length() - 1);
		
		outputc(text, String.valueOf(o), startingMethod);
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:02:39)
	 */
	public static void ac(String text, Object o) {
		
		Aus a = new Aus();
		
		Throwable thro = new Throwable();
		
		java.io.PrintStream p = new java.io.PrintStream(a);
		
		thro.printStackTrace(p);
		
		String[] split = a.splitString(a.string, "\n");
		
		String startingMethod = split[3];
		
		startingMethod = startingMethod.substring(1, startingMethod.length() - 1);
		
		if (o != null) {
			
			outputc(text, o.toString(), startingMethod);
			
		} else {
			
			outputc(text, "null", startingMethod);
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:02:39)
	 */
	public static void ac(String text, boolean o) {
		
		Aus a = new Aus();
		
		Throwable thro = new Throwable();
		
		java.io.PrintStream p = new java.io.PrintStream(a);
		
		thro.printStackTrace(p);
		
		String[] split = a.splitString(a.string, "\n");
		
		String startingMethod = split[3];
		
		startingMethod = startingMethod.substring(1, startingMethod.length() - 1);
		
		outputc(text, String.valueOf(o), startingMethod);
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (18.12.2001 16:10:15)
	 */
	public static void closeWriter() {
		
		try {
			
			if (bw != null)
				bw.close();
			
			bw = null;
			
		}

		catch (java.io.IOException e) {
			System.out.println("kein Zugriff auf Datei");
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (18.12.2001 15:40:36)
	 * 
	 * @param bw
	 *           java.io.BufferedWriter
	 * @param str
	 *           java.lang.String
	 */
	public static void flushFile() {
		
		if (bw != null) {
			
			try {
				
				bw.flush();
				
			}

			catch (java.io.IOException e) {
				System.out.println("kein Zugriff auf Datei");
			}
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (12.01.02 17:40:06)
	 * 
	 * @return java.lang.String
	 */
	public static String getStartingMethod() {
		
		Aus a = new Aus();
		
		Throwable thro = new Throwable();
		
		java.io.PrintStream p = new java.io.PrintStream(a);
		
		thro.printStackTrace(p);
		
		String[] split = a.splitString(a.string, "\n");
		
		String startingMethod = split[4];
		
		startingMethod = startingMethod.substring(1, startingMethod.length() - 1);
		
		return startingMethod;
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:02:39)
	 */
	public static String m() {
		
		Aus a = new Aus();
		
		Throwable thro = new Throwable();
		
		java.io.PrintStream p = new java.io.PrintStream(a);
		
		thro.printStackTrace(p);
		
		String[] split = a.splitString(a.string, "\n");
		
		String startingMethod = split[3];
		
		startingMethod = startingMethod.substring(1, startingMethod.length() - 1);
		
		return startingMethod;
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (19.12.2001 20:10:21)
	 * 
	 * @param str
	 *           java.lang.String
	 */
	public static void output(String text, String value, String startingMethod) {
		
		// System.out.println(text+"	:	"+value+"			//	"+startingMethod);
		
		writeln(text + "	:	|" + value + "|			//	" + startingMethod);
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (19.12.2001 20:10:21)
	 * 
	 * @param str
	 *           java.lang.String
	 */
	public static void outputc(String text, String value, String startingMethod) {
		
		System.out.println(text + "	:	|" + value + "|			//	" + startingMethod);
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (18.12.2001 16:07:32)
	 */
	public static void setWriter() {
		
		if (bw == null) {
			
			try {
				
				bw = new java.io.BufferedWriter(new java.io.FileWriter("d:\\javalog.txt"));
				
			}

			catch (java.io.IOException e) {
				System.out.println("kein Zugriff auf Datei");
			}
			
		}
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:33:25)
	 */
	public String[] splitString(String str, String split) {
		
		java.util.Vector vec = new java.util.Vector();
		
		int lastIndex = 0;
		
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == split.charAt(0)) {
				vec.addElement(str.substring(lastIndex, i));
				lastIndex = i + 1;
			}
		}
		
		vec.addElement(str.substring(lastIndex, str.length()));
		
		String[] stringSplitted = new String[vec.size()];
		
		for (int i = 0; i < vec.size(); i++) {
			
			stringSplitted[i] = (String) vec.elementAt(i);
			
		}
		
		return stringSplitted;
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (14.12.2001 14:26:12)
	 * 
	 * @param i
	 *           int
	 */
	public void write(int i) {
		
		char[] c = { (char) i };
		
		String str = new String(c);
		
		string = string + str;
		
	}
	
	/**
	 * Insert the method's description here.
	 * Creation date: (18.12.2001 15:40:36)
	 * 
	 * @param bw
	 *           java.io.BufferedWriter
	 * @param str
	 *           java.lang.String
	 */
	public static void writeln(String str) {
		
		setWriter();
		
		try {
			
			bw.write(str);
			
			bw.newLine();
			
			bw.flush();
			
		}

		catch (java.io.IOException e) {
			System.out.println("kein Zugriff auf Datei");
		}
		
	}
}
