package iap.example.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.RunnableOnImage;
import iap.blocks.unused.ClusterSizeAndClusterId;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import org.Colors;
import org.StringManipulationTools;
import org.SystemAnalysis;
import org.Vector2i;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.graffiti.graph.Edge;

import de.ipk.ag_ba.image.operation.DistanceCalculationMode;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.operations.segmentation.ClusterDetection;
import de.ipk.ag_ba.image.operations.skeleton.SkeletonGraph;
import de.ipk.ag_ba.image.operations.skeleton.SkeletonProcessor2d;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;

/**
 * Skeletonize the roots and store root lengths, and other parameters.
 * 
 * @author klukas
 */
public class BlRootsSkeletonize extends AbstractSnapshotAnalysisBlock {
	boolean debug = false;
	
	@Override
	protected Image processVISmask() {
		debug = getBoolean("debug", false);
		int background = optionsAndResults.getBackground();
		
		ImageOperation img = null;
		{
			Image i = input().masks().vis();
			if (i == null)
				return null;
			img = i.io().closing();
		}
		ResultsTableWithUnits rt = new ResultsTableWithUnits();
		rt.incrementCounter();
		ImageOperation origImage = img.copy();
		img = img.binary(0, Color.WHITE.getRGB());
		
		ImageOperation inDilatedForSectionDetection = img.copy().bm().dilate(getInt("Dilate for section detection", 1)).io()
				.show("Dilated image for section detection", debug);
		
		ClusterDetection cd = new ClusterDetection(inDilatedForSectionDetection.getImage(), ImageOperation.BACKGROUND_COLORint);
		cd.detectClusters();
		int clusters = cd.getClusterCount();
		rt.addValue("roots.part.count", clusters);
		ArrayList<RunnableOnImage> postProcessing = new ArrayList<RunnableOnImage>();
		img = skeletonizeImage("", background, img, origImage, rt, postProcessing);
		
		int[] unchangedSkeletonPixels = img.getAs1D();
		
		ImageOperation ioClusteredSkeltonImage = new Image(
				inDilatedForSectionDetection.getWidth(),
				inDilatedForSectionDetection.getHeight(),
				cd.getImageClusterIdMask()).io();
		
		ArrayList<Color> cols = Colors.get(clusters + 1);
		for (int i = 1; i < cols.size(); i++) {
			ioClusteredSkeltonImage = ioClusteredSkeltonImage.replaceColor(i, cols.get(i).getRGB());
		}
		
		int[] clusterIDsPixels = ioClusteredSkeltonImage.getAs1D();
		
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
			
			ioClusteredSkeltonImage = new ImageOperation(clusterIDsPixels, ioClusteredSkeltonImage.getWidth(), ioClusteredSkeltonImage.getHeight());
		}
		ioClusteredSkeltonImage.show("CLUSTERS", false);
		
		getResultSet().storeResults("RESULT_", rt, getBlockPosition());
		Image ress = ioClusteredSkeltonImage.bm().dilate(getInt("Dilate for section detection", 5)).getImage();
		for (RunnableOnImage roi : postProcessing) {
			getResultSet().addImagePostProcessor(CameraType.VIS, roi, null);
		}
		ress.show("res", debug);
		return ress;
	}
	
	private void graphAnalysis(int[] optClusterIDsPixels, ImageOperation in, ResultsTableWithUnits rt, String resultPrefix,
			boolean isThinnedImage, int[][] optDistanceMap, ArrayList<RunnableOnImage> postProcessing)
			throws Exception {
		boolean graphAnalysis = getBoolean("Calculate Graph Diameters", false);
		if (graphAnalysis) {
			SkeletonProcessor2d skel = new SkeletonProcessor2d(in.getImage());
			skel.background = SkeletonProcessor2d.getDefaultBackground();
			skel.createEndpointsAndBranchesLists();
			// skel.getAsFlexibleImage().show("BOBOBO");
			// int nEndLimbs = skel.endlimbs.size();
			// rt.addValue("roots" + resultPrefix + ".skeleton.endlimbs", nEndLimbs);
			in = skel.getImageOperation();
			SkeletonGraph sg = new SkeletonGraph(in.getWidth(), in.getHeight(), skel.skelImg);
			sg.createGraph(optClusterIDsPixels, optDistanceMap, 0, postProcessing, getInt("Remove leaf segments shorter than", 20));
			new ImageOperation(optClusterIDsPixels, in.getWidth(), in.getHeight())
					// .debugPrintValueSetToConsole()
					.debugIntToGrayScale().bm().dilate(5).io();
			
			if (sg.getGraph().getNumberOfNodes() > 0) {
				rt.addValue("roots" + resultPrefix + ".graph.leafnodes", GraphHelper.getLeafNodesUndir(sg.getGraph()).size());
				rt.addValue("roots" + resultPrefix + ".graph.nodes", sg.getGraph().getNumberOfNodes());
				rt.addValue("roots" + resultPrefix + ".graph.edges", sg.getGraph().getNumberOfEdges());
				{
					DescriptiveStatistics limbs = new DescriptiveStatistics();
					for (Edge e : sg.getGraph().getEdges()) {
						Double val = e.getDouble("len");
						if (val != null) {
							limbs.addValue(val / 2);
						}
					}
					if (resultPrefix.isEmpty())
						if (limbs.getN() > 0) {
							rt.addValue("roots" + resultPrefix + ".graph.edges.length.average", limbs.getMean());
							rt.addValue("roots" + resultPrefix + ".graph.edges.length.stddev", limbs.getStandardDeviation());
							rt.addValue("roots" + resultPrefix + ".graph.edges.length.skewness", limbs.getSkewness());
							rt.addValue("roots" + resultPrefix + ".graph.edges.length.kurtosis", limbs.getKurtosis());
						}
				}
				rt.addValue("roots" + resultPrefix + ".graph.analysis_performed", 1);
				HashMap<Integer, Double> id2size = getBoolean("Diameter Calculation Limit to Thick to Thin", true) ?
						sg.calculateDiameterThickToThin(getBoolean("Debug - Save Graphs to Files", false), isThinnedImage, postProcessing, rt,
								!getBoolean("Diameter Calculation Limit to Thick to Thin Disable Edge Traversal Veto", true)) :
						sg.calculateDiameter(getBoolean("Debug - Save Graphs to Files", false), postProcessing, isThinnedImage);
				HashMap<Integer, Integer> co2i = new HashMap<Integer, Integer>();
				int idx = 1;
				ArrayList<Double> sizeList = new ArrayList<Double>(id2size.size() - 1);
				for (Integer id : id2size.keySet()) {
					if (id > 0)
						sizeList.add(id2size.get(id));
				}
				Collections.sort(sizeList);
				rt.addValue("roots" + resultPrefix + ".graph.parts", id2size.size() - 1); // the largest graph is added twice (once more with id -1)
				for (Integer id : id2size.keySet()) {
					if (id2size.get(id) <= 0)
						continue;
					if (id != -1 && !co2i.containsKey(id))
						co2i.put(id, idx++);
					int niceID = id;
					if (id != -1)
						niceID = co2i.get(id);
					if (niceID >= 0) {
						double sz = sizeList.remove(sizeList.size() - 1);// id2size.get(id) / 2;
						if (resultPrefix.isEmpty()) {
							rt.addValue("roots" + resultPrefix + ".graph.diameter_part." + StringManipulationTools.formatNumber(niceID, "00") + ".length",
									sz);
						}
					} else {
						double sz = id2size.get(-1) / 2;
						rt.addValue("roots" + resultPrefix + ".graph.diameter.max", sz);
					}
				}
			}
		}
	}
	
	private ImageOperation skeletonizeImage(
			String pre,
			int background, ImageOperation img, ImageOperation nonBinaryImage,
			ResultsTableWithUnits rt, ArrayList<RunnableOnImage> postProcessing) {
		ImageOperation inp = img;
		int n = inp.countFilledPixels();
		double sumGray = 0;
		
		if (rt != null) {
			
			rt.addValue(pre + "roots.filled.pixels", n);
			
			for (int p : nonBinaryImage.getAs1D()) {
				if (p == ImageOperation.BACKGROUND_COLORint)
					continue;
				int bf = (p & 0x0000ff);
				double weight = 255 - bf; // white weight = 0, black weight = 1
				sumGray += weight / 255d;
			}
			rt.addValue(pre + "roots.filled.gray_scale_sum", sumGray);
			if (n > 0) {
				rt.addValue(pre + "roots.filled.avg_gray", sumGray / n);
			}
		}
		ImageOperation binary = inp;// .binary(Color.BLACK.getRGB(), background);
		{
			ImageOperation image = binary.copy();
			if (rt != null)
				calculateWidthHistogram(rt, image, nonBinaryImage, postProcessing);
		}
		
		inp = binary.skeletonize().show("INPUT FOR BRANCH DETECTION", debug);
		
		int len = inp.countFilledPixels();
		
		if (rt != null)
			rt.addValue(pre + "roots.skeleton.length", len);
		
		SkeletonProcessor2d skel = new SkeletonProcessor2d(inp.show("INPUT FOR SKEL", debug).getImage()); // getInvert(
		skel.createEndpointsAndBranchesLists(postProcessing);
		
		img = skel.getImageOperation().show("THE SKELETON", debug);
		
		ArrayList<Point> branchPoints = skel.getBranches();
		ArrayList<Point> eps = skel.getEndpoints();
		if (rt != null)
			rt.addValue(pre + "roots.skeleton.branchpoints", branchPoints.size());
		if (rt != null)
			rt.addValue(pre + "roots.skeleton.endpoints", eps.size());
		if (rt != null && len > 0)
			rt.addValue(pre + "roots.width.area_based.average", n / (double) len);
		if (rt != null)
			rt.addValue(pre + "roots.width.fractional.average", sumGray / len);
		return img;
	}
	
	private void calculateWidthHistogram(ResultsTableWithUnits rt, ImageOperation image,
			ImageOperation nonBinaryImage,
			ArrayList<RunnableOnImage> postProcessing) {
		HashMap<Integer, Integer> width2len = new HashMap<Integer, Integer>();
		// double area = 80;
		int width = 1;
		int pixelCnt;
		double scale = 2;
		image = image.resize(scale);
		nonBinaryImage = nonBinaryImage.resize(scale);
		do {
			ImageOperation sk = image.copy().skeletonize();
			
			pixelCnt = sk.countFilledPixels();
			if (pixelCnt > 0) {
				if (width < 4) {
					String prefix = "";
					if (width > 1)
						prefix = ".thinned" + StringManipulationTools.formatNumber(width - 1, "00");
					if (getBoolean("Calculate Width-Histogram", true) || getBoolean("Calculate Graph Diameters", false)) {
						
						try {
							ImageOperation tobeSkeletonized = image.copy().bm().dilate().io();
							int[][] distanceMap = null;
							ImageOperation skeletonImage = tobeSkeletonized.skeletonize();// .resize(0.5d);
							if (width < 2) {
								// first image, not thinned
								
								distanceMap = nonBinaryImage.distanceMap(DistanceCalculationMode.INT_DISTANCE_TIMES10_GRAY_YIELDS_FRACTION, scale).getAs2D();
								// skeletonImage.or(tobeSkeletonized.distanceMap(DistanceCalculationMode.DISTANCE_VISUALISATION_GRAY).getImage()).show("DISTANCE MAP",
								// false);
								
								// calculate fraction based volume
								DescriptiveStatistics skelStat = new DescriptiveStatistics();
								for (Vector2i i : skeletonImage.getForegroundPixels())
									skelStat.addValue(distanceMap[i.x][i.y] / 10d / 2d);
								int endTipps = SkeletonProcessor2d.countEndPoints(skeletonImage.copy().getImage());
								rt.addValue("roots.volume.skeleton_dist_based", Math.PI * (1 + skelStat.getMean()) * (1 + skelStat.getMean())
										* (endTipps * skelStat.getMean() + skelStat.getN() / 2d));
								rt.addValue("roots.width.skeleton_based.average", (1 + skelStat.getMean()));
								if (getBoolean("Calculate Graph Diameters", true)) {
									graphAnalysis(getClusterIDarray(image.copy().bm().dilate(5).io()),
											new Image(skeletonImage.getWidth(), skeletonImage.getHeight(),
													skeletonImage.getAs1D())
													.show("input for graph analysis", debug).io(), rt, prefix, width > 1, distanceMap, postProcessing);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				width2len.put(width, pixelCnt);
				image = image.bm().erode().io();
				width += 1;
			}
		} while (pixelCnt > 0);
		width--;
		int over19 = 0;
		double volume = 0;
		for (int w = 1; w <= 20; w++) {
			Integer currentLengthWider = width2len.get(w);
			if (currentLengthWider == null)
				currentLengthWider = 0;
			Integer nextLengthThinner = 0;
			if (w < width) {
				nextLengthThinner = width2len.get(w + 1);
				if (nextLengthThinner == null)
					nextLengthThinner = 0;
			}
			int realLen = currentLengthWider - nextLengthThinner;
			if (realLen < 0) {
				System.out.println(SystemAnalysis.getCurrentTime() + ">WARNING: Negative skeleton length calculated for width " + w + " (set to 0): " + realLen);
				realLen = 0;
			}
			int www = w;// width - w + 1;
			if (www > 19) {
				over19 += realLen;
			} else {
				int displayLen = realLen / 2 + (realLen >= w ? w - 1 : 0);
				if (displayLen == 1)
					displayLen = 0;
				if (getBoolean("Calculate Width-Histogram", true))
					rt.addValue("roots.skeleton.width." + StringManipulationTools.formatNumber(www, "00") + ".length", displayLen);
				// Volume = Length*Pi*(D/2)^2
				if (displayLen > 0 && www > 0)
					volume += displayLen * Math.PI * www * www;
			}
			nextLengthThinner += currentLengthWider;
		}
		if (over19 > 0) {
			if (getBoolean("Calculate Width-Histogram", true))
				rt.addValue("roots.skeleton.width.20.length", over19 / 2 + 20);
			volume = Double.NaN;
		}
		if (!Double.isNaN(volume))
			rt.addValue("roots.volume.histogram_based", volume);
	}
	
	private int[] getClusterIDarray(ImageOperation img) {
		ClusterDetection cd = new ClusterDetection(img.getImage(), ImageOperation.BACKGROUND_COLORint);
		cd.detectClusters();
		// int clusters = cd.getClusterCount();
		
		ImageOperation ioClusteredSkeltonImage = new Image(
				img.getWidth(),
				img.getHeight(),
				cd.getImageClusterIdMask()).io();// .debugPrintValueSetToConsole();
		
		return ioClusteredSkeltonImage.getAs1D();
	}
	
	// colorize cluster image:
	// ArrayList<Color> cols = Colors.get(clusters + 1);
	// int[] source = ioClusteredSkeltonImage.getImageAs1dArray();
	// int[] target = new int[source.length];
	// for (int i = 1; i < cols.size(); i++) {
	// int search = i;
	// int replace = cols.get(i).getRGB();
	//
	// int idx = 0;
	// for (int v : source) {
	// if (v != search)
	// target[idx++] = v;
	// else
	// target[idx++] = replace;
	// }
	// }
	//
	
	/**
	 * Function to invert skeleton image, invert from class imageoperation does not work
	 * 
	 * @param input
	 * @return
	 */
	private Image getInvert(Image input) {
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
		return new Image(res);
	}
	
	@Override
	public HashSet<CameraType> getCameraInputTypes() {
		HashSet<CameraType> res = new HashSet<CameraType>();
		res.add(CameraType.VIS);
		return res;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		return getCameraInputTypes();
	}
	
	@Override
	public BlockType getBlockType() {
		return BlockType.FEATURE_EXTRACTION;
	}
	
	@Override
	public String getName() {
		return "Root Skeletonization";
	}
	
	@Override
	public String getDescription() {
		return "Skeletonize the roots and store root lengths, and other parameters.";
	}
	
}
