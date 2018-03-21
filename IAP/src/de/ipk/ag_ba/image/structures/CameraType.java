package de.ipk.ag_ba.image.structures;

import java.util.ArrayList;
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
	
	public static CameraType fromString(String string) {
		for (CameraType ct : CameraType.values())
			if (ct.toString().equals(string))
				return ct;
		return null;
	}
	
	public String getNiceName() {
		String s = "unknown";
		switch (this) {
			case VIS:
				s = "visible-light";
				break;
			case FLUO:
				s = "fluorescence";
				break;
			case NIR:
				s = "near-infrared";
				break;
			case IR:
				s = "infrared";
				break;
			case MULTI:
				s = "multi camera";
		}
		return s;
	}
	
	public static ArrayList<String> getListOfNames(CameraType[] values) {
		ArrayList<String> result = new ArrayList<String>();
		for (CameraType c : values)
			result.add(c.name());
		return result;
	}
}
