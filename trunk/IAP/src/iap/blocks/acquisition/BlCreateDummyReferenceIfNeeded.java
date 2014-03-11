package iap.blocks.acquisition;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.awt.Color;
import java.util.HashSet;

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
		int c = getColor("Visible Replacement Color", Color.WHITE).getRGB();
		if (input().images().vis() != null && input().masks().vis() == null) {
			Image n = input().images().vis();
			int w = n.getWidth();
			int h = n.getHeight();
			return n.copy().io().canvas().fillRect(0, 0, w, h, c).getImage();
		} else
			return super.processVISmask();
	}
	
	@Override
	protected Image processFLUOmask() {
		int c = getColor("Fluo Replacement Color", Color.BLACK).getRGB();
		if (input().images().fluo() != null && input().masks().fluo() == null) {
			Image n = input().images().fluo();
			int w = n.getWidth();
			int h = n.getHeight();
			return n.copy().io().canvas().fillRect(0, 0, w, h, c).getImage();
		} else
			return super.processFLUOmask();
	}
	
	@Override
	protected Image processNIRmask() {
		int c = getColor("Nir Replacement Color", new Color(180, 180, 180)).getRGB();
		if (input().images().nir() != null && input().masks().nir() == null) {
			Image n = input().images().nir();
			int w = n.getWidth();
			int h = n.getHeight();
			return n.copy().io().canvas().fillRect(0, 0, w, h, c).getImage();
		} else
			return super.processNIRmask();
	}
	
	@Override
	protected Image processIRmask() {
		int c = getColor("Ir Replacement Color", Color.BLACK).getRGB();
		if (input().images().ir() != null && input().masks().ir() == null) {
			Image n = input().images().ir();
			int w = n.getWidth();
			int h = n.getHeight();
			return n.copy().io().canvas().fillRect(0, 0, w, h, c).getImage();
		} else
			return super.processIRmask();
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
