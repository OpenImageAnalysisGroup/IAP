package iap.blocks.segmentation;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;

import org.StringManipulationTools;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author pape, klukas
 */
public class BlKMeansVis extends AbstractSnapshotAnalysisBlock {
	
	private final boolean[] distanceEnabled = new boolean[6];
	
	@Override
	protected Image processVISmask() {
		Image res = null;
		boolean debug = debugValues;
		
		distanceEnabled[0] = getBoolean("Lightness (L*a*b*)", false);
		distanceEnabled[1] = getBoolean("Green-Magenta (L*a*b*)", true);
		distanceEnabled[2] = getBoolean("Blue-Yellow (L*a*b*)", true);
		distanceEnabled[3] = getBoolean("Hue (HSB)", true);
		distanceEnabled[4] = getBoolean("Saturation (HSB)", true);
		distanceEnabled[5] = getBoolean("Brightness (HSB)", true);
		
		// getProperties().getPropertiesExactMatch(false, true, "top.main.axis");
		// HashMap<String, HashMap<Integer, ArrayList<BlockPropertyValue>>> previousResults = options
		// .getPropertiesExactMatchForPreviousResultsOfCurrentSnapshot("top.main.axis");
		
		if (input().masks().vis() != null) {
			Image inp = input().masks().vis();
			
			inp.show("inp", debug);
			
			ArrayList<Color> seedColors = new ArrayList<Color>();
			ArrayList<Color> clusterColors = new ArrayList<Color>();
			
			boolean initClusters = getBoolean(getSettingsNameForLoop(), true);
			if (initClusters)
				setBoolean(getSettingsNameForLoop(), false);
			
			Color[] initColor = new Color[] {
					Color.getHSBColor(0.25f, 0.25f, 0.5f),
					Color.getHSBColor(0.75f, 0.25f, 0.5f),
					Color.getHSBColor(0.75f, 0.75f, 0.5f),
					Color.getHSBColor(0.75f, 0.75f, 0.75f),
					Color.getHSBColor(0.25f, 0.75f, 0.5f),
					Color.getHSBColor(0.25f, 0.75f, 0.75f),
					Color.getHSBColor(0.25f, 0.25f, 0.75f),
					Color.getHSBColor(0.75f, 0.75f, 0.75f)
			};
			
			if (initClusters) {
				setInt(getSettingsNameForSeedColorCount(), initColor.length);
				for (int i = 0; i < initColor.length; i++)
					setColor(getSettingsNameForSeedColor(i), initColor[i]);
			}
			
			int n = getInt(getSettingsNameForSeedColorCount(), initColor.length);
			for (int i = 0; i < n; i++) {
				Color col = getColor(getSettingsNameForSeedColor(i), (i + 1) < initColor.length ? initColor[i] : Color.BLACK);
				boolean foreground = getBoolean(getSettingsNameForForeground(i), i >= 1 || i <= 4);
				seedColors.add(col);
				clusterColors.add(foreground ? col : ImageOperation.BACKGROUND_COLOR);
			}
			
			if (debugValues) {
				clusterColors = seedColors;
			}
			
			float epsilon = (float) getDouble("epsilon", 0.001);
			
			res = kMeans(inp.copy().io().blurImageJ(getDouble("Blur", 0)).getImage(), seedColors, clusterColors, epsilon, initClusters);
			
			res.show("segres", debug);
			
			if (!getBoolean("Inspect Segmentation Result", false))
				res = inp.io().applyMask(res).getImage();
		}
		return res;
	}
	
	public String getSettingsNameForSeedColor(int i) {
		return "Seed Color " + (i + 1);
	}
	
	public String getSettingsNameForForeground(int i) {
		return "Foreground " + (i + 1);
	}
	
	public String getSettingsNameForSeedColorCount() {
		return "Color Classes";
	}
	
	private Image kMeans(Image img, ArrayList<Color> seedColors, ArrayList<Color> clusterColors, float epsilon,
			boolean initClusters) {
		int[] img1d = img.getAs1A();
		int w = img.getWidth();
		int h = img.getHeight();
		
		float[][][] lc = ImageOperation.getLabCubeInstance();
		
		double[][] centerPoints = new double[seedColors.size()][6];
		float[] hsb = new float[3];
		
		int seed_idx = 0;
		for (Color c : seedColors) {
			int rgb = c.getRGB();
			int red = ((rgb >> 16) & 0xff);
			int green = ((rgb >> 8) & 0xff);
			int blue = (rgb & 0xff);
			
			Color.RGBtoHSB(red, green, blue, hsb);
			
			float[] lci = lc[red][green];
			centerPoints[seed_idx][0] = lci[blue];
			centerPoints[seed_idx][1] = lci[blue + 256];
			centerPoints[seed_idx][2] = lci[blue + 512];
			centerPoints[seed_idx][3] = hsb[0];
			centerPoints[seed_idx][4] = hsb[1];
			centerPoints[seed_idx][5] = hsb[2];
			
			seed_idx++;
		}
		
		double[][] distclasses = new double[centerPoints.length][6];
		
		boolean run = true;
		double[][] new_center = new double[centerPoints.length][6];
		int[] measurements_acCluster = new int[w * h];
		
		while (run) {
			for (int i = 0; i < distclasses.length; i++) {
				for (int off = 0; off < 6; off++)
					distclasses[i][off] = 0;
			}
			int[] n_ab = new int[centerPoints.length];
			for (int pix_idx = 0; pix_idx < w * h; pix_idx++) {
				int rgb = img1d[pix_idx];
				if (rgb == ImageOperation.BACKGROUND_COLORint)
					continue;
				int red = ((rgb >> 16) & 0xff);
				int green = ((rgb >> 8) & 0xff);
				int blue = (rgb & 0xff);
				
				float[] lci = lc[red][green];
				double img_l = lci[blue];
				double img_a = lci[blue + 256];
				double img_b = lci[blue + 512];
				Color.RGBtoHSB(red, green, blue, hsb);
				double hue = hsb[0];
				double sat = hsb[1];
				double val = hsb[2];
				double mindist = Double.MAX_VALUE;
				
				int minidx = -1;
				double[] cp = new double[6];
				for (int idx_cp = 0; idx_cp < centerPoints.length; idx_cp++) {
					if (centerPoints[idx_cp][0] < 0)
						continue;
					cp[0] = centerPoints[idx_cp][0] - img_l;
					cp[1] = centerPoints[idx_cp][1] - img_a;
					cp[2] = centerPoints[idx_cp][2] - img_b;
					cp[3] = centerPoints[idx_cp][3] - hue;
					cp[4] = centerPoints[idx_cp][4] - sat;
					cp[5] = centerPoints[idx_cp][5] - val;
					
					double tempdist = 0d;
					for (int off = 0; off < 6; off++)
						if (distanceEnabled[off])
							tempdist += cp[off] * cp[off];
					
					if (tempdist < mindist) {
						mindist = tempdist;
						minidx = idx_cp;
					}
				}
				measurements_acCluster[pix_idx] = minidx;
				distclasses[minidx][0] += img_l;
				distclasses[minidx][1] += img_a;
				distclasses[minidx][2] += img_b;
				distclasses[minidx][3] += hue;
				distclasses[minidx][4] += sat;
				distclasses[minidx][5] += val;
				n_ab[minidx]++;
			}
			
			for (int i = 0; i < new_center.length; i++) {
				if (n_ab[i] == 0) {
					for (int off = 0; off < 6; off++)
						new_center[i][off] = -1;
				} else {
					for (int off = 0; off < 6; off++)
						new_center[i][off] = distclasses[i][off] / n_ab[i];
				}
			}
			
			run = false;
			
			if (initClusters) {
				for (int i = 0; i < new_center.length; i++) {
					double dist = 0d;
					for (int off = 0; off < 6; off++)
						if (distanceEnabled[off])
							dist += (new_center[i][off] - centerPoints[i][off]) * (new_center[i][off] - centerPoints[i][off]);
					
					if (debugValues)
						System.out.print(StringManipulationTools.formatNumber(dist, "###.#####") + " ");
					if (dist > epsilon) {
						run = true;
						break;
					}
				}
				if (debugValues)
					System.out.println();
				for (int cl = 0; cl < centerPoints.length; cl++)
					for (int off = 0; off < 6; off++)
						centerPoints[cl][off] = new_center[cl][off];
			}
		}
		
		int[] result = new int[w * h];
		
		int[] cluCol = new int[clusterColors.size()];
		for (int i = 0; i < clusterColors.size(); i++)
			cluCol[i] = clusterColors.get(i).getRGB();
		for (int i = 0; i < measurements_acCluster.length; i++) {
			if (img1d[i] == ImageOperation.BACKGROUND_COLORint)
				result[i] = ImageOperation.BACKGROUND_COLORint;
			else
				result[i] = cluCol[measurements_acCluster[i]];
		}
		
		if (initClusters) {
			ArrayList<Color> cl = new ArrayList<Color>();
			for (int i = 0; i < centerPoints.length; i++) {
				boolean noNaN = true;
				for (int off = 0; off < 6; off++)
					if (Double.isNaN(centerPoints[i][off])) {
						noNaN = false;
						break;
					}
				if (!noNaN)
					cl.add(ImageOperation.BACKGROUND_COLOR);
				else {
					// Color r = new Color_CIE_Lab(centerPoints_l[i] / 2.55, centerPoints_a[i] - 127, centerPoints_b[i] - 127).getColor();
					// cl.add(r);
					cl.add(Color.getHSBColor((float) centerPoints[i][3], (float) centerPoints[i][4], (float) centerPoints[i][5]));
				}
			}
			for (int i = 0; i < centerPoints.length; i++) {
				setColor(getSettingsNameForSeedColor(i), cl.get(i));
			}
		}
		
		return new Image(w, h, result);
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
		return BlockType.SEGMENTATION;
	}
	
	@Override
	public String getName() {
		return "Auto-tuning VIS-Segmentation (K-Means)";
	}
	
	@Override
	public String getDescription() {
		return "Segmentation based on the k-means clustering.";
	}
	
	public String getSettingsNameForLoop() {
		return "k-Means optimization loop";
	}
	
}
