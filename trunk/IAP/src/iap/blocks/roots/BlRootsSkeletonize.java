package iap.blocks.roots;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlockFIS;

import java.awt.Color;
import java.awt.Point;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.Colors;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.algorithm.ThreadSafeOptions;
import org.graffiti.plugins.ios.exporters.gml.GMLWriter;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.operations.segmentation.ClusterDetection;
import de.ipk.ag_ba.image.operations.skeleton.SkeletonGraph;
import de.ipk.ag_ba.image.operations.skeleton.SkeletonProcessor2d;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.image.structures.FlexibleImageType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.algorithms.shortest_paths.WeightedShortestPathSelectionAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.NodeHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.AttributePathNameSearchType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchType;

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
		FlexibleImage img = input().masks().vis();
		if (img != null) {
			ResultsTableWithUnits rt = new ResultsTableWithUnits();
			rt.incrementCounter();
			
			FlexibleImage inImage = img.copy();
			
			img = processRootsInVisibleImage("", background, img, rt);
			
			{
				ImageOperation in = inImage.copy().io().binary(0, Color.WHITE.getRGB()).dilate(10).show("Dilated image for section detection", debug);
				
				boolean graphAnalysis = getBoolean("graqh-based analysis", true);
				if (graphAnalysis) {
					SkeletonProcessor2d skel = new SkeletonProcessor2d(inImage.copy().io().binary(0, Color.WHITE.getRGB()).skeletonize(true)
							.show("skeleton IN for graph", debug).copy().getImage());
					skel.background = -1;
					skel.findEndpointsAndBranches();
					new FlexibleImage(skel.skelImg).copy().show("calculated skeleton for Graph analysis", debug);
					SkeletonGraph sg = new SkeletonGraph(in.getWidth(), in.getHeight(), skel.skelImg);
					sg.createGraph();
					sg.deleteSelfLoops();
					ThreadSafeOptions optLengthReturn = new ThreadSafeOptions();
					List<GraphElement> elem = WeightedShortestPathSelectionAlgorithm.findLongestShortestPathElements(
							sg.getGraph().getGraphElements(),
							new AttributePathNameSearchType("", "len", SearchType.searchDouble, "len"),
							optLengthReturn);
					rt.addValue("roots.skeleton.maximum.minimum.length", optLengthReturn.getDouble());
					
					boolean createDebugGML = false;
					if (createDebugGML) {
						for (GraphElement ge : elem) {
							if (ge instanceof Node) {
								new NodeHelper((Node) ge).setFillColor(Color.YELLOW)
										.setAttributeValue("shortest_path", "maxlen", optLengthReturn.getDouble());
							}
						}
						GMLWriter gw = new GMLWriter();
						try {
							gw.write(new FileOutputStream(ReleaseInfo.getDesktopFolder() + "/skel_root.gml"), sg.getGraph());
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					// skel2d.connectSkeleton();
					// skel2d.findTrailWithMaxBranches();
					
				}
				
				ClusterDetection cd = new ClusterDetection(in.getImage(), ImageOperation.BACKGROUND_COLORint);
				cd.detectClusters();
				int clusters = cd.getClusterCount();
				rt.addValue("roots.part.count", clusters);
				
				int[] rootPixels = img.getAs1A();
				
				ImageOperation io = new FlexibleImage(in.getWidth(), in.getHeight(), cd.getImageClusterIdMask()).io();
				
				ArrayList<Color> cols = Colors.get(clusters + 1);
				for (int i = 1; i < cols.size(); i++) {
					io = io.replaceColor(i, cols.get(i).getRGB());
				}
				
				img = io.getImage();
				
				int[] clusterIDsPixels = io.getImageAs1dArray();
				
				boolean thinClusterSkel = true;
				if (thinClusterSkel) {
					for (int i = 0; i < rootPixels.length; i++)
						if (rootPixels[i] == -16777216)
							clusterIDsPixels[i] = background;
					
					HashMap<Integer, Integer> color2clusterId = new HashMap<Integer, Integer>();
					int[] clusterSize = new int[clusters + 1];
					int idx = 0;
					for (int p : clusterIDsPixels) {
						if (!color2clusterId.containsKey(p))
							color2clusterId.put(p, idx++);
						clusterSize[color2clusterId.get(p)] += 1;
					}
					
					TreeSet<Integer> sizes = new TreeSet<Integer>();
					for (int s : clusterSize)
						sizes.add(s);
					idx = 0;
					for (Integer size : sizes) {
						int cluster = clusters - idx;
						// first cluster is background, is ignored
						if (cluster != 0)
							rt.addValue("roots.part." + StringManipulationTools.formatNumber(cluster, "00") + ".skeleton.length", size);
						idx++;
					}
					
					io = new ImageOperation(clusterIDsPixels, io.getWidth(), io.getHeight());
				}
				io.show("CLUSTERS", false);
			}
			
			getProperties().storeResults("RESULT_", rt, getBlockPosition());
		}
		return img;
	}
	
	private FlexibleImage processRootsInVisibleImage(
			String pre,
			int background, FlexibleImage img, ResultsTableWithUnits rt) {
		ImageOperation inp = img.io().show("INPUT FOR SKEL", debug);
		
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
		
		img = skel.getAsFlexibleImage().show("THE SKELETON", debug);
		
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
