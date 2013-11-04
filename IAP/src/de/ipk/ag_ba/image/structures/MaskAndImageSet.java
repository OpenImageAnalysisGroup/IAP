package de.ipk.ag_ba.image.structures;

import java.io.File;
import java.io.FileNotFoundException;

import org.SystemOptions;

import de.ipk.ag_ba.image.operation.ImageDisplay;
import de.ipk.ag_ba.image.operation.ImageOperation;

public class MaskAndImageSet {
	private ImageSet imageset;
	private ImageSet maskset;
	
	public MaskAndImageSet(ImageSet images, ImageSet masks) {
		setImages(images);
		setMasks(masks);
	}
	
	public ImageSet images() {
		return imageset;
	}
	
	public ImageSet masks() {
		return maskset;
	}
	
	public void setImages(ImageSet images) {
		this.imageset = images;
	}
	
	public void setMasks(ImageSet masks) {
		this.maskset = masks;
	}
	
	public void print(String title) {
		print(title, SystemOptions.getInstance().getInteger("IAP", "Debug-Overview-Image-Width", 1680));
	}
	
	public void print(String title, int width) {
		Image overview = getOverviewImage(width);
		
		ImageDisplay.show(overview, title);
		
	}
	
	public Image getOverviewImage(int width) {
		ImageSet resizedImages = imageset.equalize();
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
		
		int[][] imgVis = resizedImages.vis() != null ? resizedImages.vis().getAs2A() : null;
		int[][] imgFluo = resizedImages.fluo() != null ? resizedImages.fluo().getAs2A() : null;
		int[][] imgNir = resizedImages.nir() != null ? resizedImages.nir().getAs2A() : null;
		int[][] imgIr = resizedImages.ir() != null ? resizedImages.ir().getAs2A() : null;
		
		ImageOperation io = new ImageOperation(image);
		
		if (imgVis != null)
			io = io.drawAndFillRect(0 * wn, 0, imgVis);
		if (imgFluo != null)
			io = io.drawAndFillRect(1 * wn, 0, imgFluo);
		if (imgNir != null)
			io = io.drawAndFillRect(2 * wn, 0, imgNir);
		if (imgIr != null)
			io = io.drawAndFillRect(3 * wn, 0, imgIr);
		
		if (maskset != null) {
			ImageSet resizedMasks = maskset.equalize();
			
			s1 = (double) wn / resizedMasks.getLargestWidth();
			s2 = (double) hn / resizedMasks.getLargestHeight();
			s = s1 < s2 ? s1 : s2;
			
			resizedMasks = resizedMasks.resize(s, s, s, s);
			
			int[][] imgVisMask = resizedMasks.vis() != null ? resizedMasks.vis().getAs2A() : null;
			int[][] imgFluoMask = resizedMasks.fluo() != null ? resizedMasks.fluo().getAs2A() : null;
			int[][] imgNirMask = resizedMasks.nir() != null ? resizedMasks.nir().getAs2A() : null;
			int[][] imgIrMask = resizedMasks.ir() != null ? resizedMasks.ir().getAs2A() : null;
			
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
	
	public MaskAndImageSet resize(double a, double b, double c, double d) {
		return new MaskAndImageSet(images().resize(a, b, c, d), masks().resize(a, b, c, d));
	}
	
	/**
	 * Save all images in one TIFF stack
	 * 
	 * @throws FileNotFoundException
	 */
	public void save(String fileName) throws FileNotFoundException {
		ImageStack fis = new ImageStack();
		if (imageset.vis() != null)
			fis.addImage("vis", imageset.vis());
		if (maskset.vis() != null)
			fis.addImage("vis mask", maskset.vis());
		if (imageset.fluo() != null)
			fis.addImage("fluo", imageset.fluo());
		if (maskset.fluo() != null)
			fis.addImage("fluo mask", maskset.fluo());
		if (imageset.nir() != null)
			fis.addImage("nir", imageset.nir());
		if (maskset.nir() != null)
			fis.addImage("nir mask", maskset.nir());
		if (imageset.nir() != null)
			fis.addImage("ir", imageset.ir());
		if (maskset.nir() != null)
			fis.addImage("ir mask", maskset.ir());
		
		fis.saveAsLayeredTif(new File(fileName));
	}
	
	@Override
	public String toString() {
		StringBuilder res = new StringBuilder();
		res.append("[");
		if (imageset != null) {
			if (imageset.vis() != null)
				res.append("VIS:1,");
			else
				res.append("VIS:NULL,");
			if (imageset.fluo() != null)
				res.append("FLU:1,");
			else
				res.append("FLU:NULL,");
			if (imageset.nir() != null)
				res.append("NIR:1,");
			else
				res.append("NIR:NULL,");
			if (imageset.ir() != null)
				res.append("IR:1/");
			else
				res.append("IR:NULL/");
		} else
			res.append("NULL/");
		if (maskset != null) {
			if (maskset.vis() != null)
				res.append("VIS-R:1,");
			else
				res.append("VIS-R:NULL,");
			if (maskset.fluo() != null)
				res.append("FLU-R:1,");
			else
				res.append("FLU-R:NULL,");
			if (maskset.nir() != null)
				res.append("NIR:1,");
			else
				res.append("NIR:NULL,");
			if (maskset.ir() != null)
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
		if (imageset != null) {
			if (imageset.vis() != null)
				res++;
			if (imageset.fluo() != null)
				res++;
			if (imageset.nir() != null)
				res++;
			if (imageset.ir() != null)
				res++;
		}
		if (maskset != null) {
			if (maskset.vis() != null)
				res++;
			if (maskset.fluo() != null)
				res++;
			if (maskset.nir() != null)
				res++;
			if (maskset.ir() != null)
				res++;
		}
		return res;
	}
	
	public ImageSetDescription getImageSetDescription() {
		return new ImageSetDescription(this);
	}
	
	public MaskAndImageSet copy() {
		return new MaskAndImageSet(imageset != null ? imageset.copy() : null, maskset != null ? maskset.copy() : null);
	}
}
