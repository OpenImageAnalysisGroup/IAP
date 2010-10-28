/*************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *
 *************************************************************************/
package de.ipk.ag_ba.postgresql;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.Reader;

/**
 * @author entzian
 *
 */
public class readBinaer {

	public static double arr2double (byte[] arr, int start) {
		int i = 0;
		int len = 8;
		int cnt = 0;
		byte[] tmp = new byte[len];
		for (i = start; i < (start + len); i++) {
			tmp[cnt] = arr[i];
			//System.out.println(java.lang.Byte.toString(arr[i]) + " " + i);
			cnt++;
		}
		long accum = 0;
		i = 0;
		for ( int shiftBy = 0; shiftBy < 64; shiftBy += 8 ) {
			accum |= ( (long)( tmp[i] & 0xff ) ) << shiftBy;
			i++;
		}
		return Double.longBitsToDouble(accum);
	}
	
	
	public static int arr2int (byte[] arr, int start) {
		int low = arr[start] & 0xff;
		int high = arr[start+1] & 0xff;
		return (int)( high << 8 | low );
	}
	
	
	public static float arr2float (byte[] arr, int start) {
		int i = 0;
		int len = 4;
		int cnt = 0;
		byte[] tmp = new byte[len];
		for (i = start; i < (start + len); i++) {
			tmp[cnt] = arr[i];
			cnt++;
		}
		int accum = 0;
		i = 0;
		for ( int shiftBy = 0; shiftBy < 32; shiftBy += 8 ) {
			accum |= ( (long)( tmp[i] & 0xff ) ) << shiftBy;
			i++;
		}
		return Float.intBitsToFloat(accum);
	}

	
	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		
		
		byte[] d10 = new byte[] { 44, 0, 0, 0, 0, 0, 0,0 };
		byte[] d90 = new byte[] {126, (byte)200, 0, 0, 0, 0, 0, 0 };
		
		System.out.println("10:"+arr2double(d10, 0));
		System.out.println("90:"+arr2double(d90, 0));

		System.out.println("10:"+arr2float(d10, 0));
		System.out.println("90:"+arr2float(d90, 0));

		
		if (true)
		return;
		
		// TODO Auto-generated method stub
		 String pfad = "/Users/entzian/LemnaTec/Kamera/binaer/blob22066".replace('/', File.separatorChar);
		 File file = new File(pfad);
		 System.out.println( pfad );
		readFile(file);
		 /*
		String ergebnis[] = readFile(file);
		for(String zeile : ergebnis)
			System.out.println(zeile);
		*/
	}

	public static void readFile(File fileName) throws IOException, ClassNotFoundException
	{
		InputStream in = new FileInputStream(fileName);
		Reader re = new InputStreamReader(in);
		
		int data = re.read();
		while(data != -1){
			char theCar = (char) data;
			System.out.print(theCar);
			data = re.read();
		}
		
		re.close();
		in.close();
		
		
	}
	
	
	
	
}


