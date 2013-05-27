/**
 * 
 */
package iap.blocks;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageStack;
import de.ipk.ag_ba.image.structures.CameraType;

/**
 * Uses a lab-based pixel filter(s) for the vis/fluo images.
 * 
 * @author Klukas
 */
public class BlFilterByLAB extends AbstractSnapshotAnalysisBlockFIS {
	
	boolean debug;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
	}
	
	@Override
	protected Image processVISmask() {
		return process("VIS", input().images().vis(), input().masks().vis());
	}
	
	@Override
	protected Image processFLUOmask() {
		return process("FLUO", input().images().fluo(), input().masks().fluo());
	}
	
	private Image process(String optics, Image image, Image mask) {
		if (image == null || mask == null || !getBoolean("process " + optics, optics.equals("VIS")))
			return mask;
		else {
			ImageOperation processedMask = input().masks().vis().io().copy();
			ImageStack fis = debug ? new ImageStack() : null;
			if (fis != null)
				fis.addImage(optics + " start", processedMask.getImage(), null);
			
			if (getBoolean("process " + optics, true)) {
				String pf = "";
				processedMask = processedMask.filterRemoveLAB(
						getInt("min L", 120),
						getInt("max L", 255),
						getInt("min A", 0),
						getInt("max A", 127),
						getInt("min B", 127),
						getInt("max B", 255),
						options.getBackground(),
						getBoolean(pf + "invert", false));
				
				if (fis != null)
					fis.addImage(pf + " result", processedMask.getImage(), null);
			}
			if (debug)
				fis.show(optics + "result");
			return processedMask.getImage();
		}
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
}