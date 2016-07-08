/**
 * 
 */
package iap.blocks.segmentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import ij.plugin.filter.RankFilters;

/**
 * Block including different rank filters with adaptive kernel sizes.
 * 
 * @author Pape
 */
public class BlMedianFilter extends AbstractBlock {
	
	@Override
	protected Image processMask(Image mask) {
		if (mask == null || !getBoolean("Process " + mask.getCameraType(), true))
			return mask;
			
		HashMap<String, Integer> modes = new HashMap<>();
		
		modes.put("Max", RankFilters.MAX);
		modes.put("Mean", RankFilters.MEAN);
		modes.put("Median", RankFilters.MEDIAN);
		modes.put("Min", RankFilters.MIN);
		modes.put("Variance", RankFilters.VARIANCE);
		modes.put("Open", RankFilters.OPEN);
		modes.put("Close", RankFilters.CLOSE);
		modes.put("Outliers", RankFilters.OUTLIERS);
		// modes.put("Despeckle", RankFilters.DESPECKLE);
		modes.put("Dark Outliers", RankFilters.DARK_OUTLIERS);
		modes.put("Bright Outliers", RankFilters.BRIGHT_OUTLIERS);
		
		ArrayList<String> mode_names = new ArrayList<>();
		for (String key : modes.keySet())
			mode_names.add(key);
			
		String methodName = optionsAndResults.getStringSettingRadio(this, "Thresholding Method", "Median", mode_names);
		int mode = modes.get("Median");
		
		for (String m : mode_names)
			if (methodName.equalsIgnoreCase(m))
			mode = modes.get(m);
			
		Image medianMask = new ImageOperation(mask).copy().rankFilterImageJ(getInt("Kernel size", 3), mode).getImage();
		
		return medianMask;
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
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		res.add(CameraType.FLUO);
		res.add(CameraType.NIR);
		res.add(CameraType.IR);
		return res;
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.PREPROCESSING;
	}
	
	@Override
	public String getName() {
		return "Rank Filter (adaptive kernel size)";
	}
	
	@Override
	public String getDescription() {
		return "Apply a rank filter to an image such as 'Median', 'Max' ... (includes adaptive Kernel size). Waring: Some modes does not perfom correctly on already processed images (including defined background color, this color will not be ignored). Please use this block before performing the foreground-/background-segmentation";
	}
	
}
