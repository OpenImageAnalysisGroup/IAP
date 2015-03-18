package iap.blocks.extraction;

/**
 * @author klukas
 */
public enum TraitCategory {
	GEOMETRY, INTENSITY, OPTICS, DERIVED, ORGAN_GEOMETRY, ORGAN_INTENSITY, TEXTURE, ORGAN_TEXTURE;
	
	@Override
	public String toString() {
		switch (this) {
			case GEOMETRY:
				return "geometry";
			case INTENSITY:
				return "intensity";
			case ORGAN_GEOMETRY:
				return "geometry";
			case ORGAN_INTENSITY:
				return "intensity";
			case DERIVED:
				return "derived";
			case OPTICS:
				return "optics";
			case TEXTURE:
				return "texture";
			case ORGAN_TEXTURE:
				return "texture";
			default:
				return "";
		}
	}
	
	public static TraitCategory fromString(String string) {
		for (TraitCategory tc : TraitCategory.values())
			if (tc.toString().equals(string)) {
				return tc;
			}
		return null;
	}
	
	public String getNiceString() {
		switch (this) {
			case GEOMETRY:
				return "geometry";
			case INTENSITY:
				return "color-related";
			case ORGAN_GEOMETRY:
				return "geometry";
			case ORGAN_INTENSITY:
				return "intensity";
			case DERIVED:
				return "derived";
			case OPTICS:
				return "optics";
			case TEXTURE:
				return "texture";
			case ORGAN_TEXTURE:
				return "texture";
			default:
				return "";
		}
	}
}
