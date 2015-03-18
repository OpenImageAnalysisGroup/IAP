package iap.blocks.segmentation;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

@Deprecated
public class BlClosing extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected Image processVISmask() {
		return closing(input().masks().vis(), input().images().vis(), getInt("VIS-size", 3));
	}
	
	@Override
	protected Image processFLUOmask() {
		return closing(input().masks().fluo(), input().images().fluo(), getInt("FLUO-size", 0));
	}
	
	@Override
	protected Image processNIRmask() {
		return closing(input().masks().nir(), input().images().nir(), getInt("NIR-size", 0));
	}
	
	@Override
	protected Image processIRmask() {
		return closing(input().masks().ir(), input().images().ir(), getInt("IR-size", 0));
	}
	
	private Image closing(Image mask, Image image, int n) {
		if (mask == null || image == null || n == 0) {
			return mask;
		}
		return closing(mask, image, optionsAndResults.getBackground(), n);
	}
	
	private static Image closing(Image flMask, Image flImage, int iBackgroundFill, int closingRepeat) {
		int h = flMask.getHeight();
		int w = flMask.getWidth();
		
		int hImage = flImage.getHeight();
		int wImage = flImage.getWidth();
		
		if (hImage != h)
			flImage.resize(w, h);
		if (wImage != w)
			flImage.resize(w, h);
		
		ImageOperation op = flMask.io();
		
		op = op.bm().closing(BlMorphologicalOperations.getRoundMask(closingRepeat)).io();
		return flImage.copy().io().and(op.getImage()).getImage();
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Closing Operation";
	}
	
	@Override
	public String getDescription() {
		return "Performs closing operations on the visible light image.";
	}
}