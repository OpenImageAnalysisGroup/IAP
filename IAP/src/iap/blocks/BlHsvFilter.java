/**
 * 
 */
package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageStack;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

/**
 * Uses a HSV-based pixel filter(s) for the vis/fluo/nir images.
 * 
 * @author Klukas
 */
public class BlHsvFilter extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
	}
	
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
			return input().masks().vis();
		else {
			ImageOperation visMask = input().masks().vis().io().copy();
			FlexibleImageStack fis = debug ? new FlexibleImageStack() : null;
			if (fis != null)
				fis.addImage("start " + optics, visMask.getImage(), null);
			
			int HSVfilters = getInt("Number of HSV " + optics + " filters", 1);
			for (int filter = 1; filter <= HSVfilters; filter++) {
				String pf = optics + " filter " + filter + ": ";
				visMask = visMask
						.blur(getDouble(pf + " blur", 1))
						.filterRemoveHSV(
								getDouble(pf + "min H", (2 / 3d) - (1 / 2d - 1 / 3d)),
								getDouble(pf + "max H", (2 / 3d) + (1 / 2d - 1 / 3d)),
								getDouble(pf + "min S", 0),
								getDouble(pf + "max S", 1),
								getDouble(pf + "min V", 0),
								getDouble(pf + "max V", (200d / 255d)));
				visMask = input().images().vis().io().copy().applyMask(
						visMask.closing(getInt(pf + "dilate", 2), getInt(pf + "erode", 4)).getImage(),
						options.getBackground());
				
				if (fis != null)
					fis.addImage(pf + " result", visMask.getImage(), null);
			}
			
			if (debug)
				fis.print(optics + " HSV filter result");
			
			return visMask.getImage();
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