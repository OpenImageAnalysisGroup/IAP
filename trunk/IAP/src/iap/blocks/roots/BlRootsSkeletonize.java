package iap.blocks.roots;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import org.Colors;
import org.StringManipulationTools;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.operations.segmentation.ClusterDetection;
import de.ipk.ag_ba.image.operations.skeleton.SkeletonGraph;
import de.ipk.ag_ba.image.operations.skeleton.SkeletonProcessor2d;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;
import de.ipk.ag_ba.mongo.MongoDB;

/**
 * Skeletonize the roots and store root lengths, and other parameters.
 * 
 * @author klukas
 */
public class BlRootsSkeletonize extends AbstractSnapshotAnalysisBlockFIS {
	boolean debug = false;
	
	@Override
	protected FlexibleImage processVISmask() {
		debug = getBoolean("debug", false);
		int background = options.getBackground();
		
		ImageOperation img = null;
		{
			FlexibleImage i = input().masks().vis();
			if (i == null)
				return null;
			img = i.io();
		}
		ResultsTableWithUnits rt = new ResultsTableWithUnits();
		rt.incrementCounter();
		
		img = img.binary(0, Color.WHITE.getRGB());
		
		ImageOperation inDilatedForSectionDetection = img.copy().dilate(10).show("Dilated image for section detection", debug);
		
		ClusterDetection cd = new ClusterDetection(inDilatedForSectionDetection.getImage(), ImageOperation.BACKGROUND_COLORint);
		cd.detectClusters();
		int clusters = cd.getClusterCount();
		rt.addValue("roots.part.count", clusters);
		
		img = skeletonizeImage("", background, img, rt);
		
		int[] unchangedSkeletonPixels = img.getImageAs1dArray();
		
		ImageOperation ioClusteredSkeltonImage = new FlexibleImage(
				inDilatedForSectionDetection.getWidth(),
				inDilatedForSectionDetection.getHeight(),
				cd.getImageClusterIdMask()).io();
		
		ArrayList<Color> cols = Colors.get(clusters + 1);
		for (int i = 1; i < cols.size(); i++) {
			ioClusteredSkeltonImage = ioClusteredSkeltonImage.replaceColor(i, cols.get(i).getRGB());
		}
		
		int[] clusterIDsPixels = ioClusteredSkeltonImage.getImageAs1dArray();
		
		boolean thinClusterSkel = true;
		if (thinClusterSkel) {
			for (int i = 0; i < unchangedSkeletonPixels.length; i++)
				if (unchangedSkeletonPixels[i] == -16777216)
					clusterIDsPixels[i] = background;
			
			HashMap<Integer, Integer> color2clusterId = new HashMap<Integer, Integer>();
			ClusterSizeAndClusterId[] clusterSize = new ClusterSizeAndClusterId[clusters + 1];
			for (int i = 0; i < clusterSize.length; i++)
				clusterSize[i] = new ClusterSizeAndClusterId();
			int idx = 0;
			for (int p : clusterIDsPixels) {
				if (!color2clusterId.containsKey(p))
					color2clusterId.put(p, idx++);
				clusterSize[color2clusterId.get(p)].size += 1;
				clusterSize[color2clusterId.get(p)].id = color2clusterId.get(p);
			}
			
			TreeSet<ClusterSizeAndClusterId> sizes = new TreeSet<ClusterSizeAndClusterId>();
			for (ClusterSizeAndClusterId s : clusterSize)
				sizes.add(s);
			idx = 0;
			for (ClusterSizeAndClusterId size : sizes) {
				int cluster = clusters - idx;
				// first cluster is background, is ignored
				if (cluster != 0)
					rt.addValue("roots.part." + StringManipulationTools.formatNumber(cluster, "00") + ".skeleton.length", size.size);
				idx++;
			}
			
			if (getBoolean("Calculate Diameter for all Components", true)) {
				int w = img.getWidth();
				int h = img.getHeight();
				try {
					graphAnalysis(clusterIDsPixels,
							new FlexibleImage(w, h,
									img.getImageAs1dArray()
							)
									.show("input for graph analysis", debug).io(), rt);
				} catch (Exception e) {
					e.printStackTrace();
					MongoDB.saveSystemErrorMessage("Could not process skeleton graph", e);
				}
			}
			
			if (!getBoolean("Calculate Diameter for all Components", true)) {
				if (sizes.size() > 1) {
					// at least one cluster (besides the background has been found)
					while (sizes.size() > 2)
						sizes.remove(sizes.iterator().next());
					int largestClusterSize = sizes.iterator().next().size;
					int cidx = 0;
					for (int iii = 0; iii < clusterSize.length; iii++)
						if (clusterSize[iii].size == largestClusterSize)
							cidx = iii;
					int targetClusterColor = -1;
					for (int color : color2clusterId.keySet())
						if (color2clusterId.get(color) != null && color2clusterId.get(color) == cidx)
							targetClusterColor = color;
					int[] pixels = ioClusteredSkeltonImage.copy().getImageAs1dArray();
					for (int pidx = 0; pidx < pixels.length; pidx++) {
						int c = pixels[pidx];
						if (c != targetClusterColor && c != background)
							pixels[pidx] = background;
					}
					int w = ioClusteredSkeltonImage.getWidth();
					int h = ioClusteredSkeltonImage.getHeight();
					try {
						graphAnalysis(clusterIDsPixels, new FlexibleImage(w, h, pixels).show("largest root component for graph analysis", debug)
								.io(), rt);
					} catch (Exception e) {
						e.printStackTrace();
						// MongoDB.saveSystemErrorMessage("Could not process skeleton graph", e);
					}
				}
			}
			ioClusteredSkeltonImage = new ImageOperation(clusterIDsPixels, ioClusteredSkeltonImage.getWidth(), ioClusteredSkeltonImage.getHeight());
		}
		ioClusteredSkeltonImage.show("CLUSTERS", false);
		
		getProperties().storeResults("RESULT_", rt, getBlockPosition());
		return ioClusteredSkeltonImage.dilate(20).getImage();
	}
	
	private void graphAnalysis(int[] clusterIDsPixels, ImageOperation in, ResultsTableWithUnits rt) throws Exception {
		boolean graphAnalysis = getBoolean("graqh-based analysis", true);
		if (graphAnalysis) {
			SkeletonProcessor2d skel = new SkeletonProcessor2d(in.getImage());
			// System.out.println("Real background: " + in.replaceColor(-1, SkeletonProcessor2d.getDefaultBackground()).getImageAs1dArray()[0]);
			skel.background = SkeletonProcessor2d.getDefaultBackground();
			skel.findEndpointsAndBranches();
			skel.calculateEndlimbsRecursive();
			int nEndLimbs = skel.endlimbs.size();
			// System.out.println("LIMBS: " + nEndLimbs);
			SkeletonGraph sg = new SkeletonGraph(in.getWidth(), in.getHeight(), skel.skelImg);
			sg.createGraph(clusterIDsPixels);
			sg.getGraph().numberGraphElements();
			// System.out.println("Analyze Diameter(s) For Skeleton Graph with " + sg.getGraph().getNumberOfNodes()
			// + " nodes and " + sg.getGraph().getNumberOfEdges() + " edges...");
			
			HashMap<Integer, Double> id2size = sg.calculateDiameter(null);
			HashMap<Integer, Integer> co2i = new HashMap<Integer, Integer>();
			int idx = 1;
			for (Integer id : id2size.keySet()) {
				if (id2size.get(id) <= 0)
					continue;
				if (id != -1 && !co2i.containsKey(id))
					co2i.put(id, idx++);
				int niceID = id;
				if (id != -1)
					niceID = co2i.get(id);
				if (niceID >= 0)
					rt.addValue("roots.diameter_part." + StringManipulationTools.formatNumber(niceID, "00") + ".length", id2size.get(id));
				else
					rt.addValue("roots.skeleton.diameter.max", id2size.get(id));
			}
		}
	}
	
	private ImageOperation skeletonizeImage(
			String pre,
			int background, ImageOperation img, ResultsTableWithUnits rt) {
		ImageOperation inp = img.show("INPUT FOR SKEL", debug);
		
		rt.addValue(pre + "roots.filled.pixels", inp.countFilledPixels());
		ImageOperation binary = inp.binary(Color.BLACK.getRGB(), background);
		{
			ImageOperation image = binary.copy();
			widthHistogram(rt, image);
		}
		
		inp = binary.skeletonize(false).show("INPUT FOR BRANCH DETECTION", debug);
		
		rt.addValue(pre + "roots.skeleton.length", inp.countFilledPixels());
		
		SkeletonProcessor2d skel = new SkeletonProcessor2d(getInvert(inp.getImage()));
		skel.findEndpointsAndBranches();
		
		img = skel.getImageOperation().show("THE SKELETON", debug);
		
		ArrayList<Point> branchPoints = skel.getBranches();
		
		rt.addValue(pre + "roots.skeleton.branchpoints", branchPoints.size());
		rt.addValue(pre + "roots.skeleton.endpoints", skel.getEndpoints().size());
		
		return img;
	}
	
	private void widthHistogram(ResultsTableWithUnits rt, ImageOperation image) {
		HashMap<Integer, Integer> width2len = new HashMap<Integer, Integer>();
		// double area = 80;
		int width = 1;
		int pixelCnt;
		image = image.resize(2);
		do {
			// area -= Math.random() * 20d;
			// pixelCnt = (int) area;
			pixelCnt = image.skeletonize(false).countFilledPixels();
			if (pixelCnt > 0) {
				width2len.put(width, pixelCnt);
				image = image.erode();
				width += 1;
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
	
	@Override
	public HashSet<FlexibleImageType> getInputTypes() {
		HashSet<FlexibleImageType> res = new HashSet<FlexibleImageType>();
		res.add(FlexibleImageType.VIS);
		return res;
	}
	
	@Override
	public HashSet<FlexibleImageType> getOutputTypes() {
		return getInputTypes();
	}
	
}
