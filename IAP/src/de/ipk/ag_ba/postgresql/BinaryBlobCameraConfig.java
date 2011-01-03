/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Oct 22, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.postgresql;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author klukas
 */
public class BinaryBlobCameraConfig {
	public static void main(String[] args) {
		byte[] b = new byte[] { 0, 0, 0, 0, 0, 0, 22, 40 };
		byte[] b2 = new byte[] { 40, 8, 60, 0, 0, 0, 0, 10 };
		for (double d = 0; d < 20; d++) {
			try {
				ByteArrayOutputStream bytestream = new ByteArrayOutputStream();
				DataOutputStream datastream = new DataOutputStream(bytestream);
				datastream.writeDouble(d);
				datastream.flush();
				byte[] bytes = bytestream.toByteArray();
				System.out.print(d + " = " + toHexString(bytes) + " = " + arr2double(reverse(bytes), 0));
				System.out.println();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("b=" + arr2double(b, 0));
		System.out.println("b2=" + arr2double(b2, 0));
		System.out.println("bb=" + arr2double(b2, 0));
	}
	
	public static byte[] reverse(byte[] bytes) {
		byte[] res = new byte[bytes.length];
		int j = bytes.length - 1;
		for (int i = 0; i < bytes.length; i++)
			res[j--] = bytes[i];
		return res;
	}
	
	public static String toHexString(byte bytes[]) {
		StringBuffer retString = new StringBuffer();
		for (int i = bytes.length - 1; i >= 0; i--) {
			retString.append(Integer.toHexString(0x0100 + (bytes[i] & 0x00FF)).substring(1) + " ");
		}
		return retString.toString();
	}
	
	public static double arr2double(byte[] arr, int start) {
		int i = 0;
		int len = 8;
		int cnt = 0;
		byte[] tmp = new byte[len];
		for (i = start; i < (start + len); i++) {
			tmp[cnt] = arr[i];
			// System.out.println(java.lang.Byte.toString(arr[i]) + " " + i);
			cnt++;
		}
		long accum = 0;
		i = 0;
		for (int shiftBy = 0; shiftBy < 64; shiftBy += 8) {
			accum |= ((long) (tmp[i] & 0xff)) << shiftBy;
			i++;
		}
		return Double.longBitsToDouble(accum);
	}
}
