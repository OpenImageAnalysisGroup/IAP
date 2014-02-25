package iap.blocks.acquisition;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk.ag_ba.image.structures.ImageSet;

/**
 * Create a simulated, dummy reference image (in case the reference image is NULL).
 * 
 * @author pape, klukas
 */
public class BlCreateDummyReferenceIfNeeded extends AbstractSnapshotAnalysisBlock {
	
	boolean debug;
	
	@Override
	protected void prepare() {
		super.prepare();
		debug = getBoolean("debug", false);
	}
	
	@Override
	protected Image processVISmask() {
		if (input().images().vis() != null && input().masks().vis() == null) {
			Image n = input().images().vis();
			int w = n.getWidth();
			int h = n.getHeight();
			return n.copy().io().canvas().fillRect(0, 0, w, h, new Color(
					getInt("dummy-vis-background-color", 180),
					getInt("dummy-vis-background-color", 180),
					getInt("dummy-vis-background-color", 180)).getRGB()).getImage();
		}
		return super.processVISmask();
	}
	
	@Override
	protected Image processFLUOmask() {
		if (input().images().fluo() != null && input().masks().fluo() == null)
			return input().images().fluo().copy().io().
					blur(getInt("dummy-fluo-blur", 2)).
					thresholdLAB(
							getInt("dummy-fluo-minL", 0), getInt("dummy-fluo-maxL", 50),
							getInt("dummy-fluo-minA", 0), getInt("dummy-fluo-maxA", 500),
							getInt("dummy-fluo-minB", 0), getInt("dummy-fluo-maxB", 155),
							ImageOperation.BACKGROUND_COLORint, CameraPosition.SIDE, false, false).
					blur(getInt("dummy-fluo-blur", 2)).
					getImage();
		else
			return super.processFLUOmask();
	}
	
	@Override
	protected Image processNIRmask() {
		Image n = input().images().nir();
		if (n != null && input().masks().nir() == null) {
			int w = n.getWidth();
			int h = n.getHeight();
			return n.copy().io().canvas().fillRect(0, 0, w, h, new Color(
					getInt("dummy-nir-background-color", 180),
					getInt("dummy-nir-background-color", 180),
					getInt("dummy-nir-background-color", 180)).getRGB()).getImage();
		} else
			return super.processNIRmask();
	}
	
	@Override
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
		super.postProcess(processedImages, processedMasks);
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
		return BlockType.ACQUISITION;
	}
	
	@Override
	public String getName() {
		return "Create Reference Images";
	}
	
	@Override
	public String getDescription() {
		return "Create a simulated, dummy reference image (in case the reference image is NULL).";
	}
}
