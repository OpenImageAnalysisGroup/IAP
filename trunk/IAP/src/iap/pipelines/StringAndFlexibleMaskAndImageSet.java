package iap.pipelines;

import de.ipk.ag_ba.image.structures.MaskAndImageSet;

public class StringAndFlexibleMaskAndImageSet {
	
	private String settings;
	private MaskAndImageSet maskAndImageSet;
	
	public StringAndFlexibleMaskAndImageSet(String settings, MaskAndImageSet maskAndImageSet) {
		this.settings = settings;
		this.maskAndImageSet = maskAndImageSet;
	}
	
	public String getSettings() {
		return settings;
	}
	
	public MaskAndImageSet getMaskAndImageSet() {
		return maskAndImageSet;
	}
	
	public void setMaskAndImageSet(MaskAndImageSet processed) {
		maskAndImageSet = processed;
	}
	
	public void setOptions(String canonicalName) {
		settings = canonicalName;
	}
	
}
