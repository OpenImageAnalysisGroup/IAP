/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Aug 13, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.rmi_server.analysis;

import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeInputStream;

/**
 * @author klukas
 */
public class VolumeUploadData {

	private final VolumeInputStream inputStream;
	private final long length;

	public VolumeUploadData(VolumeInputStream byteArrayInputStream, long length) {
		this.inputStream = byteArrayInputStream;
		this.length = length;
	}

	public long getLength() {
		return length;
	}

	public VolumeInputStream getStream() {
		inputStream.seekToVoxel(0);
		return inputStream;
	}
}
