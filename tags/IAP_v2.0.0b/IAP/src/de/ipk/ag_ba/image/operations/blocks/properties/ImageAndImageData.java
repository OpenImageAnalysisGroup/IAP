package de.ipk.ag_ba.image.operations.blocks.properties;

import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.images.ImageData;

/**
 * @author Christian Klukas
 */
public class ImageAndImageData {
	private final Image image;
	private final ImageData imageData;
	
	public ImageAndImageData(Image image, ImageData imageData) {
		this.image = image;
		this.imageData = imageData;
	}
	
	public Image getImage() {
		return image;
	}
	
	public ImageData getImageData() {
		return imageData;
	}
}
