package de.ipk.ag_ba.image.operations.blocks.cmds.roots;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;

import org.StringManipulationTools;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.skeleton.SkeletonProcessor2d;
import de.ipk.ag_ba.image.structures.FlexibleImage;

/**
 * Skeletonize the roots and store root lengths, and other parameters.
 * 
 * @author klukas
 */
public class BlRootsSkeletonize extends AbstractSnapshotAnalysisBlockFIS {
	boolean debug = false;
	
	int white = Color.WHITE.getRGB();
	int black = Color.BLACK.getRGB();
	int blue = Color.BLUE.getRGB();
	
	@Override
	protected FlexibleImage processVISmask() {
		int background = options.getBackground();
		
		FlexibleImage img = input().masks().vis();
		if (img != null) {
			ResultsTableWithUnits rt = new ResultsTableWithUnits();
			rt.incrementCounter();
			
			ImageOperation inp = img.io().print("INPUT FOR SKEL", debug);
			
			rt.addValue("roots.filled.pixels", inp.countFilledPixels());
			ImageOperation binary = inp.binary(Color.BLACK.getRGB(), background);
			{
				int width = 1;
				int pixelCnt = 0;
				ImageOperation image = binary.copy();
				widthHistogram(rt, image);
			}
			
			inp = binary.skeletonize(false).print("INPUT FOR BRANCH DETECTION", debug);
			
			rt.addValue("roots.skeleton.length", inp.countFilledPixels());
			
			SkeletonProcessor2d skel = new SkeletonProcessor2d(getInvert(inp.getImage()));
			skel.findEndpointsAndBranches();
			
			img = skel.getAsFlexibleImage().print("THE SKELETON", debug);
			
			ArrayList<Point> branchPoints = skel.getBranches();
			rt.addValue("roots.skeleton.branchpoints", branchPoints.size());
			rt.addValue("roots.skeleton.endpoints", skel.getEndpoints().size());
			
			getProperties().storeResults("RESULT_scan.", rt, getBlockPosition());
		}
		return img;
	}
	
	private void widthHistogram(ResultsTableWithUnits rt, ImageOperation image) {
		HashMap<Integer, Integer> width2len = new HashMap<Integer, Integer>();
		double area = 80;
		int width = 1;
		int pixelCnt;
		do {
			area -= Math.random() * 20d;
			pixelCnt = (int) area;
			if (pixelCnt > 0) {
				width2len.put(width, pixelCnt);
				image = image.erode();
				width++;
			}
		} while (pixelCnt > 0);
		width--;
		int over19 = 0;
		for (int w = 1; w <= width; w++) {
			Integer len = width2len.get(w);
			if (len == null)
				len = 0;
			Integer subtract = 0;
			if (w < width) {
				subtract = width2len.get(w + 1);
				if (subtract == null)
					subtract = 0;
			}
			int realLen = len - subtract;
			int www = width - w + 1;
			if (w == 1 && www < 19) {
				for (int wm = www + 1; wm <= 19; wm++)
					rt.addValue("roots.skeleton.width." + StringManipulationTools.formatNumber(wm, "00") + ".length", 0);
			}
			if (www > 19)
				over19 += realLen;
			else
				rt.addValue("roots.skeleton.width." + StringManipulationTools.formatNumber(www, "00") + ".length", realLen);
			subtract += len;
		}
		rt.addValue("roots.skeleton.width.20.length", over19);
	}
	
	/**
	 * Function to invert skeleton image, invert from class imageoperation does not work
	 * 
	 * @param input
	 * @return
	 */
	private FlexibleImage getInvert(FlexibleImage input) {
		int[][] img = input.getAs2A();
		int width = img.length;
		int height = img[0].length;
		int[][] res = new int[width][height];
		int black = Color.BLACK.getRGB();
		int white = Color.WHITE.getRGB();
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				
				if (img[x][y] == -768) // -768 Background should be added, depends on the values of the imagej skeleton, color.White dont work
					res[x][y] = black;
				else {
					res[x][y] = white;
				}
			}
		}
		return new FlexibleImage(res);
	}
}
