package iap.blocks.acquisition;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.ImageSet;

/**
 * @author klukas
 */

public class BlFilterImagesByAngle extends AbstractSnapshotAnalysisBlock {
	
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
		boolean process = getBoolean("Process " + p.intValue() + u, true);
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
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
		res.add(CameraType.FLUO);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.ACQUISITION;
	}
	
	@Override
	public String getName() {
		return "Filter Images By Angle";
	}
	
	@Override
	public String getDescription() {
		return "Removes images with specific side or top rotation angle.";
	}
}
