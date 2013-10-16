package iap.blocks.postprocessing;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.operations.skeleton.SkeletonProcessor2d;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

public class BlDrawSkeleton extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected Image processVISmask() {
		if (input() == null || input().masks() == null)
			return null;
		Image plantImg = input().masks().vis();
		boolean drawSkeleton = getBoolean("draw_skeleton", true);
		boolean debug = getBoolean("debug", false);
		Image skel = getProperties().getImage("skeleton");
		if (skel != null && plantImg != null) {
			skel.show("skel", debug);
			Image temp = plantImg.io().drawSkeleton(skel, drawSkeleton, SkeletonProcessor2d.getDefaultBackground()).getImage();
			temp.show("skel on vis", debug);
			return temp;
		} else
			return plantImg;
	}
	
	@Override
	protected Image processFLUOmask() {
		if (input() == null || input().masks() == null)
			return null;
		Image plantImg = input().masks().fluo();
		boolean drawSkeleton = getBoolean("draw_skeleton", true);
		Image skel = getProperties().getImage("skeleton_fluo");
		if (skel != null && plantImg != null)
			return plantImg.io().drawSkeleton(skel, drawSkeleton, SkeletonProcessor2d.getDefaultBackground()).getImage();
		else
			return plantImg;
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
		return BlockType.POSTPROCESSING;
	}
	
	@Override
	public String getName() {
		return "Draw Skeleton";
	}
	
	@Override
	public String getDescription() {
		return "Draws stored skeleton image on result images.";
	}
}
