package iap.blocks.data_structures;

public enum BlockType {
	ACQUISITION, PREPROCESSING, SEGMENTATION, FEATURE_EXTRACTION, POSTPROCESSING, FILTER, UNDEFINED;
	
	public String getName() {
		switch (this) {
			case ACQUISITION:
				return "Aquisition";
			case FEATURE_EXTRACTION:
				return "Feature Extraction";
			case POSTPROCESSING:
				return "Post-processing";
			case PREPROCESSING:
				return "Pre-processing";
			case SEGMENTATION:
				return "Segmentation";
			case FILTER:
				return "Filter";
			default:
				return "[Unknown BlockType]";
		}
	}
	
	public String getColor() {
		switch (this) {
			case ACQUISITION:
				return "#DDFFDD";
			case FEATURE_EXTRACTION:
				return "#DDDDFF";
			case POSTPROCESSING:
				return "#DDFFFF";
			case PREPROCESSING:
				return "#FFFFDD";
			case SEGMENTATION:
				return "#FFDDDD";
			case FILTER:
				return "#FFDDFF";
			case UNDEFINED:
				return "#FF5555";
		}
		return "";
	}
}
