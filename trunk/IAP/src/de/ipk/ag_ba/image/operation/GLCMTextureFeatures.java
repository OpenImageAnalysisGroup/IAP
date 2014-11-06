package de.ipk.ag_ba.image.operation;

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
}