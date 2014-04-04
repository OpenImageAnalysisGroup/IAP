package tests.plugins.pipelines.tobacco;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Block for the tobacco flower segmentation, plus additional remove small clusters to erase some noise.
 * 
 * @author pape
 */
public class BlTLabFilter extends AbstractSnapshotAnalysisBlock {
	
	@Override
	public void prepare() {
		
		int background = ImageOperation.BACKGROUND_COLORint;
		Image img = input().masks().vis();
		
		if (img == null)
			return;
		
		Integer[] valuesFlower = {
				getInt("from L", 200),
				getInt("to L", 255),
				getInt("from a", 0),
				getInt("to a", 255),
				getInt("from b", 0),
				getInt("to L", 255)
		};
		
		Image filtered = img.copy().io().filterRemoveLAB(valuesFlower, background, false).removeSmallClusters(true, null).getImage();
		input().masks().setVis(filtered);
		
		if (getBoolean("remove flowers from fluo", true)) {
			Image fluo = input().masks().fluo();
			input().masks()
					.setFluo(
							fluo.io()
									.applyMaskInversed_ResizeMaskIfNeeded(filtered.io().copy().blur(getDouble("blur flowers for removal", 5.0)).getImage(), background)
									.getImage());
		}
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
		return BlockType.PREPROCESSING;
	}
	
	@Override
	public String getName() {
		return "Lab Filter";
	}
	
	@Override
	public String getDescription() {
		return "Lab filter on Visible image for tobacco filtering.";
	}
	
}
