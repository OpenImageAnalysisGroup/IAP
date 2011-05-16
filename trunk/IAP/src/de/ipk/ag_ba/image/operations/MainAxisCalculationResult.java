package de.ipk.ag_ba.image.operations;

import de.ipk.ag_ba.image.structures.FlexibleImage;

public class MainAxisCalculationResult {
	
	private final FlexibleImage imageResult;
	private final DistanceSumAndPixelCount minResult;
	
	public MainAxisCalculationResult(FlexibleImage imageResult, DistanceSumAndPixelCount minResult) {
		this.imageResult = imageResult;
		this.minResult = minResult;
		
	}
	
	public FlexibleImage getImageResult() {
		return imageResult;
	}
	
	public DistanceSumAndPixelCount getMinResult() {
		return minResult;
	}
}
