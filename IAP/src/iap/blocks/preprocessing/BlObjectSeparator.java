package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptionsAndResults;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
		maxN = getDefinedWellCount(optionsAndResults);
		minimumSize = getDouble("Minimum Object Size (percent)", 1) / 100;
		this.sortBySize = getBoolean("Sort By Size (off for sort by position)", false);
		this.sortByX = getBoolean("Left to Right (X)", true);
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
			
			ClusterDetection cd = new ClusterDetection(mask, optionsAndResults.getBackground());
			cd.detectClusters();
			
			Vector2i[] clusterPositions = cd.getClusterCenterPoints();
			int[] clusterSize = cd.getClusterSize();
			
			final int[] sortCriteria;
			if (sortBySize) {
				sortCriteria = clusterSize != null ? clusterSize : new int[] {};
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
			if (clusteredPixels == null)
				return null;
			int minimumPixelCount = (int) (clusteredPixels.length * minimumSize);
			int nValidClusters = 0;
			boolean first = true;
			if (clusterSize != null)
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
			
			ArrayList<Integer> sortedClusterProperties = new ArrayList<Integer>();
			if (clusterSize != null)
				for (Integer cs : clusterSize)
					sortedClusterProperties.add(cs);
			if (sortedClusterProperties.size() > 0) {
				sortedClusterProperties.remove(0); // remove background ID (0)
				Collections.sort(sortedClusterProperties);
				while (sortedClusterProperties.size() > nValidClusters)
					sortedClusterProperties.remove(0);
				Collections.reverse(sortedClusterProperties);
			}
			ArrayList<Integer> orderOfIds = new ArrayList<Integer>(nValidClusters);
			ArrayList<Integer> targetIdxList = new ArrayList<Integer>(sortCriteria.length);
			if (clusterSize != null)
				for (int id = 0; id < clusterSize.length; id++)
					targetIdxList.add(id);
			
			for (Integer sortedClusterProperty : sortedClusterProperties) {
				for (Integer clusterID : targetIdxList) {
					if (clusterSize[clusterID] == sortedClusterProperty.intValue()) {
						orderOfIds.add(clusterID);
					}
				}
			}
			
			if (!sortBySize)
				Collections.sort(orderOfIds, new Comparator<Integer>() {
					@Override
					public int compare(Integer o1, Integer o2) {
						Integer s1 = sortCriteria[o1];
						Integer s2 = sortCriteria[o2];
						return s1.compareTo(s2);
					}
				});
			
			int wellIdx = optionsAndResults.getWellIdx();
			int validClusterID = -1;
			if (wellIdx < orderOfIds.size())
				validClusterID = orderOfIds.get(wellIdx);
			
			int[] pixels = mask.getAs1A();
			int[] result = new int[pixels.length];
			int back = optionsAndResults.getBackground();
			int filled = 0;
			for (int i = 0; i < pixels.length; i++) {
				int cluster = clusteredPixels[i];
				if (cluster == validClusterID) {
					result[i] = pixels[i];
					filled++;
				} else
					result[i] = back;
			}
			boolean returnInputMask = getBoolean("Return Inputmask if Objectcount < min Objctcount", false);
			if (filled == 0)
				if (returnInputMask )
					return mask;
				else
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
	public int getDefinedWellCount(ImageProcessorOptionsAndResults options) {
		if (options.getBooleanSetting(this, "enabled", true) == false)
			return 1;
		return options.getIntSetting(this, "Maximum Object Count", 10);
	}
}
