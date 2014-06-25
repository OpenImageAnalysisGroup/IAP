package de.ipk.ag_ba.image.structures;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author klukas
 */
public class ImageInMemory extends ImageData {
	
	private final Image imageData;
	
	public ImageInMemory(SampleInterface parent, Image imageData) {
		super(parent);
		this.imageData = imageData;
	}
	
	public Image getImageData() {
		return imageData;
	}
}
