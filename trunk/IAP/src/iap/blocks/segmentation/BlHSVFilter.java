/**
 * 
 */
package iap.blocks.segmentation;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageStack;
import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

/**
 * Uses a hsb(v)-based pixel filter(s) for the vis/fluo images (possible to repeat n-times).
 * 
 * @author Pape, Klukas
 */
public class BlHSVFilter extends AbstractSnapshotAnalysisBlock {
	
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
			ImageOperation processedMask = mask.io().copy();
			ImageStack fis = debug ? new ImageStack() : null;
			if (fis != null)
				fis.addImage(optics + " start", processedMask.getImage(), null);
			boolean isVis = optics.equals("VIS");
			String add = " (" + optics.toString() + ") ";
			if (getBoolean("process " + optics, true)) {
				String pf = "";
				
				for (int f_num = 1; f_num <= getInt("Number of Filters " + optics, 1); f_num++) {
					if (f_num == 1) {
						processedMask = processedMask.filterRemoveHSV(
							getDouble("min H" + add + f_num, !isVis ? 0.4 : optionsAndResults.getCameraPosition() == CameraPosition.TOP ? 0.4 : 0.4),
							getDouble("max H" + add + f_num, !isVis ? 1.0 : optionsAndResults.getCameraPosition() == CameraPosition.TOP ? 1.0 : 1.0),
							getDouble("min S" + add + f_num, !isVis ? 0.0 : optionsAndResults.getCameraPosition() == CameraPosition.TOP ? 0.0 : 0.0),
							getDouble("max S" + add + f_num, !isVis ? 1.0 : optionsAndResults.getCameraPosition() == CameraPosition.TOP ? 1.0 : 1.0),
							getDouble("min B" + add + f_num, !isVis ? 0.0 : optionsAndResults.getCameraPosition() == CameraPosition.TOP ? 0.0 : 0.0),
							getDouble("max B" + add + f_num, !isVis ? 1.0 : optionsAndResults.getCameraPosition() == CameraPosition.TOP ? 1.0 : 1.0));
					} else {
						processedMask = processedMask.and(processedMask.filterRemoveHSV(
							getDouble("min H" + add + f_num, !isVis ? 0.4 : optionsAndResults.getCameraPosition() == CameraPosition.TOP ? 0.4 : 0.4),
							getDouble("max H" + add + f_num, !isVis ? 1.0 : optionsAndResults.getCameraPosition() == CameraPosition.TOP ? 1.0 : 1.0),
							getDouble("min S" + add + f_num, !isVis ? 0.0 : optionsAndResults.getCameraPosition() == CameraPosition.TOP ? 0.0 : 0.0),
							getDouble("max S" + add + f_num, !isVis ? 1.0 : optionsAndResults.getCameraPosition() == CameraPosition.TOP ? 1.0 : 1.0),
							getDouble("min B" + add + f_num, !isVis ? 0.0 : optionsAndResults.getCameraPosition() == CameraPosition.TOP ? 0.0 : 0.0),
							getDouble("max B" + add + f_num, !isVis ? 1.0 : optionsAndResults.getCameraPosition() == CameraPosition.TOP ? 1.0 : 1.0)).getImage());
					}
				}
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
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Color Segmentation (HSB)";
	}
	
	@Override
	public String getDescriptionForParameters() {
		return "<ul><li>Number of Filters - Number of applied filters (differnt parameters possible)<li>H - Hue from tow to high<li>S - Saturation from low to high<li>B - Brightness from low to high</ul>";
	}
	
	@Override
	public String getDescription() {
		return "Uses a HSB(V)-based pixel filter(s) for the vis/fluo images to remove the specified color.";
	}
}