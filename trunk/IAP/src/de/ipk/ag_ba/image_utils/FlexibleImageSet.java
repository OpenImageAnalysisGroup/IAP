/*******************************************************************************
 * Copyright (c) 2010 IPK Gatersleben, Group Image Analysis
 *******************************************************************************/
/*
 * Created on Nov 26, 2010 by Christian Klukas
 */

package de.ipk.ag_ba.image_utils;

import java.awt.image.BufferedImage;

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

	public FlexibleImageSet() {
		// empty
		// use setVis, ...
	}

	public FlexibleImageSet(FlexibleImage vis, FlexibleImage fluo, FlexibleImage nir) {
		this.vis = vis;
		this.fluo = fluo;
		this.nir = nir;
	}

	public FlexibleImageSet(BufferedImage vis, BufferedImage fluo, BufferedImage nir) {
		this.vis = new FlexibleImage(vis);
		this.fluo = new FlexibleImage(fluo);
		this.nir = new FlexibleImage(nir);
	}

	public FlexibleImage getVis() {
		return vis;
	}

	public FlexibleImage getFluo() {
		return fluo;
	}

	public FlexibleImage getNir() {
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
		return new FlexibleImageSet(vis.resize(w, h), fluo.resize(w, h), nir.resize(w, h));
	}

	public void setVis(FlexibleImage vis) {
		this.vis = vis;
	}

	public void setFluo(FlexibleImage fluo) {
		this.fluo = fluo;
	}

	public void setNir(FlexibleImage nir) {
		this.nir = nir;
	}

	public boolean hasAllThreeImages() {
		return vis != null && fluo != null && nir != null;
	}

	public void set(FlexibleImage flexibleImage) {
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
}
