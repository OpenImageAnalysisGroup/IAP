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
public class ShortVolumeInputStream extends VolumeInputStream {
	
	protected short[][][] volumeData;
	protected long voxelPos;
	protected long markVoxel = 0;
	protected long voxelCount;
	private int voxelPosX = 0;
	private int voxelPosY = 0;
	private int voxelPosZ = 0;
	private boolean evenByte = false;
	private boolean markEvenByte = false;
	
	public ShortVolumeInputStream(short buf[][][]) {
		this.volumeData = buf;
		this.voxelPos = 0;
		this.voxelCount = buf.length * buf[0].length * buf[0][0].length;
	}
	
	@Override
	public synchronized int read() {
		if (voxelPos >= voxelCount)
			return -1;
		short value = volumeData[voxelPosX][voxelPosY][voxelPosZ];
		byte b = (byte) (evenByte ? value >> 8 & 0xff : value & 0xff);
		
		advance(1);
		int res = b & 0xff;
		if (res < 0)
			System.out.println("ERR");
		return res;
	}
	
	private void advance(long nBytes) {
		for (int n = 0; n < nBytes; n++) {
			if (evenByte) {
				evenByte = false;
			} else {
				evenByte = true;
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
	}
	
	@Override
	public synchronized long skip(long nBytes) {
		long p1 = voxelPos * 2 + (evenByte ? 0 : 1);
		advance(nBytes);
		long p2 = voxelPos * 2 + (evenByte ? 0 : 1);
		return p2 - p1;
	}
	
	@Override
	public synchronized int available() {
		if (voxelCount - voxelPos < Integer.MAX_VALUE) {
			return (int) (voxelCount - voxelPos - 1) * 2 + (evenByte ? 1 : 0);
		} else
			return Integer.MAX_VALUE;
	}
	
	@Override
	public boolean markSupported() {
		return true;
	}
	
	@Override
	public void mark(int readAheadLimit) {
		markVoxel = voxelPos;
		markEvenByte = evenByte;
	}
	
	@Override
	public synchronized void reset() {
		voxelPosX = 0;
		voxelPosY = 0;
		voxelPosZ = 0;
		voxelPos = 0;
		evenByte = false;
		advance(markVoxel * 2 + (markEvenByte ? 0 : 1));
	}
	
	@Override
	public void close() throws IOException {
		volumeData = null;
	}
	
	@Override
	public long getNumberOfBytes() {
		return voxelCount * 2;
	}
	
	@Override
	public void seekToVoxel(long pos) {
		this.voxelPos = pos;
		evenByte = false;
		advance(pos * 2);
	}
	
}
