package de.ipk.ag_ba.image.operation;

import org.apache.commons.lang3.text.WordUtils;

public enum FirstOrderTextureFeatures {
	STD, MEAN, VARIANCE, ASM, CONTRAST, CORRELATION, ENTROPY;
	
	@Override
	public String toString() {
		return this.name().toLowerCase();
	}
	
	public String getNiceName() {
		switch (this) {
			case ASM:
				return "Angular Second Moment";
			case STD:
				return "Standard Deviation";
			default:
				return WordUtils.capitalize(this.name());
		}
	}
	
	public String getReferenceLink() {
		switch (this) {
			case ASM:
				return "http://www.fp.ucalgary.ca/mhallbey/asm.htm";
			case CONTRAST:
				break;
			case CORRELATION:
				break;
			case ENTROPY:
				break;
			case MEAN:
				break;
			case STD:
				break;
			case VARIANCE:
				break;
			default:
				break;
		
		}
		return null;
	}
}