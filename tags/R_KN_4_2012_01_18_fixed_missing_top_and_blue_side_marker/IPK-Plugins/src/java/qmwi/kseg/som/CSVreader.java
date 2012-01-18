/*******************************************************************************
 * Copyright (c) 2001 Christian Klukas
 *******************************************************************************/
package qmwi.kseg.som;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class CSVreader {
	
	public static DataSet readFile(String fileName) {
		
		DataSet mainData;
		mainData = new DataSet();
		
		String currentLine;
		
		System.out.println("Datei wird eingelesen...");
		
		try {
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			mainData.setGroupDescription(reader.readLine());
			
			while ((currentLine = reader.readLine()) != null) {
				currentLine.replace(',', '.');
				mainData.addEntry(currentLine);
			}
		} catch (FileNotFoundException e) {
			System.err.println("File not found: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("I/O Error: " + e.getMessage());
			return null;
		}
		return mainData;
	} // readFile
} // class ReadData
