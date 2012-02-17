package de.ipk.ag_ba.image.structures;

import java.io.File;
import java.io.FileNotFoundException;

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
			int wn = b_ / 4;
			int hn = h_ / 2;
			double s1 = (double) wn / resizedImages.getLargestWidth();
			double s2 = (double) hn / resizedImages.getLargestHeight();
			double s = s1 < s2 ? s1 : s2;
			targetHeight = (int) (resizedImages.getLargestHeight() * s) * 2;
		}
		
		int b_ = width;
		int h_ = targetHeight;
		int[][] image = new int[b_][h_];
		int wn = b_ / 4;
		int hn = h_ / 2;
		
		double s1 = (double) wn / resizedImages.getLargestWidth();
		double s2 = (double) hn / resizedImages.getLargestHeight();
		double s = s1 < s2 ? s1 : s2;
		
		resizedImages = resizedImages.resize(s, s, s, s);
		
		int[][] imgVis = resizedImages.getVis() != null ? resizedImages.getVis().getAs2A() : null;
		int[][] imgFluo = resizedImages.getFluo() != null ? resizedImages.getFluo().getAs2A() : null;
		int[][] imgNir = resizedImages.getNir() != null ? resizedImages.getNir().getAs2A() : null;
		int[][] imgIr = resizedImages.getIr() != null ? resizedImages.getIr().getAs2A() : null;
		
		ImageOperation io = new ImageOperation(image);
		
		if (imgVis != null)
			io = io.drawAndFillRect(0 * wn, 0, imgVis);
		if (imgFluo != null)
			io = io.drawAndFillRect(1 * wn, 0, imgFluo);
		if (imgNir != null)
			io = io.drawAndFillRect(2 * wn, 0, imgNir);
		if (imgIr != null)
			io = io.drawAndFillRect(3 * wn, 0, imgIr);
		
		if (masks != null) {
			FlexibleImageSet resizedMasks = masks.equalize();
			
			s1 = (double) wn / resizedMasks.getLargestWidth();
			s2 = (double) hn / resizedMasks.getLargestHeight();
			s = s1 < s2 ? s1 : s2;
			
			resizedMasks = resizedMasks.resize(s, s, s, s);
			
			int[][] imgVisMask = resizedMasks.getVis() != null ? resizedMasks.getVis().getAs2A() : null;
			int[][] imgFluoMask = resizedMasks.getFluo() != null ? resizedMasks.getFluo().getAs2A() : null;
			int[][] imgNirMask = resizedMasks.getNir() != null ? resizedMasks.getNir().getAs2A() : null;
			int[][] imgIrMask = resizedMasks.getIr() != null ? resizedMasks.getIr().getAs2A() : null;
			
			if (imgVisMask != null)
				io = io.drawAndFillRect(0 * wn, hn, imgVisMask);
			if (imgFluoMask != null)
				io = io.drawAndFillRect(1 * wn, hn, imgFluoMask);
			if (imgNirMask != null)
				io = io.drawAndFillRect(2 * wn, hn, imgNirMask);
			if (imgIrMask != null)
				io = io.drawAndFillRect(3 * wn, hn, imgIrMask);
		}
		return io.getImage();
	}
	
	public FlexibleMaskAndImageSet resize(double a, double b, double c, double d) {
		return new FlexibleMaskAndImageSet(getImages().resize(a, b, c, d), getMasks().resize(a, b, c, d));
	}
	
	/**
	 * Save all images in one TIFF stack
	 * 
	 * @throws FileNotFoundException
	 */
	public void save(String fileName) throws FileNotFoundException {
		FlexibleImageStack fis = new FlexibleImageStack();
		if (images.getVis() != null)
			fis.addImage("vis", images.getVis());
		if (masks.getVis() != null)
			fis.addImage("vis mask", masks.getVis());
		if (images.getFluo() != null)
			fis.addImage("fluo", images.getFluo());
		if (masks.getFluo() != null)
			fis.addImage("fluo mask", masks.getFluo());
		if (images.getNir() != null)
			fis.addImage("nir", images.getNir());
		if (masks.getNir() != null)
			fis.addImage("nir mask", masks.getNir());
		if (images.getNir() != null)
			fis.addImage("ir", images.getIr());
		if (masks.getNir() != null)
			fis.addImage("ir mask", masks.getIr());
		
		fis.saveAsLayeredTif(new File(fileName));
	}
	
	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append("[");
		if (images != null) {
			if (images.getVis() != null)
				res.append("VIS:1,");
			else
				res.append("VIS:NULL,");
			if (images.getFluo() != null)
				res.append("FLU:1,");
			else
				res.append("FLU:NULL,");
			if (images.getNir() != null)
				res.append("NIR:1,");
			else
				res.append("NIR:NULL,");
			if (images.getIr() != null)
				res.append("IR:1/");
			else
				res.append("IR:NULL/");
		} else
			res.append("NULL/");
		if (masks != null) {
			if (masks.getVis() != null)
				res.append("VIS-R:1,");
			else
				res.append("VIS-R:NULL,");
			if (masks.getFluo() != null)
				res.append("FLU-R:1,");
			else
				res.append("FLU-R:NULL,");
			if (masks.getNir() != null)
				res.append("NIR:1,");
			else
				res.append("NIR:NULL,");
			if (masks.getIr() != null)
				res.append("IR:1");
			else
				res.append("IR:NULL");
		} else
			res.append("NULL");
		res.append("]");
		return res.toString();
	}
	
	public int getImageCount() {
		int res = 0;
		if (images != null) {
			if (images.getVis() != null)
				res++;
			if (images.getFluo() != null)
				res++;
			if (images.getNir() != null)
				res++;
			if (images.getIr() != null)
				res++;
		}
		if (masks != null) {
			if (masks.getVis() != null)
				res++;
			if (masks.getFluo() != null)
				res++;
			if (masks.getNir() != null)
				res++;
			if (masks.getIr() != null)
				res++;
		}
		return res;
	}
}
