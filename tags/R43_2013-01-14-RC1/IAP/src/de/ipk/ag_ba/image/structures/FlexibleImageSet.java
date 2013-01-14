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
public class FlexibleImageSet {
	
	private FlexibleImage vis;
	private FlexibleImage fluo;
	private FlexibleImage nir;
	private FlexibleImage ir;
	private ImageData visInfo;
	private ImageData fluoInfo;
	private ImageData nirInfo;
	private ImageData irInfo;
	
	public FlexibleImageSet() {
		// empty
		// use setVis, ...
	}
	
	public FlexibleImageSet(FlexibleImageSet copyImageInfoFromThisSet) {
		if (copyImageInfoFromThisSet != null) {
			visInfo = copyImageInfoFromThisSet.visInfo;
			fluoInfo = copyImageInfoFromThisSet.fluoInfo;
			nirInfo = copyImageInfoFromThisSet.nirInfo;
			irInfo = copyImageInfoFromThisSet.irInfo;
		}
	}
	
	public FlexibleImageSet(FlexibleImage vis, FlexibleImage fluo, FlexibleImage nir, FlexibleImage ir) {
		if (vis != null)
			vis.setType(FlexibleImageType.VIS);
		if (fluo != null)
			fluo.setType(FlexibleImageType.FLUO);
		if (nir != null)
			nir.setType(FlexibleImageType.NIR);
		if (ir != null)
			ir.setType(FlexibleImageType.IR);
		
		this.vis = vis;
		this.fluo = fluo;
		this.nir = nir;
		this.ir = ir;
	}
	
	public FlexibleImageSet(BufferedImage vis, BufferedImage fluo, BufferedImage nir, BufferedImage ir) {
		this.vis = new FlexibleImage(vis, FlexibleImageType.VIS);
		this.fluo = new FlexibleImage(fluo, FlexibleImageType.FLUO);
		this.nir = new FlexibleImage(nir, FlexibleImageType.NIR);
		this.ir = new FlexibleImage(ir, FlexibleImageType.IR);
	}
	
	public FlexibleImage vis() {
		if (vis != null)
			vis.setType(FlexibleImageType.VIS);
		return vis;
	}
	
	public FlexibleImage fluo() {
		if (fluo != null)
			fluo.setType(FlexibleImageType.FLUO);
		return fluo;
	}
	
	public FlexibleImage nir() {
		if (nir != null)
			nir.setType(FlexibleImageType.NIR);
		return nir;
	}
	
	public FlexibleImage ir() {
		if (ir != null)
			ir.setType(FlexibleImageType.IR);
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
	public FlexibleImageSet equalize() {
		int w = getLargestWidth();
		int h = getLargestHeight();
		// PrintImage.printImage(fluo);
		return new FlexibleImageSet(
				vis != null ? vis.resize(w, h, true) : null,
				fluo != null ? fluo.resize(w, h, true) : null,
				nir != null ? nir.resize(w, h, true) : null,
				ir != null ? ir.resize(w, h, true) : null);
	}
	
	public void setVis(FlexibleImage vis) {
		if (vis != null)
			vis.setType(FlexibleImageType.VIS);
		this.vis = vis;
	}
	
	public void setFluo(FlexibleImage fluo) {
		if (fluo != null)
			fluo.setType(FlexibleImageType.FLUO);
		this.fluo = fluo;
	}
	
	public void setNir(FlexibleImage nir) {
		if (nir != null)
			nir.setType(FlexibleImageType.NIR);
		this.nir = nir;
	}
	
	public void setIr(FlexibleImage ir) {
		if (ir != null)
			ir.setType(FlexibleImageType.NIR);
		this.ir = ir;
	}
	
	public void set(FlexibleImage flexibleImage) {
		if (flexibleImage == null) {
			throw new UnsupportedOperationException("FlexibleImage is NULL");
		}
		switch (flexibleImage.getType()) {
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
	
	public FlexibleImageSet copy() {
		FlexibleImageSet res = new FlexibleImageSet(
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
	
	public FlexibleImageSet resize(double scaleVis, double scaleFluo, double scaleNir, double scaleIr) {
		FlexibleImage scaledVis = vis != null ? new ImageOperation(vis).resize(scaleVis).getImage() : null;
		FlexibleImage scaledFluo = fluo != null ? new ImageOperation(fluo).resize(scaleFluo).getImage() : null;
		FlexibleImage scaledNir = nir != null ? new ImageOperation(nir).resize(scaleNir).getImage() : null;
		FlexibleImage scaledIr = ir != null ? new ImageOperation(ir).resize(scaleIr).getImage() : null;
		return new FlexibleImageSet(scaledVis, scaledFluo, scaledNir, scaledIr);
	}
	
	public FlexibleImageSet invert() {
		FlexibleImage v = new ImageOperation(vis).invert().getImage();
		FlexibleImage f = new ImageOperation(fluo).invert().getImage();
		FlexibleImage n = new ImageOperation(nir).invert().getImage();
		FlexibleImage i = new ImageOperation(ir).invert().getImage();
		return new FlexibleImageSet(v, f, n, i);
	}
	
	public FlexibleImageSet draw(FlexibleImageSet masks, int background) {
		FlexibleImage v = new ImageOperation(vis).draw(masks.vis(), background);
		FlexibleImage f = new ImageOperation(fluo).draw(masks.fluo(), background);
		FlexibleImage n = new ImageOperation(nir).draw(masks.nir(), background);
		FlexibleImage i = new ImageOperation(ir).draw(masks.ir(), background);
		return new FlexibleImageSet(v, f, n, i);
	}
	
	public ArrayList<FlexibleImage> getImages() {
		ArrayList<FlexibleImage> res = new ArrayList<FlexibleImage>();
		if (vis != null) {
			vis.setType(FlexibleImageType.VIS);
			res.add(vis);
		}
		if (fluo != null) {
			fluo.setType(FlexibleImageType.FLUO);
			res.add(fluo);
		}
		if (nir != null) {
			nir.setType(FlexibleImageType.NIR);
			res.add(nir);
		}
		if (ir != null) {
			ir.setType(FlexibleImageType.IR);
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
		FlexibleImageStack fis = new FlexibleImageStack();
		if (vis != null)
			fis.addImage("vis", vis);
		if (fluo != null)
			fis.addImage("fluo", fluo);
		if (nir != null)
			fis.addImage("nir", nir);
		if (ir != null)
			fis.addImage("ir", ir);
		fis.print(title);
	}
	
	public FlexibleImage getImage(FlexibleImageType inp) {
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
	
	public ImageData getImageInfo(FlexibleImageType inp) {
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
}
