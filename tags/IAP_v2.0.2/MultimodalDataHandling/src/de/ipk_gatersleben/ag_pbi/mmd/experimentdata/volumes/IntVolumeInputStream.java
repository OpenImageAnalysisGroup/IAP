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
public class IntVolumeInputStream extends VolumeInputStream {
	
	protected int[][][] volumeData;
	protected long voxelPos;
	protected long markVoxel = 0;
	protected long voxelCount;
	private int voxelPosX = 0;
	private int voxelPosY = 0;
	private int voxelPosZ = 0;
	private int bytenum0123 = 0;
	private int markByte0123;
	long delivered = 0;
	
	public IntVolumeInputStream(int buf[][][]) {
		this.volumeData = buf;
		this.voxelPos = 0;
		this.voxelCount = buf.length * buf[0].length * buf[0][0].length;
	}
	
	@Override
	public synchronized int read() {
		if (voxelPos >= voxelCount)
			return -1;
		int value = volumeData[voxelPosX][voxelPosY][voxelPosZ];
		byte b;
		if (bytenum0123 == 0) {
			b = (byte) (value >>> 24);
		} else
			if (bytenum0123 == 1) {
				b = (byte) (value >> 16 & 0xff);
			} else
				if (bytenum0123 == 2)
					b = (byte) (value >> 8 & 0xff);
				else
					b = (byte) (value & 0xff);
		
		advance(1);
		delivered++;
		int res = b & 0xff;
		if (res < 0)
			System.out.println("ERR");
		return res;
	}
	
	private void advance(long nBytes) {
		for (int n = 0; n < nBytes; n++) {
			if (bytenum0123 < 3) {
				bytenum0123++;
			} else {
				bytenum0123 = 0;
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
		long p1 = voxelPos * 4 + bytenum0123;
		advance(nBytes);
		long p2 = voxelPos * 4 + bytenum0123;
		return p2 - p1;
	}
	
	@Override
	public synchronized int available() {
		if (voxelCount - voxelPos < Integer.MAX_VALUE)
			return (int) (voxelCount - voxelPos - 1) * 4 + (4 - bytenum0123);
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
		markByte0123 = bytenum0123;
	}
	
	@Override
	public synchronized void reset() {
		voxelPosX = 0;
		voxelPosY = 0;
		voxelPosZ = 0;
		voxelPos = 0;
		bytenum0123 = 0;
		advance(markVoxel * 4 + markByte0123);
	}
	
	@Override
	public void close() throws IOException {
		volumeData = null;
	}
	
	@Override
	public long getNumberOfBytes() {
		return voxelCount * 4;
	}
	
	@Override
	public void seekToVoxel(long pos) {
		this.voxelPos = 0;
		bytenum0123 = 0;
		advance(pos * 4);
	}
}
