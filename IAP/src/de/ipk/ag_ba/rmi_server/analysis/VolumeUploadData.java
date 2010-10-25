/*******************************************************************************
 * 
 *    Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 * 
 *******************************************************************************/
/*
 * Created on Aug 13, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.rmi_server.analysis;

import java.io.ByteArrayInputStream;

import org.graffiti.plugin.io.resources.MyByteArrayInputStream;


/**
 * @author klukas
 * 
 */
public class VolumeUploadData {

	private final MyByteArrayInputStream byteArrayInputStream;
	private final long length;

	public VolumeUploadData(MyByteArrayInputStream byteArrayInputStream, long length) {
		this.byteArrayInputStream = byteArrayInputStream;
		this.length = length;
	}

	public long getLength() {
		return length;
	}

	public ByteArrayInputStream getStream() {
		return new MyByteArrayInputStream(byteArrayInputStream.getBuff());
	}
}
