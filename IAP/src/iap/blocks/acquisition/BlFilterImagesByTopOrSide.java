package iap.blocks.acquisition;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.ImageSet;

/**
 * @author klukas
 */

public class BlFilterImagesByTopOrSide extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected boolean isChangingImages() {
		return true;
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		return res;
	}
	
	@Override
	protected void postProcess(ImageSet processedImages, ImageSet processedMasks) {
		Double p = processedImages.getAnyInfo().getPosition();
		if (p == null)
			p = 0d;
		String u = processedImages.getAnyInfo().getPositionUnit();
		if (u == null)
			u = "";
		else
			u = " " + u;
		boolean process = getBoolean("Process " + optionsAndResults.getCameraPosition() + " view images", true);
		if (!process) {
			processedImages.setVis(null);
			processedImages.setFluo(null);
			processedImages.setNir(null);
			processedImages.setIr(null);
			
			processedMasks.setVis(null);
			processedMasks.setFluo(null);
			processedMasks.setNir(null);
			processedMasks.setIr(null);
		}
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.FILTER;
	}
	
	@Override
	public String getName() {
		return "Filter Images by Camera Position";
	}
	
	@Override
	public String getDescription() {
		return "Removes images from side or top.";
	}
}
