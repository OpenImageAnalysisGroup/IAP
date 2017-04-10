package de.ipk.ag_ba.commands.control;

import java.awt.image.BufferedImage;
import java.util.TreeMap;

public class ImageInfo {
	
	private TreeMap<Integer, BufferedImage> images;
	private TreeMap<Integer, String> imageSubstances;
	private TreeMap<Integer, String> imageBarcode;
	private TreeMap<Integer, Double> imageRotation;
	private boolean oneIsReadyForCapture;
	
	public ImageInfo(TreeMap<Integer, BufferedImage> images,
			TreeMap<Integer, String> imageSubstances,
			TreeMap<Integer, String> imageBarcode,
			TreeMap<Integer, Double> imageRotation, boolean oneIsReadyForCapture) {
		this.oneIsReadyForCapture = oneIsReadyForCapture;
		this.setImageRotation(imageRotation);
		this.setImages(images);
		this.setImageSubstances(imageSubstances);
		this.setImageBarcode(imageBarcode);
	}
	
	public TreeMap<Integer, BufferedImage> getImages() {
		return images;
	}
	
	private void setImages(TreeMap<Integer, BufferedImage> images) {
		this.images = images;
	}
	
	public TreeMap<Integer, String> getImageSubstances() {
		return imageSubstances;
	}
	
	private void setImageSubstances(TreeMap<Integer, String> imageSubstances) {
		this.imageSubstances = imageSubstances;
	}
	
	public TreeMap<Integer, String> getImageBarcode() {
		return imageBarcode;
	}
	
	private void setImageBarcode(TreeMap<Integer, String> imageBarcode) {
		this.imageBarcode = imageBarcode;
	}
	
	public TreeMap<Integer, Double> getImageRotation() {
		return imageRotation;
	}
	
	private void setImageRotation(TreeMap<Integer, Double> imageRotation) {
		this.imageRotation = imageRotation;
	}
	
	public boolean isAnyCameraConfiguredForCaptureAndNotLiveView() {
		return oneIsReadyForCapture;
	}
	
}
