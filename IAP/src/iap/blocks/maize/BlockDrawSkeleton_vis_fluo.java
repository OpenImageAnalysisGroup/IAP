package iap.blocks.maize;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.util.HashSet;

import de.ipk.ag_ba.image.operations.skeleton.SkeletonProcessor2d;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;

public class BlockDrawSkeleton_vis_fluo extends AbstractSnapshotAnalysisBlockFIS {
	
	@Override
	protected FlexibleImage processVISmask() {
		if (input() == null || input().masks() == null)
			return null;
		FlexibleImage plantImg = input().masks().vis();
		boolean drawSkeleton = getBoolean("draw_skeleton", true);
		FlexibleImage skel = getProperties().getImage("skeleton");
		if (skel != null && plantImg != null)
			return plantImg.io().drawSkeleton(skel, drawSkeleton, SkeletonProcessor2d.getDefaultBackground()).getImage();
		else
			return plantImg;
	}
	
	@Override
	protected FlexibleImage processFLUOmask() {
		if (input() == null || input().masks() == null)
			return null;
		FlexibleImage plantImg = input().masks().fluo();
		boolean drawSkeleton = getBoolean("draw_skeleton", true);
		FlexibleImage skel = getProperties().getImage("skeleton_fluo");
		if (skel != null && plantImg != null)
			return plantImg.io().drawSkeleton(skel, drawSkeleton, SkeletonProcessor2d.getDefaultBackground()).getImage();
		else
			return plantImg;
	}
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		res.add(FlexibleImageType.FLUO);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		return getInputTypes();
	}
}
