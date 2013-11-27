/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 22, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes;

import java.io.IOException;

/**
 * @author klukas
 */
public class ByteVolumeInputStream extends VolumeInputStream {
	
	protected byte[][][] volumeData;
	protected long voxelPos;
	protected long markVoxel = 0;
	protected long voxelCount;
	private int voxelPosX = 0;
	private int voxelPosY = 0;
	private int voxelPosZ = 0;
	
	public ByteVolumeInputStream(byte buf[][][]) {
		this.volumeData = buf;
		this.voxelPos = 0;
		this.voxelCount = buf.length * buf[0].length * buf[0][0].length;
	}
	
	@Override
	public synchronized int read() {
		if (voxelPos >= voxelCount)
			return -1;
		int res = volumeData[voxelPosX][voxelPosY][voxelPosZ] & 0xff;
		advance(1);
		if (res < 0)
			System.out.println("ERR");
		return res;
	}
	
	private void advance(long nBytes) {
		for (int n = 0; n < nBytes; n++) {
			voxelPos++;
			if (voxelPosX < volumeData.length - 1)
				voxelPosX++;
			else {
				voxelPosX = 0;
				if (voxelPosY < volumeData[0].length - 1)
					voxelPosY++;
				else {
					voxelPosX = 0;
					voxelPosY = 0;
					voxelPosZ++;
				}
			}
		}
	}
	
	@Override
	public synchronized long skip(long nBytes) {
		long p1 = voxelPos;
		advance(nBytes);
		return voxelPos - p1;
	}
	
	@Override
	public synchronized int available() {
		if (voxelCount - voxelPos < Integer.MAX_VALUE)
			return (int) (voxelCount - voxelPos);
		else
			return Integer.MAX_VALUE;
	}
	
	@Override
	public boolean markSupported() {
		return true;
	}
	
	@Override
	public void mark(int readAheadLimit) {
		markVoxel = voxelPos;
	}
	
	@Override
	public synchronized void reset() {
		voxelPosX = 0;
		voxelPosY = 0;
		voxelPosZ = 0;
		voxelPos = 0;
		advance(markVoxel);
	}
	
	@Override
	public void close() throws IOException {
		volumeData = null;
	}
	
	@Override
	public long getNumberOfBytes() {
		return voxelCount;
	}
	
	@Override
	public void seekToVoxel(long pos) {
		this.voxelPos = 0;
		advance(pos);
	}
	
}
