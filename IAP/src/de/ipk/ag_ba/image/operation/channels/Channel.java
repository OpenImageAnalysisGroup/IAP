package de.ipk.ag_ba.image.operation.channels;

public enum Channel {
	RGB_R, RGB_G, RGB_B, LAB_L, LAB_A, LAB_B, HSV_H, HSV_S, HSV_V;
	
	@Override
	public String toString() {
		String r = this.name();
		r = r.toLowerCase();
		r = r.replace('_', '.');
		return r;
	}
}
