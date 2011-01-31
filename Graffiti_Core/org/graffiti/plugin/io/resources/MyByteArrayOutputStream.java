/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 6, 2010 by Christian Klukas
 */
package org.graffiti.plugin.io.resources;

import java.io.ByteArrayOutputStream;

/**
 * @author klukas
 */
public class MyByteArrayOutputStream extends ByteArrayOutputStream {
	public MyByteArrayOutputStream() {
		super();
	}
	
	public MyByteArrayOutputStream(int i) {
		super(i);
	}
	
	public void writeInt(int rgb) {
		for (byte b : intToByteArray(rgb))
			write(b);
	}
	
	private byte[] intToByteArray(final int integer) {
		int byteNum = (40 - Integer.numberOfLeadingZeros(integer < 0 ? ~integer : integer)) / 8;
		byte[] byteArray = new byte[4];
		
		for (int n = 0; n < byteNum; n++)
			byteArray[3 - n] = (byte) (integer >>> (n * 8));
		
		return (byteArray);
	}
	
	public byte[] getBuff() {
		return buf;
	}
	
	public void setBuf(byte[] buf) {
		
	}
}
