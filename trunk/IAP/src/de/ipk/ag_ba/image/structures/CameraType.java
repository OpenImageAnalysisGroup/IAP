package de.ipk.ag_ba.image.structures;

import java.util.HashSet;

public enum CameraType {
	VIS, FLUO, NIR, IR, UNKNOWN, MULTI;
	
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
			case MULTI:
				s = "multi";
		}
		return s;
	}
	
	public static HashSet<CameraType> getHashSet(CameraType... ct) {
		HashSet<CameraType> r = new HashSet<CameraType>();
		for (CameraType c : ct)
			r.add(c);
		return r;
	}
}
