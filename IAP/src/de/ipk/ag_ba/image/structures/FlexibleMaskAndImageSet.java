package de.ipk.ag_ba.image.structures;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.PrintImage;

public class FlexibleMaskAndImageSet {
	private FlexibleImageSet images;
	private FlexibleImageSet masks;
	
	public FlexibleMaskAndImageSet(FlexibleImageSet images, FlexibleImageSet masks) {
		setImages(images);
		setMasks(masks);
	}
	
	public FlexibleImageSet getImages() {
		return images;
	}
	
	public FlexibleImageSet getMasks() {
		return masks;
	}
	
	public void setImages(FlexibleImageSet images) {
		this.images = images;
	}
	
	public void setMasks(FlexibleImageSet masks) {
		this.masks = masks;
	}
	
	public void print(String title) {
		print(title, 1024);
	}
	
	public void print(String title, int width) {
		FlexibleImage overview = getOverviewImage(width);
		
		PrintImage.printImage(overview, title);
		
	}
	
	public FlexibleImage getOverviewImage(int width) {
		FlexibleImageSet resizedImages = images.equalize();
		int targetHeight;
		{
			double b = resizedImages.getLargestWidth();
			double h = resizedImages.getLargestHeight();
			int b_ = width;
			int h_ = (int) (b_ / b * h);
			int wn = b_ / 3;
			int hn = h_ / 2;
			double s1 = (double) wn / resizedImages.getLargestWidth();
			double s2 = (double) hn / resizedImages.getLargestHeight();
			double s = s1 < s2 ? s1 : s2;
			targetHeight = (int) (resizedImages.getLargestHeight() * s) * 2;
		}
		
		int b_ = width;
		int h_ = targetHeight;
		int[][] image = new int[b_][h_];
		int wn = b_ / 3;
		int hn = h_ / 2;
		
		double s1 = (double) wn / resizedImages.getLargestWidth();
		double s2 = (double) hn / resizedImages.getLargestHeight();
		double s = s1 < s2 ? s1 : s2;
		
		resizedImages = resizedImages.resize(s, s, s);
		
		int[][] imgVis = resizedImages.getVis() != null ? resizedImages.getVis().getConvertAs2A() : null;
		int[][] imgFluo = resizedImages.getFluo() != null ? resizedImages.getFluo().getConvertAs2A() : null;
		int[][] imgNir = resizedImages.getNir() != null ? resizedImages.getNir().getConvertAs2A() : null;
		
		ImageOperation io = new ImageOperation(image);
		
		if (imgVis != null)
			io = io.drawAndFillRect(0 * wn, 0, imgVis);
		if (imgFluo != null)
			io = io.drawAndFillRect(1 * wn, 0, imgFluo);
		if (imgNir != null)
			io = io.drawAndFillRect(2 * wn, 0, imgNir);
		
		if (masks != null) {
			FlexibleImageSet resizedMasks = masks.equalize();
			
			s1 = (double) wn / resizedMasks.getLargestWidth();
			s2 = (double) hn / resizedMasks.getLargestHeight();
			s = s1 < s2 ? s1 : s2;
			
			resizedMasks = resizedMasks.resize(s, s, s);
			
			int[][] imgVisMask = resizedMasks.getVis() != null ? resizedMasks.getVis().getConvertAs2A() : null;
			int[][] imgFluoMask = resizedMasks.getFluo() != null ? resizedMasks.getFluo().getConvertAs2A() : null;
			int[][] imgNirMask = resizedMasks.getNir() != null ? resizedMasks.getNir().getConvertAs2A() : null;
			
			if (imgVisMask != null)
				io = io.drawAndFillRect(0 * wn, hn, imgVisMask);
			if (imgFluoMask != null)
				io = io.drawAndFillRect(1 * wn, hn, imgFluoMask);
			if (imgNirMask != null)
				io = io.drawAndFillRect(2 * wn, hn, imgNirMask);
		}
		return io.getImage();
	}
	
	public FlexibleMaskAndImageSet resize(double a, double b, double c) {
		return new FlexibleMaskAndImageSet(getImages().resize(a, b, c), getMasks().resize(a, b, c));
	}
}
