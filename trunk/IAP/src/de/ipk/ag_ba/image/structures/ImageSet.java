/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.image.structures;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * Holds up to four images (Vis/Fluo/NIR/IR). Makes it more easy to process a
 * snapshot and to relate images of different kinds.
 * 
 * @author klukas
 */
public class ImageSet {
	
	private Image vis;
	private Image fluo;
	private Image nir;
	private Image ir;
	private ImageData visInfo;
	private ImageData fluoInfo;
	private ImageData nirInfo;
	private ImageData irInfo;
	private boolean isSideImageSet;
	
	public ImageSet() {
		// empty
		// use setVis, ...
	}
	
	@Override
	public String toString() {
		return (vis != null ? "V:" + vis : "V:n/a") + ";" + (vis != null ? "F:" + fluo : "F:n/a") + ";" + (nir != null ? "N:" + nir : "N:n/a") + ";"
				+ (ir != null ? "I:" + ir : "I:n/a");
	}
	
	public ImageSet(ImageSet copyImageInfoFromThisSet) {
		if (copyImageInfoFromThisSet != null) {
			visInfo = copyImageInfoFromThisSet.visInfo;
			fluoInfo = copyImageInfoFromThisSet.fluoInfo;
			nirInfo = copyImageInfoFromThisSet.nirInfo;
			irInfo = copyImageInfoFromThisSet.irInfo;
		}
	}
	
	public ImageSet(Image vis, Image fluo, Image nir, Image ir) {
		if (vis != null)
			vis.setCameraType(CameraType.VIS);
		if (fluo != null)
			fluo.setCameraType(CameraType.FLUO);
		if (nir != null)
			nir.setCameraType(CameraType.NIR);
		if (ir != null)
			ir.setCameraType(CameraType.IR);
		
		this.vis = vis;
		this.fluo = fluo;
		this.nir = nir;
		this.ir = ir;
	}
	
	public ImageSet(BufferedImage vis, BufferedImage fluo, BufferedImage nir, BufferedImage ir) {
		this.vis = new Image(vis, CameraType.VIS);
		this.fluo = new Image(fluo, CameraType.FLUO);
		this.nir = new Image(nir, CameraType.NIR);
		this.ir = new Image(ir, CameraType.IR);
	}
	
	public Image vis() {
		if (vis != null)
			vis.setCameraType(CameraType.VIS);
		return vis;
	}
	
	public Image fluo() {
		if (fluo != null)
			fluo.setCameraType(CameraType.FLUO);
		return fluo;
	}
	
	public Image nir() {
		if (nir != null)
			nir.setCameraType(CameraType.NIR);
		return nir;
	}
	
	public Image ir() {
		if (ir != null)
			ir.setCameraType(CameraType.IR);
		return ir;
	}
	
	/**
	 * @return The sum of the pixel count of the input images.
	 */
	public long getPixelCount() {
		return vis.getWidth() * vis.getHeight() + fluo.getWidth() * fluo.getHeight() + nir.getWidth() * nir.getHeight() + ir.getWidth() * ir.getHeight();
	}
	
	public int getLargestWidth() {
		int largest = 0;
		if (vis != null)
			largest = vis.getWidth();
		if (fluo != null)
			if (fluo.getWidth() > largest)
				largest = fluo.getWidth();
		if (nir != null)
			if (nir.getWidth() > largest)
				largest = nir.getWidth();
		if (ir != null)
			if (ir.getWidth() > largest)
				largest = ir.getWidth();
		return largest;
	}
	
	public int getLargestHeight() {
		int largest = 0;
		if (vis != null)
			largest = vis.getHeight();
		if (fluo != null)
			if (fluo.getHeight() > largest)
				largest = fluo.getHeight();
		if (nir != null)
			if (nir.getHeight() > largest)
				largest = nir.getHeight();
		if (ir != null)
			if (ir.getHeight() > largest)
				largest = ir.getHeight();
		return largest;
	}
	
	/**
	 * Enlarge the images so that they all share the same size.
	 * 
	 * @return A set of eventually modified (resized) images, all of the same
	 *         size.
	 */
	public ImageSet equalize() {
		int w = getLargestWidth();
		int h = getLargestHeight();
		// PrintImage.printImage(fluo);
		return new ImageSet(
				vis != null ? vis.resize(w, h, true) : null,
				fluo != null ? fluo.resize(w, h, true) : null,
				nir != null ? nir.resize(w, h, true) : null,
				ir != null ? ir.resize(w, h, true) : null);
	}
	
	public void setVis(Image vis) {
		if (vis != null)
			vis.setCameraType(CameraType.VIS);
		this.vis = vis;
	}
	
	public void setFluo(Image fluo) {
		if (fluo != null)
			fluo.setCameraType(CameraType.FLUO);
		this.fluo = fluo;
	}
	
	public void setNir(Image nir) {
		if (nir != null)
			nir.setCameraType(CameraType.NIR);
		this.nir = nir;
	}
	
	public void setIr(Image ir) {
		if (ir != null)
			ir.setCameraType(CameraType.NIR);
		this.ir = ir;
	}
	
	public void set(Image flexibleImage) {
		if (flexibleImage == null) {
			throw new UnsupportedOperationException("FlexibleImage is NULL");
		}
		setImage(flexibleImage.getCameraType(), flexibleImage);
	}
	
	public ImageSet copy() {
		ImageSet res = new ImageSet(
				vis != null ? vis.copy() : null,
				fluo != null ? fluo.copy() : null,
				nir != null ? nir.copy() : null,
				ir != null ? ir.copy() : null);
		res.visInfo = visInfo;
		res.fluoInfo = fluoInfo;
		res.nirInfo = nirInfo;
		res.irInfo = irInfo;
		return res;
	}
	
	public ImageSet resize(double scaleVis, double scaleFluo, double scaleNir, double scaleIr) {
		Image scaledVis = vis != null ? new ImageOperation(vis).resize(scaleVis).getImage() : null;
		Image scaledFluo = fluo != null ? new ImageOperation(fluo).resize(scaleFluo).getImage() : null;
		Image scaledNir = nir != null ? new ImageOperation(nir).resize(scaleNir).getImage() : null;
		Image scaledIr = ir != null ? new ImageOperation(ir).resize(scaleIr).getImage() : null;
		return new ImageSet(scaledVis, scaledFluo, scaledNir, scaledIr);
	}
	
	public ImageSet invert() {
		Image v = new ImageOperation(vis).invert().getImage();
		Image f = new ImageOperation(fluo).invert().getImage();
		Image n = new ImageOperation(nir).invert().getImage();
		Image i = new ImageOperation(ir).invert().getImage();
		return new ImageSet(v, f, n, i);
	}
	
	public ImageSet draw(ImageSet masks, int background) {
		Image v = new ImageOperation(vis).draw(masks.vis(), background);
		Image f = new ImageOperation(fluo).draw(masks.fluo(), background);
		Image n = new ImageOperation(nir).draw(masks.nir(), background);
		Image i = new ImageOperation(ir).draw(masks.ir(), background);
		return new ImageSet(v, f, n, i);
	}
	
	public ArrayList<Image> getImages() {
		ArrayList<Image> res = new ArrayList<Image>();
		if (vis != null) {
			vis.setCameraType(CameraType.VIS);
			res.add(vis);
		}
		if (fluo != null) {
			fluo.setCameraType(CameraType.FLUO);
			res.add(fluo);
		}
		if (nir != null) {
			nir.setCameraType(CameraType.NIR);
			res.add(nir);
		}
		if (ir != null) {
			ir.setCameraType(CameraType.IR);
			res.add(ir);
		}
		return res;
	}
	
	public void setImageInfo(ImageData visInfo, ImageData fluoInfo, ImageData nirInfo, ImageData irInfo) {
		this.setVisInfo(visInfo);
		this.setFluoInfo(fluoInfo);
		this.setNirInfo(nirInfo);
		this.setIrInfo(irInfo);
	}
	
	public ImageData getAnyInfo() {
		if (visInfo != null)
			return visInfo;
		else
			if (fluoInfo != null)
				return fluoInfo;
			else
				if (nirInfo != null)
					return nirInfo;
				else
					if (irInfo != null)
						return irInfo;
					else
						return null;
	}
	
	public void setVisInfo(ImageData visInfo) {
		this.visInfo = visInfo;
	}
	
	public ImageData getVisInfo() {
		return visInfo;
	}
	
	public void setFluoInfo(ImageData fluoInfo) {
		this.fluoInfo = fluoInfo;
	}
	
	public ImageData getFluoInfo() {
		return fluoInfo;
	}
	
	public void setNirInfo(ImageData nirInfo) {
		this.nirInfo = nirInfo;
	}
	
	public ImageData getNirInfo() {
		return nirInfo;
	}
	
	public void setIrInfo(ImageData irInfo) {
		this.irInfo = irInfo;
	}
	
	public ImageData getIrInfo() {
		return irInfo;
	}
	
	public int getSmallestWidth() {
		int smallest = Integer.MAX_VALUE;
		if (vis != null)
			smallest = vis.getWidth();
		if (fluo != null)
			if (fluo.getWidth() < smallest)
				smallest = fluo.getWidth();
		if (nir != null)
			if (nir.getWidth() < smallest)
				smallest = nir.getWidth();
		if (ir != null)
			if (ir.getWidth() < smallest)
				smallest = ir.getWidth();
		if (smallest < Integer.MAX_VALUE)
			return smallest;
		else
			return 0;
	}
	
	public int getSmallestHeight(boolean v, boolean f, boolean n) {
		int smallest = Integer.MAX_VALUE;
		if (vis != null && v)
			smallest = vis.getHeight();
		if (fluo != null && f)
			if (fluo.getWidth() < smallest)
				smallest = fluo.getHeight();
		if (nir != null && n)
			if (nir.getWidth() < smallest)
				smallest = nir.getHeight();
		if (ir != null && n)
			if (ir.getWidth() < smallest)
				smallest = ir.getHeight();
		if (smallest < Integer.MAX_VALUE)
			return smallest;
		else
			return 0;
	}
	
	public void print(String title) {
		ImageStack fis = new ImageStack();
		if (vis != null)
			fis.addImage("vis", vis);
		if (fluo != null)
			fis.addImage("fluo", fluo);
		if (nir != null)
			fis.addImage("nir", nir);
		if (ir != null)
			fis.addImage("ir", ir);
		fis.show(title);
	}
	
	public Image getImage(CameraType inp) {
		switch (inp) {
			case VIS:
				return vis;
			case FLUO:
				return fluo;
			case NIR:
				return nir;
			case IR:
				return ir;
		}
		return null;
	}
	
	public ImageData getImageInfo(CameraType inp) {
		switch (inp) {
			case VIS:
				return visInfo;
			case FLUO:
				return fluoInfo;
			case NIR:
				return nirInfo;
			case IR:
				return irInfo;
		}
		return null;
	}
	
	public void setIsSide(boolean isSide) {
		this.isSideImageSet = isSide;
	}
	
	public boolean isSideImage() {
		return isSideImageSet;
	}
	
	public void setImage(CameraType ct, Image flexibleImage) {
		switch (ct) {
			case VIS:
				setVis(flexibleImage);
				break;
			case FLUO:
				setFluo(flexibleImage);
				break;
			case NIR:
				setNir(flexibleImage);
				break;
			case IR:
				setIr(flexibleImage);
				break;
			case UNKNOWN:
				throw new UnsupportedOperationException("FlexibleImage-Type is not set!");
		}
	}
}
