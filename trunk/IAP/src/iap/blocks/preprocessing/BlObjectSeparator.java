package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.Vector2i;

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
	private boolean sortBySize;
	private boolean sortByX;
	private boolean sortByY;
	
	@Override
	protected void prepare() {
		super.prepare();
		maxN = getDefinedWellCount(options);
		minimumSize = getDouble("Minimum Object Size (percent)", 5) / 100;
		this.sortBySize = getBoolean("Sort By Size (off for sort by position)", true);
		this.sortByX = getBoolean("Left to Right (X)", false);
		this.sortByY = getBoolean("Top to Bottom (Y)", false);
		
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
			
			Vector2i[] clusterPositions = cd.getClusterCenterPoints();
			int[] clusterSize = cd.getClusterSize();
			
			int[] sortCriteria;
			if (sortBySize) {
				sortCriteria = clusterSize;
			} else {
				sortCriteria = new int[clusterPositions.length];
				int idx = 0;
				for (Vector2i p : clusterPositions) {
					if (sortByX && sortByY) {
						sortCriteria[idx++] = p.x + p.y;
					} else
						if (sortByX) {
							sortCriteria[idx++] = p.x;
						} else {
							sortCriteria[idx++] = p.y;
						}
				}
			}
			
			int[] clusteredPixels = cd.getImageClusterIdMask();
			
			int minimumPixelCount = (int) (clusteredPixels.length * minimumSize);
			int nValidClusters = 0;
			boolean first = true;
			for (int cs : sortCriteria) {
				if (first) {
					first = false;
					continue;
				}
				if (cs >= minimumPixelCount)
					nValidClusters++;
			}
			if (nValidClusters > maxN)
				nValidClusters = maxN;
			
			ArrayList<Integer> sortedClusterProperties = new ArrayList<Integer>();
			for (Integer cs : sortCriteria)
				sortedClusterProperties.add(cs);
			sortedClusterProperties.remove(0); // remove background ID (0)
			Collections.sort(sortedClusterProperties);
			while (sortedClusterProperties.size() > nValidClusters)
				sortedClusterProperties.remove(0);
			Collections.reverse(sortedClusterProperties);
			
			ArrayList<Integer> orderOfIds = new ArrayList<Integer>(nValidClusters);
			ArrayList<Integer> targetIdxList = new ArrayList<Integer>(sortCriteria.length);
			for (int id = 0; id < sortCriteria.length; id++)
				targetIdxList.add(id);
			
			for (Integer sortedClusterProperty : sortedClusterProperties) {
				for (Integer clusterID : targetIdxList) {
					if (sortCriteria[clusterID] == sortedClusterProperty.intValue()) {
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
			int filled = 0;
			for (int i = 0; i < pixels.length; i++) {
				int cluster = clusteredPixels[i];
				if (cluster == validClusterID) {
					result[i] = pixels[i];
					filled++;
				} else
					result[i] = back;
			}
			if (filled == 0)
				return null;
			else
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
