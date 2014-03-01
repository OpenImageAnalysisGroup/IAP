/**
 * 
 */
package iap.blocks.segmentation;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageStack;

/**
 * Uses a HSV-based pixel filter(s) for the vis/fluo/nir images.
 * 
 * @author Klukas
 */
public class BlFilterByHSV extends AbstractSnapshotAnalysisBlock {
	
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
	
	private Image process(String optics, Image image, Image mask) {
		if (image == null || mask == null || !getBoolean("process " + optics, optics.equals("VIS")))
			return mask;
		else {
			boolean debug = false;
			ImageOperation processedMask = mask.io().show("in mask", debug).copy();
			int HSVfilters = getInt("Number of HSV " + optics + " filters", 1);
			Image imageUnChanged = image.copy();
			ImageStack st = null;
			if (getBoolean("Debug Mask Manipulation", false))
				st = new ImageStack();
			for (int filter = 1; filter <= HSVfilters; filter++) {
				String pf = optics + " filter " + filter + " ";
				ImageOperation blurred = processedMask.blur(getDouble(pf + " blur", 1)).show("in mask blurred", debug);
				boolean manip = getBoolean(pf + "manipulate mask", false);
				if (!manip) {
					processedMask = blurred.filterRemoveHSV(
							getDouble(pf + "min H", (2 / 3d) - (1 / 2d - 1 / 3d)),
							getDouble(pf + "max H", (2 / 3d) + (1 / 2d - 1 / 3d)),
							getDouble(pf + "min S", 0),
							getDouble(pf + "max S", 1),
							getDouble(pf + "min V", 0),
							getDouble(pf + "max V", (200d / 255d))).show(pf + " res", debug);
				} else {
					ImageOperation filteredContent = blurred.filterRemainHSV(
							getDouble(pf + "min H", (2 / 3d) - (1 / 2d - 1 / 3d)),
							getDouble(pf + "max H", (2 / 3d) + (1 / 2d - 1 / 3d)),
							getDouble(pf + "min S", 0),
							getDouble(pf + "max S", 1),
							getDouble(pf + "min V", 0),
							getDouble(pf + "max V", (200d / 255d))).show(pf + " res", false);
					int dilate = getInt(pf + "mask dilate 1", 0);
					int erode = getInt(pf + "mask erode 2", 0);
					int dilate2 = getInt(pf + "mask dilate 3", 0);
					if (st != null)
						st.addImage("mask " + filter, filteredContent.getImage());
					if (dilate > 0)
						filteredContent = filteredContent.bm().dilate(dilate).io();
					if (erode > 0)
						filteredContent = filteredContent.bm().erode(erode).io();
					if (dilate2 > 0)
						filteredContent = filteredContent.bm().dilate(dilate2).io();
					if (st != null)
						st.addImage("mask " + filter + ", modified", filteredContent.getImage());
					processedMask.show(pf + " processed mask", getBoolean("Debug Mask Manipulation", false));
					mask.show(pf + " mask", getBoolean("Debug Mask Manipulation", false));
					processedMask = processedMask.xor(mask).show(pf + " xor res", getBoolean("Debug Mask Manipulation", false));
					mask = processedMask.getImage();
				}
				processedMask = processedMask.and(mask);
				image = image.io().applyMask(
						processedMask.closing(getInt(pf + "dilate", 2), getInt(pf + "erode", 4)).getImage(),
						optionsAndResults.getBackground()).getImage();
			}
			if (st != null && st.size() > 0)
				st.show("Debug mask");
			// blur introduces new pixel areas, so the original mask is applied here, to shrink it down
			// so that the result does not introduce new pixel areas, only less (filter operation)
			processedMask = imageUnChanged.io().applyMask(processedMask.getImage(), optionsAndResults.getBackground());
			return processedMask.getImage();
		}
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
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
		return "Color Segmentation (HSV)";
	}
	
	@Override
	public String getDescription() {
		return "Uses a HSV-based pixel filter(s) for the vis/fluo/nir images.";
	}
}