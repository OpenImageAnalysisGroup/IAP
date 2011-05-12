package de.ipk.ag_ba.server.analysis.image_analysis_tasks;

import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

public class ImageSet {
	private ImageData vis;
	private ImageData fluo;
	private ImageData nir;
	private boolean isSide;
	
	public ImageSet(ImageData vis, ImageData fluo, ImageData nir) {
		this.vis = vis;
		this.fluo = fluo;
		this.nir = nir;
	}
	
	public ImageData getVIS() {
		return vis;
	}
	
	public ImageData getFLUO() {
		return fluo;
	}
	
	public ImageData getNIR() {
		return nir;
	}
	
	public void setVis(ImageData id) {
		vis = id;
	}
	
	public void setFluo(ImageData id) {
		fluo = id;
	}
	
	public void setNir(ImageData id) {
		nir = id;
	}
	
	public boolean hasAllImageTypes() {
		return vis != null && fluo != null && nir != null;
	}
	
	public boolean isSide() {
		return isSide;
	}
	
	public void setSide(boolean isSide) {
		this.isSide = isSide;
	}
}
