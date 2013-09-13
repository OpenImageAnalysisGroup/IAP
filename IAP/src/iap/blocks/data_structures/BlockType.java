package iap.blocks.data_structures;

public enum BlockType {
	ACQUISITION, PREPROCESSING, SEGMENTATION, FEATURE_EXTRACTION, POSTPROCESSING;
	
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
			default:
				return "[Unknown BlockType]";
		}
	}
}
