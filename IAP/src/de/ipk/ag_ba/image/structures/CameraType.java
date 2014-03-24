package de.ipk.ag_ba.image.structures;

public enum CameraType {
	VIS, FLUO, NIR, IR, UNKNOWN;
	
	@Override
	public String toString() {
		String s = "unknown";
		switch (this) {
			case VIS:
				s = "vis";
				break;
			case FLUO:
				s = "fluo";
				break;
			case NIR:
				s = "nir";
				break;
			case IR:
				s = "ir";
				break;
		}
		return s;
	}
}
