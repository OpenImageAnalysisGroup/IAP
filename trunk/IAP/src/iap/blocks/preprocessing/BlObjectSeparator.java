package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import de.ipk.ag_ba.image.operations.segmentation.ClusterDetection;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Separate objects according to their size, up to the maximum object count,
 * and starting from the biggest to the smallest object.
 * Objects need to be larger than the specified minimum size.
 * 
 * @author klukas
 */
public class BlObjectSeparator extends AbstractBlock implements WellProcessor {
	int maxN;
	double minimumSize;
	
	@Override
	protected void prepare() {
		super.prepare();
		maxN = getDefinedWellCount(options);
		minimumSize = getDouble("Minimum Object Size (percent)", 5) / 100;
	}
	
	@Override
	protected Image processMask(Image mask) {
		if (mask == null || mask.getHeight() <= 1)
			return mask;
		else {
			boolean process = getBoolean("Process " + mask.getCameraType(), mask.getCameraType() == CameraType.VIS);
			
			if (!process)
				return mask;
			
			ClusterDetection cd = new ClusterDetection(mask, options.getBackground());
			cd.detectClusters();
			
			int[] clusterSize = cd.getClusterSize();
			int[] clusteredPixels = cd.getImageClusterIdMask();
			
			int minimumPixelCount = (int) (clusteredPixels.length * minimumSize);
			int nValidClusters = 0;
			boolean first = true;
			for (int cs : clusterSize) {
				if (first) {
					first = false;
					continue;
				}
				if (cs >= minimumPixelCount)
					nValidClusters++;
			}
			if (nValidClusters > maxN)
				nValidClusters = maxN;
			
			ArrayList<Integer> sortedClusterSizes = new ArrayList<Integer>();
			for (Integer cs : clusterSize)
				sortedClusterSizes.add(cs);
			sortedClusterSizes.remove(0); // remove background ID (0)
			Collections.sort(sortedClusterSizes);
			while (sortedClusterSizes.size() > nValidClusters)
				sortedClusterSizes.remove(0);
			Collections.reverse(sortedClusterSizes);
			
			ArrayList<Integer> orderOfIds = new ArrayList<Integer>(nValidClusters);
			ArrayList<Integer> clusterIDs = new ArrayList<Integer>(clusterSize.length);
			for (int id = 0; id < clusterSize.length; id++)
				clusterIDs.add(id);
			
			for (Integer sortedSize : sortedClusterSizes) {
				for (Integer clusterID : clusterIDs) {
					if (clusterSize[clusterID] == sortedSize.intValue()) {
						orderOfIds.add(clusterID);
					}
				}
			}
			
			int wellIdx = options.getWellIdx();
			int validClusterID = -1;
			if (wellIdx < orderOfIds.size())
				validClusterID = orderOfIds.get(options.getWellIdx());
			int[] pixels = mask.getAs1A();
			int[] result = new int[pixels.length];
			int back = options.getBackground();
			for (int i = 0; i < pixels.length; i++) {
				int cluster = clusteredPixels[i];
				if (cluster == validClusterID)
					result[i] = pixels[i];
				else
					result[i] = back;
			}
			return new Image(mask.getWidth(), mask.getHeight(), result);
		}
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
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
		return "Separate Objects";
	}
	
	@Override
	public String getDescription() {
		return "Processes separated objects individually (from biggest to smallest, " +
				"up to maximum object count, and larger than " +
				"the minimum size).";
	}
	
	@Override
	public int getDefinedWellCount(ImageProcessorOptions options) {
		return options.getIntSetting(this, "Maximum Object Count", 10);
	}
	
}
