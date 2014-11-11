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
	
	public String getNiceName() {
		switch (this) {
			case HSV_H:
				return "Hue (HSV)";
			case HSV_S:
				return "Saturation (HSV)";
			case HSV_V:
				return "Brightness (HSV)";
			case LAB_A:
				return "Green-Red-Parameter a* (L*a*b*)";
			case LAB_B:
				return "Blue-Yellow-Parameter b* (L*a*b*)";
			case LAB_L:
				return "Ligthness-Parameter L* (L*a*b*)";
			case RGB_B:
				return "Blue (RGB)";
			case RGB_G:
				return "Green (RGB)";
			case RGB_R:
				return "Red (RGB)";
		}
		return null;
	}
}
