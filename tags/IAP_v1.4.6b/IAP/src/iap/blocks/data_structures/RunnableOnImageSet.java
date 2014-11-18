package iap.blocks.data_structures;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author klukas
 */
public interface RunnableOnImageSet {
	public Image postProcessImage(Image image);
	
	public Image postProcessMask(Image mask);
	
	/**
	 * @return Desired image source type (vis/Fluo/nir/ir).
	 */
	public CameraType getConfig();
}
