package de.ipk.ag_ba.image.operation;

import org.apache.commons.lang3.text.WordUtils;

public enum GLCMTextureFeatures {
	GLCM_CONTRAST_V,
	GLCM_CONTRAST_H,
	GLCM_DISSIMILARITY_V,
	GLCM_DISSIMILARITY_H,
	GLCM_HOMOGENEITY_H,
	GLCM_HOMOGENEITY_V,
	GLCM_ASM_V,
	GLCM_ASM_H,
	GLCM_ENTROPY_V,
	GLCM_ENTROPY_H,
	GLCM_MEAN_V,
	GLCM_MEAN_H,
	GLCM_VARIANCE_V,
	GLCM_VARIANCE_H,
	GLCM_DERIVATION_V,
	GLCM_DERIVATION_H,
	GLCM_CORRELATION_V,
	GLCM_CORRELATION_H;
	
	@Override
	public String toString() {
		return this.name().toLowerCase().replace('_', '.');
	}
	
	public String getNiceName() {
		switch (this) {
			case GLCM_ASM_H:
				return "Angular Second Moment (" + this.getOrientation() + ")";
			case GLCM_ASM_V:
				return "Angular Second Moment (" + this.getOrientation() + ")";
			default:
				return WordUtils.capitalize(this.name().split("_")[1]) + " (" + this.getOrientation() + ")";
		}
	}
	
	private String getOrientation() {
		if (this.name().endsWith("H"))
			return "horizontal";
		else
			return "vertical";
	}
	
	public String getReferenceLink() {
		switch (this) {
			case GLCM_ASM_H:
			case GLCM_ASM_V:
				return "http://www.fp.ucalgary.ca/mhallbey/asm.htm";
			case GLCM_CONTRAST_H:
			case GLCM_CONTRAST_V:
				return "http://www.fp.ucalgary.ca/mhallbey/contrast.htm";
			case GLCM_CORRELATION_H:
			case GLCM_CORRELATION_V:
				return "http://www.fp.ucalgary.ca/mhallbey/correlation.htm";
			case GLCM_DERIVATION_H:
			case GLCM_DERIVATION_V:
				return "http://www.fp.ucalgary.ca/mhallbey/glcm_variance.htm";
			case GLCM_DISSIMILARITY_H:
			case GLCM_DISSIMILARITY_V:
				return "http://www.fp.ucalgary.ca/mhallbey/dissimilarity.htm";
			case GLCM_ENTROPY_H:
			case GLCM_ENTROPY_V:
				return "http://www.fp.ucalgary.ca/mhallbey/entropy.htm";
			case GLCM_HOMOGENEITY_H:
			case GLCM_HOMOGENEITY_V:
				return "http://www.fp.ucalgary.ca/mhallbey/homogeneity.htm";
			case GLCM_MEAN_H:
			case GLCM_MEAN_V:
				return "http://www.fp.ucalgary.ca/mhallbey/glcm_mean.htm";
			case GLCM_VARIANCE_H:
			case GLCM_VARIANCE_V:
				return "http://www.fp.ucalgary.ca/mhallbey/glcm_variance.htm";
		}
		return null;
	}
}