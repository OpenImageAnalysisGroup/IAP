/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.image.structures;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * Holds up to three images (Vis/Fluo/NIR). Makes it more easy to process a
 * snapshot and to relate images of differnt kinds.
 * 
 * @author klukas
 */
public class FlexibleImageSet {
	
	private FlexibleImage vis;
	private FlexibleImage fluo;
	private FlexibleImage nir;
	private ImageData visInfo;
	private ImageData fluoInfo;
	private ImageData nirInfo;
	
	public FlexibleImageSet() {
		// empty
		// use setVis, ...
	}
	
	public FlexibleImageSet(FlexibleImage vis, FlexibleImage fluo, FlexibleImage nir) {
		if (vis != null)
			vis.setType(FlexibleImageType.VIS);
		if (fluo != null)
			fluo.setType(FlexibleImageType.FLUO);
		if (nir != null)
			nir.setType(FlexibleImageType.NIR);
		
		this.vis = vis;
		this.fluo = fluo;
		this.nir = nir;
	}
	
	public FlexibleImageSet(BufferedImage vis, BufferedImage fluo, BufferedImage nir) {
		this.vis = new FlexibleImage(vis, FlexibleImageType.VIS);
		this.fluo = new FlexibleImage(fluo, FlexibleImageType.FLUO);
		this.nir = new FlexibleImage(nir, FlexibleImageType.NIR);
	}
	
	public FlexibleImage getVis() {
		if (vis != null)
			vis.setType(FlexibleImageType.VIS);
		return vis;
	}
	
	public FlexibleImage getFluo() {
		if (fluo != null)
			fluo.setType(FlexibleImageType.FLUO);
		return fluo;
	}
	
	public FlexibleImage getNir() {
		if (nir != null)
			nir.setType(FlexibleImageType.NIR);
		return nir;
	}
	
	/**
	 * @return The sum of the pixel count of the input images.
	 */
	public long getPixelCount() {
		return vis.getWidth() * vis.getHeight() + fluo.getWidth() * fluo.getHeight() + nir.getWidth() * nir.getHeight();
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
				vis != null ? vis.resize(w, h) : null,
				fluo != null ? fluo.resize(w, h) : null,
				nir != null ? nir.resize(w, h) : null);
	}
	
	public void setVis(FlexibleImage vis) {
		if (vis != null)
			vis.setType(FlexibleImageType.VIS);
		this.vis = vis;
	}
	
	public void setFluo(FlexibleImage fluo) {
		if (vis != null)
			vis.setType(FlexibleImageType.FLUO);
		this.fluo = fluo;
	}
	
	public void setNir(FlexibleImage nir) {
		if (vis != null)
			vis.setType(FlexibleImageType.NIR);
		this.nir = nir;
	}
	
	public boolean hasAllThreeImages() {
		return vis != null && fluo != null && nir != null;
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
			case UNKNOWN:
				throw new UnsupportedOperationException("FlexibleImage-Type is not set!");
		}
	}
	
	public FlexibleImageSet copy() {
		return new FlexibleImageSet(vis.copy(), fluo.copy(), nir.copy());
	}
	
	public FlexibleImageSet resize(double scaleVis, double scaleFluo, double scaleNir) {
		FlexibleImage scaledVis = vis != null ? new ImageOperation(vis).resize(scaleVis).getImage() : null;
		FlexibleImage scaledFluo = fluo != null ? new ImageOperation(fluo).resize(scaleFluo).getImage() : null;
		FlexibleImage scaledNir = nir != null ? new ImageOperation(nir).resize(scaleNir).getImage() : null;
		return new FlexibleImageSet(scaledVis, scaledFluo, scaledNir);
	}
	
	public FlexibleImageSet invert() {
		FlexibleImage v = new ImageOperation(vis).invert().getImage();
		FlexibleImage f = new ImageOperation(fluo).invert().getImage();
		FlexibleImage n = new ImageOperation(nir).invert().getImage();
		return new FlexibleImageSet(v, f, n);
	}
	
	public FlexibleImageSet draw(FlexibleImageSet masks, int background) {
		FlexibleImage v = new ImageOperation(vis).draw(masks.getVis(), background);
		FlexibleImage f = new ImageOperation(fluo).draw(masks.getFluo(), background);
		FlexibleImage n = new ImageOperation(nir).draw(masks.getNir(), background);
		return new FlexibleImageSet(v, f, n);
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
		return res;
	}
	
	public void setImageInfo(ImageData visInfo, ImageData fluoInfo, ImageData nirInfo) {
		this.setVisInfo(visInfo);
		this.setFluoInfo(fluoInfo);
		this.setNirInfo(nirInfo);
		
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
	
}
