/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 6, 2010 by Christian Klukas
 */
package org.graffiti.plugin.io.resources;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.zip.CRC32;

/**
 * @author klukas
 */
public class MyByteArrayInputStream extends ByteArrayInputStream {
	
	public MyByteArrayInputStream(byte[] buf, int validLen) {
		super(buf, 0, validLen);
	}
	
	public MyByteArrayInputStream() {
		super(new byte[] {});
	}
	
	public MyByteArrayInputStream(byte[] buf) {
		this(buf, buf.length);
	}
	
	public byte[] getBuff() {
		return buf;
	}
	
	public byte[] getBuffTrimmed() {
		return Arrays.copyOfRange(buf, 0, count);
	}
	
	public int getCount() {
		return count;
	}
	
	public long getCRC32() {
		CRC32 crc = new CRC32();
		crc.update(getBuff(), 0, getCount());
		return crc.getValue();
	}
	
	public MyByteArrayInputStream getNewStream() {
		return new MyByteArrayInputStream(buf, count);
	}
}
