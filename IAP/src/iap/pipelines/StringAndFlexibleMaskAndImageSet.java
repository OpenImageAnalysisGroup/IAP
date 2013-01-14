package iap.pipelines;

import de.ipk.ag_ba.image.structures.FlexibleMaskAndImageSet;

public class StringAndFlexibleMaskAndImageSet {
	
	private String settings;
	private FlexibleMaskAndImageSet maskAndImageSet;
	
	public StringAndFlexibleMaskAndImageSet(String settings, FlexibleMaskAndImageSet maskAndImageSet) {
		this.settings = settings;
		this.maskAndImageSet = maskAndImageSet;
	}
	
	public String getSettings() {
		return settings;
	}
	
	public FlexibleMaskAndImageSet getMaskAndImageSet() {
		return maskAndImageSet;
	}
	
	public void setMaskAndImageSet(FlexibleMaskAndImageSet processed) {
		maskAndImageSet = processed;
	}
	
	public void setOptions(String canonicalName) {
		settings = canonicalName;
	}
	
}
