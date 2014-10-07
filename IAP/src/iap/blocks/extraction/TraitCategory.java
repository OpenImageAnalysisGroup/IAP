package iap.blocks.extraction;

public enum TraitCategory {
	GEOMETRY, INTENSITY, OPTICS, DERIVED, ORGAN_GEOMETRY, ORGAN_INTENSITY;
	
	@Override
	public String toString() {
		switch (this) {
			case GEOMETRY:
				return "geometry";
			case INTENSITY:
				return "intensity";
			case ORGAN_GEOMETRY:
				return "geometry.organ";
			case ORGAN_INTENSITY:
				return "intensity.organ";
			case DERIVED:
				return "derived";
			case OPTICS:
				return "optics";
			default:
				return "";
		}
	}
}
