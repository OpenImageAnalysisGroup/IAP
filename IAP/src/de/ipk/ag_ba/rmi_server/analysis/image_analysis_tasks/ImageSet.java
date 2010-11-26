package de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks;

import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.ImageData;

public class ImageSet {
	private ImageData vis;
	private ImageData fluo;
	private ImageData nir;

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
		if (vis != null)
			System.err.println("Warning: Overriding Visible Image in ImageSet");
		vis = id;
	}

	public void setFluo(ImageData id) {
		if (fluo != null)
			System.err.println("Warning: Overriding Fluo Image in ImageSet");
		fluo = id;
	}

	public void setNir(ImageData id) {
		if (nir != null)
			System.err.println("Warning: Overriding NIR Image in ImageSet");
		nir = id;
	}
}
