package de.ipk.ag_ba.image.operation.channels;

import de.ipk.ag_ba.image.structures.ColorSpace;

public enum Channel {
	RGB_R, RGB_G, RGB_B, LAB_L, LAB_A, LAB_B, HSV_H, HSV_S, HSV_V, XYZ_X, XYZ_Y, XYZ_Z;
	
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
			case XYZ_X:
				return "X (XYZ)";
			case XYZ_Y:
				return "Y (XYZ)";
			case XYZ_Z:
				return "Z (XYZ)";
		}
		return null;
	}
	
	public ColorSpace getColorSpace() {
		switch (this) {
			case HSV_H:
				
			case HSV_S:
				
			case HSV_V:
				return ColorSpace.HSV;
			case LAB_A:
				
			case LAB_B:
				
			case LAB_L:
				return ColorSpace.LAB;
			case RGB_B:
				
			case RGB_G:
				
			case RGB_R:
				return ColorSpace.RGB;
			case XYZ_X:
				
			case XYZ_Y:
				
			case XYZ_Z:
				return ColorSpace.XYZ;
		}
		return null;
	}
}
