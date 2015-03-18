package iap.blocks.preprocessing;

import iap.blocks.data_structures.AbstractBlock;
import iap.blocks.data_structures.BlockType;
import iap.pipelines.ImageProcessorOptionsAndResults;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;

import org.Colors;
import org.Vector2i;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Calculates the distance for each pixel to center point from each cluster which is defined by x and y position.
 * 
 * @author Ulrich, Pape
 */

public class BlObjectSeparatorByDistance extends AbstractBlock implements WellProcessor {
	
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
		return BlockType.PREPROCESSING;
	}
	
	@Override
	public String getName() {
		return "Cut Well Parts (Irregular Positions)";
	}
	
	@Override
	public String getDescription() {
		return "Separates objects by the distance of each pixel to each given center point of a cluster.";
	}
	
	@Override
	protected Image processMask(Image mask) {
		
		if (mask == null || input() == null || input().masks() == null || input().masks().vis() == null)
			return null;
		
		int wellcount = getDefinedWellCount(optionsAndResults);
		
		final double SCALE_X = mask.getWidth() / (double) input().masks().vis().getWidth();
		final double SCALE_Y = mask.getHeight() / (double) input().masks().vis().getHeight();
		
		int[][] result = mask.getAs2A();
		
		Vector2i[] centerpoints = new Vector2i[wellcount];
		
		for (int i = 0; i < wellcount; i++) {
			int cx = (int) (getInt("Position (VIS) " + (i + 1) + " (x)", 0) * SCALE_X);
			int cy = (int) (getInt("Position (VIS) " + (i + 1) + " (y)", 0) * SCALE_Y);
			
			centerpoints[i] = new Vector2i(cx, cy);
		}
		boolean debug = getBoolean("debug", false);
		ArrayList<Color> wellColorsC = Colors.get(wellcount);
		int[] wellColors = new int[wellColorsC.size()];
		{
			int idx = 0;
			for (Color c : wellColorsC) {
				wellColors[idx++] = c.getRGB();
			}
		}
		int currentwell = optionsAndResults.getWellIdx();
		for (int x = 0; x < mask.getWidth(); x++) {
			for (int y = 0; y < mask.getHeight(); y++) {
				int minWellDistance = Integer.MAX_VALUE;
				int min_well_idx = -1;
				
				for (int well_idx = 0; well_idx < wellcount; well_idx++) {
					// distance for each pixel to center point from each cluster
					int distance_to_cluster = (x - centerpoints[well_idx].x) * (x - centerpoints[well_idx].x) +
							(y - centerpoints[well_idx].y) * (y - centerpoints[well_idx].y);
					
					int tempDist = distance_to_cluster;
					
					if (tempDist < minWellDistance) {
						minWellDistance = tempDist;
						min_well_idx = well_idx;
					}
				}
				if (debug) {
					if (min_well_idx >= 0 && (((x + y) % 2) == 0))
						result[x][y] = wellColors[min_well_idx];
				} else
					if (min_well_idx != currentwell) {
						result[x][y] = ImageOperation.BACKGROUND_COLORint;
					}
			}
		}
		return new Image(result);
		
	}
	
	@Override
	public int getDefinedWellCount(ImageProcessorOptionsAndResults options) {
		if (options.getBooleanSetting(this, "enabled", true) == false)
			return 1;
		return options.getIntSetting(this, "Maximum Object Count", 1);
	}
	
}