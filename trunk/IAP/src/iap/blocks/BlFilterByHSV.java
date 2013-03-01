/**
 * 
 */
package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * Uses a HSV-based pixel filter(s) for the vis/fluo/nir images.
 * 
 * @author Klukas
 */
public class BlFilterByHSV extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		return process("VIS", input().images().vis(), input().masks().vis());
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		return process("FLUO", input().images().fluo(), input().masks().fluo());
	}
	
	@Override
	protected FlexibleImage processNIRmask() {
		return process("NIR", input().images().nir(), input().masks().nir());
	}
	
	private FlexibleImage process(String optics, FlexibleImage image, FlexibleImage mask) {
		if (image == null || mask == null || !getBoolean("process " + optics, optics.equals("VIS")))
			return mask;
		else {
			boolean debug = false;
			ImageOperation processedMask = mask.io().show("in mask", debug).copy();
			int HSVfilters = getInt("Number of HSV " + optics + " filters", 1);
			FlexibleImage imageUnChanged = image.copy();
			for (int filter = 1; filter <= HSVfilters; filter++) {
				String pf = optics + " filter " + filter + ": ";
				processedMask = processedMask
						.blur(getDouble(pf + " blur", 1)).show("in mask blurred", debug)
						.filterRemoveHSV(
								getDouble(pf + "min H", (2 / 3d) - (1 / 2d - 1 / 3d)),
								getDouble(pf + "max H", (2 / 3d) + (1 / 2d - 1 / 3d)),
								getDouble(pf + "min S", 0),
								getDouble(pf + "max S", 1),
								getDouble(pf + "min V", 0),
								getDouble(pf + "max V", (200d / 255d))).show(pf + " res", debug);
				processedMask = processedMask.and(mask);
				image = image.io().applyMask(
						processedMask.closing(getInt(pf + "dilate", 2), getInt(pf + "erode", 4)).getImage(),
						options.getBackground()).getImage();
			}
			// blur introduces new pixel areas, so the original mask is applied here, to shrink it down
			// so that the result does not introduce new pixel areas, only less (filter operation)
			processedMask = imageUnChanged.io().applyMask(processedMask.getImage(), options.getBackground());
			return processedMask.getImage();
		}
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		res.add(FlexibleImageType.NIR);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		return getInputTypes();
	}
}