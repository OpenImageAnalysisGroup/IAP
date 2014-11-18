/**
 * 
 */
package iap.blocks.segmentation;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageStack;

/**
 * Uses a lab-based pixel filter(s) for the vis/fluo images.
 * 
 * @author klukas
 */
public class BlLabFilter extends AbstractSnapshotAnalysisBlock {
	
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
			String add = isVis ? "" : " (" + optics + ")";
			if (getBoolean("process " + optics, true)) {
				String pf = "";
				processedMask = processedMask.filterRemoveLAB(
						getInt("min L" + add, !isVis ? 0 : optionsAndResults.getCameraPosition() == CameraPosition.TOP ? 120 : 120),
						getInt("max L" + add, !isVis ? 255 : optionsAndResults.getCameraPosition() == CameraPosition.TOP ? 255 : 255),
						getInt("min A" + add, !isVis ? 0 : optionsAndResults.getCameraPosition() == CameraPosition.TOP ? 0 : 0),
						getInt("max A" + add, !isVis ? 255 : optionsAndResults.getCameraPosition() == CameraPosition.TOP ? 138 : 138),
						getInt("min B" + add, !isVis ? 0 : optionsAndResults.getCameraPosition() == CameraPosition.TOP ? 125 : 125),
						getInt("max B" + add, !isVis ? 255 : optionsAndResults.getCameraPosition() == CameraPosition.TOP ? 255 : 255),
						optionsAndResults.getBackground(),
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
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Color Segmentation (Lab)";
	}
	
	@Override
	public String getDescriptionForParameters() {
		return "<ul><li>L - Brightness Low to High<li>A - Green to Red<li>B - Blue to Yellow</ul>";
	}
	
	@Override
	public String getDescription() {
		return "Uses a lab-based pixel filter(s) for the vis/fluo images.";
	}
}