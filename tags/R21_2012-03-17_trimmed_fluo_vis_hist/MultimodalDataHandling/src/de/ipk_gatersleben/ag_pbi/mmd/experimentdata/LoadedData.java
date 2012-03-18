package de.ipk_gatersleben.ag_pbi.mmd.experimentdata;

import java.io.InputStream;

public interface LoadedData extends BinaryMeasurement {
	public InputStream getInputStream();
	
	public InputStream getInputStreamLabelField();
}