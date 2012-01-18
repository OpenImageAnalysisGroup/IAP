package de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes;

import java.io.InputStream;

public abstract class VolumeInputStream extends InputStream {
	
	public abstract long getNumberOfBytes();
	
	public abstract void seekToVoxel(long pos);
	
}
