/*******************************************************************************
 * Copyright (c) 2010 Image Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Sep 6, 2010 by Christian Klukas
 */
package org.graffiti.plugin.io.resources;

import java.io.ByteArrayInputStream;

/**
 * @author klukas
 */
public class MyByteArrayInputStream extends ByteArrayInputStream {
	
	public MyByteArrayInputStream(byte[] buf) {
		super(buf);
	}
	
	public MyByteArrayInputStream() {
		super(new byte[] {});
	}
	
	public byte[] getBuff() {
		return buf;
	}
}
