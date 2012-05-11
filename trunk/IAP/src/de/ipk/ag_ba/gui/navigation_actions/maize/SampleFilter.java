package de.ipk.ag_ba.gui.navigation_actions.maize;

import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.Sample3D;

public interface SampleFilter {
	
	boolean isValidSample(Sample3D sd3);
	
}
