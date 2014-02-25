package iap.blocks.unused;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.awt.Color;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.MaskOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

public class BlockSetVisAndFluoMaskFromMergedVisAndFluo extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected Image processVISmask() {
		MaskOperation o = new MaskOperation(input().masks().fluo(), input().masks().vis(), null, optionsAndResults.getBackground(), Color.GRAY.getRGB());
		o.mergeMasks();
		Image result = new Image(input().masks().getLargestWidth(), input().masks().getLargestHeight(), o.getMask());
		int[] resPixels = result.getAs1A();
		{
			int filled = 0;
			int b = optionsAndResults.getBackground();
			for (int p : resPixels)
				if (p != b)
					filled++;
			// if vis is emptied by mask operation, don't change vis (fluo image is empty in this case)
			if (filled < resPixels.length * 0.001d)
				return input().masks().vis();
		}
		{
			int[] srcPixels = input().masks().vis().getAs1A();
			int b = optionsAndResults.getBackground();
			int filledSrc = 0;
			for (int p : srcPixels)
				if (p != b)
					filledSrc++;
			int filledRes = 0;
			for (int p : resPixels)
				if (p != b)
					filledRes++;
			// if vis is cleared for more than 95%, the vis image remains unchanged
			if (filledRes < filledSrc * 0.05d)
				return input().masks().vis();
		}
		
		return result;
	}
	
	@Override
	protected Image processFLUOmask() {
		MaskOperation o = new MaskOperation(input().masks().vis(), input().masks().fluo(), null, optionsAndResults.getBackground(), Color.GRAY.getRGB());
		o.mergeMasks();
		return new Image(input().masks().getLargestWidth(), input().masks().getLargestHeight(), o.getMask());
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
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Merge VIS and FLUO masks";
	}
	
	@Override
	public String getDescription() {
		return "Overlays the VIS and FLUO masks, the overlay is applied to the VIS and FLUO images. Only " +
				"parts where both camera types confirm plant areas are retained.";
	}
	
}
