/**
 * 
 */
package iap.blocks.segmentation;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Clears the background by comparison of foreground and background.
 * Additionally the border around the masks is cleared (width 2 pixels).
 * 
 * @author pape, klukas
 */
public class BlRemoveBackground extends AbstractBlock {
	
	@Override
	protected Image processMask(Image mask) {
		boolean debug = debugValues;
		debug = false;
		CameraType ct = mask.getCameraType();
		if (input().images().getImage(ct) != null) {
			Image image = input().images().getImage(ct).show("inp", debug);
			image = image.io().histogramEqualisation(true).getImage().show("img_he", debug);
			mask = mask.io().histogramEqualisation(true).getImage().show("mask_he", debug);
			Image diff_image = mask.io().diff(image).getImage().show("diff", debug);
			Image thresh_image = diff_image.io().thresholdImageJ("yen", false).getImage().show("thresh");
			// return image.io()
			// .applyMask(thresh_image)
			// .getImage();
			return null;
		} else
			return null;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Compare Images and Reference Images";
	}
	
	@Override
	public String getDescription() {
		return " Clears the background by comparison of foreground and background.";
	}
}
