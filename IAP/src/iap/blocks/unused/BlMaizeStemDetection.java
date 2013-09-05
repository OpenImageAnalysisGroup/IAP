package iap.blocks.unused;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import org.AttributeHelper;
import org.Colors;
import org.StringManipulationTools;
import org.Vector2d;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Edge;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.operations.segmentation.ClusterDetection;
import de.ipk.ag_ba.image.operations.skeleton.SkeletonGraph;
import de.ipk.ag_ba.image.operations.skeleton.SkeletonProcessor2d;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * Detect the maize stem
 * 
 * @author klukas
 */
public class BlMaizeStemDetection extends AbstractSnapshotAnalysisBlock {
	boolean debug = false;
	
	@Override
	protected Image processVISmask() {
		debug = getBoolean("debug", false);
		int background = options.getBackground();
		String prefix4 = "prefix4";
		ImageOperation img = null;
		{
			Image i = input().masks().vis();
			if (i == null)
				return null;
			img = i.io().closing();
		}
		ResultsTableWithUnits rt = new ResultsTableWithUnits();
		rt.incrementCounter();
		
		img = img.binary(0, Color.WHITE.getRGB());
		
		ImageOperation inDilatedForSectionDetection = img.copy().dilate(10).show("Dilated image for section detection", debug);
		
		ClusterDetection cd = new ClusterDetection(inDilatedForSectionDetection.getImage(), ImageOperation.BACKGROUND_COLORint);
		cd.detectClusters();
		int clusters = cd.getClusterCount();
		rt.addValue(prefix4 + ".part.count", clusters);
		img = skeletonizeImage("", background, img, rt);
		
		int[] unchangedSkeletonPixels = img.getImageAs1dArray();
		
		ImageOperation ioClusteredSkeltonImage = new Image(
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
					rt.addValue(prefix4 + ".part." + StringManipulationTools.formatNumber(cluster, "00") + ".skeleton.length", size.size);
				idx++;
			}
			
			ioClusteredSkeltonImage = new ImageOperation(clusterIDsPixels, ioClusteredSkeltonImage.getWidth(), ioClusteredSkeltonImage.getHeight());
		}
		ioClusteredSkeltonImage.show("CLUSTERS", false);
		
		getProperties().storeResults("RESULT_", rt, getBlockPosition());
		Image ress = ioClusteredSkeltonImage.dilate(20).getImage();
		return ress;
	}
	
	private void graphAnalysis(int[] optClusterIDsPixels, ImageOperation in, ResultsTableWithUnits rt, String resultPrefix) throws Exception {
		boolean graphAnalysis = getBoolean("graqh-based analysis", true);
		if (graphAnalysis) {
			SkeletonProcessor2d skel = new SkeletonProcessor2d(in.getImage());
			skel.background = SkeletonProcessor2d.getDefaultBackground();
			skel.findEndpointsAndBranches(null);
			skel.calculateEndlimbsRecursive();
			int nEndLimbs = skel.endlimbs.size();
			String prefix = "prefix1";
			rt.addValue(prefix + resultPrefix + ".skeleton.endlimbs", nEndLimbs);
			SkeletonGraph sg = new SkeletonGraph(in.getWidth(), in.getHeight(), skel.skelImg);
			sg.createGraph(optClusterIDsPixels, null, 10, null, 0);
			sg.deleteSelfLoops();
			sg.removeParallelEdges();
			sg.getGraph().numberGraphElements();
			// double minSegmentLen = getDouble("minimum stem segment len", 100);
			if (sg.getGraph().getNumberOfNodes() > 0) {
				rt.addValue(prefix + resultPrefix + ".graph.nodes", sg.getGraph().getNumberOfNodes());
				rt.addValue(prefix + resultPrefix + ".graph.edges", sg.getGraph().getNumberOfEdges());
				{
					DescriptiveStatistics limbs = new DescriptiveStatistics();
					for (Edge e : sg.getGraph().getEdges()) {
						Double val = e.getDouble("len");
						if (val != null) {
							limbs.addValue(val);
							if (val > 10) {
								Vector2d sta = new Vector2d(e.getSource().getInteger("x"), e.getSource().getInteger("y"));
								Vector2d end = new Vector2d(e.getTarget().getInteger("x"), e.getTarget().getInteger("y"));
								double directLen = sta.distance(end);
								if (directLen > 10) {// minSegmentLen) {
									// Krümmung bestimmen
									double kruemmung = 1 - directLen / val; // 0 straight, 1 sloped
									if (sta.y > end.y) {
										Vector2d t = sta;
										sta = end;
										end = t;
									}
									double rotation = sta.angle(end);
									double distToStraightness = Math.abs(rotation - Math.PI / 2);
									AttributeHelper.setLabel(e, "<html>Rotation: " + (int) (rotation / Math.PI * 180) + "<br>"
											+ "Dist. to straight: " + (int) (distToStraightness / Math.PI * 180) + "<br>"
											+ "Kruemmung (0 straight): " + StringManipulationTools.formatNumber(kruemmung, 2));
									AttributeHelper.setAttribute(e, "", "length", val);
									AttributeHelper.setAttribute(e, "", "diststraight", distToStraightness / Math.PI * 180);
									AttributeHelper.setAttribute(e, "", "kruemmung", kruemmung);
									AttributeHelper.setAttribute(e, "", "dist", directLen);
								}
							}
						}
					}
					if (resultPrefix.isEmpty())
						if (limbs.getN() > 0) {
							rt.addValue(prefix + resultPrefix + ".graph.limb.length.average", limbs.getMean());
							rt.addValue(prefix + resultPrefix + ".graph.limb.length.stddev", limbs.getStandardDeviation());
							rt.addValue(prefix + resultPrefix + ".graph.limb.length.skewness", limbs.getSkewness());
							rt.addValue(prefix + resultPrefix + ".graph.limb.length.kurtosis", limbs.getKurtosis());
						}
				}
				MainFrame.getInstance().showGraph(sg.getGraph(), null);
			}
		}
	}
	
	private ImageOperation skeletonizeImage(
			String pre,
			int background, ImageOperation img, ResultsTableWithUnits rt) {
		ImageOperation inp = img.show("INPUT FOR SKEL", debug);
		String prefix2 = "prefix2";
		if (rt != null)
			rt.addValue(pre + prefix2 + ".filled.pixels", inp.countFilledPixels());
		ImageOperation binary = inp.binary(Color.BLACK.getRGB(), background);
		{
			ImageOperation image = binary.copy();
			if (rt != null)
				widthHistogram(rt, image);
		}
		
		inp = binary.skeletonize(false).show("INPUT FOR BRANCH DETECTION", debug);
		
		if (rt != null)
			rt.addValue(pre + prefix2 + ".skeleton.length", inp.countFilledPixels());
		
		SkeletonProcessor2d skel = new SkeletonProcessor2d(getInvert(inp.getImage()));
		skel.findEndpointsAndBranches(null);
		
		img = skel.getImageOperation().show("THE SKELETON", debug);
		
		ArrayList<Point> branchPoints = skel.getBranches();
		
		if (rt != null)
			rt.addValue(pre + prefix2 + ".skeleton.branchpoints", branchPoints.size());
		if (rt != null)
			rt.addValue(pre + prefix2 + ".skeleton.endpoints", skel.getEndpoints().size());
		
		return img;
	}
	
	private void widthHistogram(ResultsTableWithUnits rt, ImageOperation image) {
		HashMap<Integer, Integer> width2len = new HashMap<Integer, Integer>();
		// double area = 80;
		int width = 1;
		int pixelCnt;
		image = image.resize(2);
		String prefix3 = "prefix3";
		boolean graphAnalysed = false;
		do {
			pixelCnt = image.copy().skeletonize(false).countFilledPixels();
			if (pixelCnt > 0) {
				if (width < 4) {
					String prefix = "";
					if (width > 1)
						prefix = ".thinned" + StringManipulationTools.formatNumber(width - 1, "00");
					if (getBoolean("Calculate Graph Diameters", true) && !graphAnalysed) {
						try {
							graphAnalysed = true;
							ImageOperation si = image.copy().dilate().skeletonize(true);// .resize(0.5d);
							graphAnalysis(getClusterIDarray(image),
									new Image(si.getWidth(), si.getHeight(),
											si.getImageAs1dArray())
											.show("input for graph analysis", debug).io(), rt, prefix);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				width2len.put(width, pixelCnt);
				image = image.erode();
				width += 1;
			}
		} while (pixelCnt > 0);
		width--;
		int over19 = 0;
		double volume = 0;
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
			if (www > 19) {
				over19 += realLen;
				volume = Double.NaN;
			} else {
				rt.addValue(prefix3 + ".skeleton.width." + StringManipulationTools.formatNumber(www, "00") + ".length", realLen);
				// Volume = Length*Pi*(D/2)^2
				if (realLen > 0 && www > 0)
					volume += realLen * Math.PI * (www / 2d) * (www / 2d);
			}
			subtract += len;
		}
		if (over19 > 0)
			rt.addValue(prefix3 + ".skeleton.width.20.length", over19);
		if (!Double.isNaN(volume))
			rt.addValue(prefix3 + ".volume", volume);
	}
	
	private int[] getClusterIDarray(ImageOperation img) {
		ClusterDetection cd = new ClusterDetection(img.getImage(), ImageOperation.BACKGROUND_COLORint);
		cd.detectClusters();
		int clusters = cd.getClusterCount();
		
		ImageOperation ioClusteredSkeltonImage = new Image(
				img.getWidth(),
				img.getHeight(),
				cd.getImageClusterIdMask()).io();
		
		ArrayList<Color> cols = Colors.get(clusters + 1);
		int[] source = ioClusteredSkeltonImage.getImageAs1dArray();
		int[] target = new int[source.length];
		for (int i = 1; i < cols.size(); i++) {
			int search = i;
			int replace = cols.get(i).getRGB();
			
			int idx = 0;
			for (int v : source) {
				if (v != search)
					target[idx++] = v;
				else
					target[idx++] = replace;
			}
		}
		
		return target;
	}
	
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
}
