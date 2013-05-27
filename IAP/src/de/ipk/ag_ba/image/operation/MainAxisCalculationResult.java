package de.ipk.ag_ba.image.operation;

import org.Vector2d;

import de.ipk.ag_ba.image.structures.Image;

public class MainAxisCalculationResult {
	
	private final Image imageResult;
	private final DistanceSumAndPixelCount minResult;
	private Vector2d centroid;
	
	public MainAxisCalculationResult(Image imageResult, DistanceSumAndPixelCount minResult, Vector2d centroid2) {
		this.imageResult = imageResult;
		this.minResult = minResult;
		this.centroid = centroid2;
	}
	
	public Image getImageResult() {
		return imageResult;
	}
	
	public DistanceSumAndPixelCount getMinResult() {
		return minResult;
	}
	
	public void setCentroid(Vector2d centroid) {
		this.centroid = centroid;
	}
	
	public Vector2d getCentroid() {
		return centroid;
	}
}
