/*************************************************************************************
 * The MultimodalDataHandling Add-on is (c) 2008-2010 Plant Bioinformatics
 * Group,
 * IPK Gatersleben, http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
package de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes;

public class ByteShortIntArray {
	
	private byte[][][] bytearray = null;
	private short[][][] shortarray = null;
	private int[][][] intarray = null;
	private int[][][] intarray2;
	
	public ByteShortIntArray(Object volume) {
		if (volume instanceof byte[][][])
			bytearray = (byte[][][]) volume;
		if (volume instanceof short[][][])
			shortarray = (short[][][]) volume;
		if (volume instanceof int[][][])
			intarray = (int[][][]) volume;
	}
	
	public boolean isByte() {
		return bytearray != null;
	}
	
	public boolean isShort() {
		return shortarray != null;
	}
	
	public boolean isInt() {
		return intarray != null;
	}
	
	public byte getGrayVoxel(int x, int y, int z) {
		return bytearray[x][y][z];
	}
	
	public short getShortVoxel(int x, int y, int z) {
		return shortarray[x][y][z];
	}
	
	public int getColorVoxel(int x, int y, int z) {
		return intarray[x][y][z];
	}
	
	public long getVoxelCount() {
		if (isByte())
			return (long) bytearray.length * (long) bytearray[0].length * bytearray[0][0].length;
		if (isShort())
			return (long) shortarray.length * (long) shortarray[0].length * shortarray[0][0].length;
		if (isInt())
			return (long) intarray.length * (long) intarray[0].length * intarray[0][0].length;
		throw new UnsupportedOperationException("Unknown voxel format");
		
	}
	
	public VolumeInputStream getInputStream() {
		if (isByte())
			return new ByteVolumeInputStream(bytearray);
		else
			if (isShort())
				return new ShortVolumeInputStream(shortarray);
			else
				if (isInt())
					return new IntVolumeInputStream(intarray);
		return null;
	}
	
	public void visitIntArray(IntVolumeVisitor intVolumeVisitor) throws Exception {
		for (int x = 0; x < intarray.length; x++)
			for (int y = 0; y < intarray[x].length; y++)
				for (int z = 0; z < intarray[x][y].length; z++)
					intVolumeVisitor.visit(x, y, z, intarray[x][y][z]);
	}
	
	public int[][][] getIntArray() {
		return intarray;
	}
	
	public void setIntArray(int[][][] intarray) {
		this.intarray = intarray;
	}
}
