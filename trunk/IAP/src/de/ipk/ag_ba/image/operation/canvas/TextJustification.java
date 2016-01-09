package de.ipk.ag_ba.image.operation.canvas;

import ij.process.ImageProcessor;

public enum TextJustification {
	LEFT, CENTER, RIGHT;
	
	public int getValue() {
		switch (this) {
			case CENTER:
				return ImageProcessor.CENTER_JUSTIFY;
			case LEFT:
				return ImageProcessor.LEFT_JUSTIFY;
			case RIGHT:
				return ImageProcessor.RIGHT_JUSTIFY;
			default:
				throw new RuntimeException("Unknown justification value");
		}
	}
}
