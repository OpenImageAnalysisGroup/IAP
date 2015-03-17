package de.ipk.ag_ba.image.structures;

public enum ImageType {
	GRAY8(8), GRAY16(16), GRAY32(32), COLOR_256(256), COLOR_RGB(24);
	
	int depth;
	
	ImageType(int depth) {
		this.depth = depth;
	}
	
	public int getDepth() {
		return depth;
	}
}
