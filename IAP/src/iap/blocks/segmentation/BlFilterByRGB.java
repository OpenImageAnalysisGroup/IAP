/**
 * 
 */
package iap.blocks.segmentation;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

/**
 * Uses a RGB-based pixel filter(s) for the vis/fluo/nir images. (Always changes the image.)
 * 
 * @author Klukas
 */
public class BlFilterByRGB extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected Image processVISmask() {
		return process("VIS", input().images().vis(), input().masks().vis());
	}
	
	@Override
	protected Image processFLUOmask() {
		return process("FLUO", input().images().fluo(), input().masks().fluo());
	}
	
	@Override
	protected Image processNIRmask() {
		return process("NIR", input().images().nir(), input().masks().nir());
	}
	
	@Override
	protected Image processIRmask() {
		return process("IR", input().images().ir(), input().masks().ir());
	}
	
	private Image process(String optics, Image image, Image mask) {
		if (image == null || mask == null || !getBoolean("process " + optics, optics.equals("VIS")))
			return mask;
		else {
			boolean debug = false;
			
			String pf = optics + " filter " + 1 + " ";
			
			ImageOperation filteredContent = mask.io().filterRemoveRGB(
					getDouble(pf + "min R", 0d),
					getDouble(pf + "max R", 255d),
					getDouble(pf + "min G", 0d),
					getDouble(pf + "max G", 255d),
					getDouble(pf + "min B", 0d),
					getDouble(pf + "max B", 255d)).show(pf + " res", debug);
			
			int dilate = getInt(pf + "mask dilate 1", 0);
			int erode = getInt(pf + "mask erode 2", 0);
			int dilate2 = getInt(pf + "mask dilate 3", 0);
			
			if (dilate > 0)
				filteredContent = filteredContent.bm().dilate(dilate).io();
			if (erode > 0)
				filteredContent = filteredContent.bm().erode(erode).io();
			if (dilate2 > 0)
				filteredContent = filteredContent.bm().dilate(dilate2).io();
			
			return image.io().applyMask(filteredContent.getImage(), optionsAndResults.getBackground()).getImage();
		}
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
		return "Color Segmentation (RGB)";
	}
	
	@Override
	public String getDescription() {
		return "Uses a RGB-based pixel filter(s) for the images.";
	}
}