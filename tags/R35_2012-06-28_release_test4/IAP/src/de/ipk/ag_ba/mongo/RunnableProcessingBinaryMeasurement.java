package de.ipk.ag_ba.mongo;

import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.BinaryMeasurement;

public interface RunnableProcessingBinaryMeasurement extends Runnable {
	public void setBinaryMeasurement(BinaryMeasurement bm);
}
