package de.ipk.ag_ba.image.operation;

public enum FirstOrderTextureFeatures {
	STD, MEAN, VARIANCE, ASM, CONTRAST, CORRELATION, ENTROPY;
	
	@Override
	public String toString() {
		return this.name().toLowerCase();
	}
}