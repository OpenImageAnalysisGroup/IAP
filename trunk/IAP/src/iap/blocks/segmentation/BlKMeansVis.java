package iap.blocks.segmentation;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;

import org.StringManipulationTools;

import de.ipk.ag_ba.image.color.ColorUtil;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

/**
 * @author pape, klukas
 */
public class BlKMeansVis extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected Image processVISmask() {
		Image res = null;
		boolean debug = debugValues;
		
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
					Color.getHSBColor(0.0f, 1f, 0.5f),
					Color.getHSBColor(0.1f, 1f, 0.5f),
					Color.getHSBColor(0.2f, 1f, 0.5f),
					Color.getHSBColor(0.3f, 1f, 0.5f),
					Color.getHSBColor(0.4f, 1f, 0.5f),
					Color.getHSBColor(0.6f, 1f, 0.5f),
					Color.getHSBColor(0.8f, 1f, 0.5f),
					Color.getHSBColor(1.0f, 1f, 0.5f)
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
			
			res = kMeans(inp.copy().io().blur(getDouble("Blur", 0)).getImage(), seedColors, clusterColors, epsilon, initClusters);
			
			// res.show("segres", true);
			
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
		
		double[] centerPoints_l = new double[seedColors.size()];
		double[] centerPoints_a = new double[seedColors.size()];
		double[] centerPoints_b = new double[seedColors.size()];
		int seed_idx = 0;
		for (Color c : seedColors) {
			int rgb = c.getRGB();
			int red = ((rgb >> 16) & 0xff);
			int green = ((rgb >> 8) & 0xff);
			int blue = (rgb & 0xff);
			
			centerPoints_l[seed_idx] = lc[red][green][blue];
			centerPoints_a[seed_idx] = lc[red][green][blue + 256];
			centerPoints_b[seed_idx] = lc[red][green][blue + 512];
			
			seed_idx++;
		}
		
		double[] distclasses_l = new double[centerPoints_l.length];
		double[] distclasses_a = new double[centerPoints_a.length];
		double[] distclasses_b = new double[centerPoints_b.length];
		
		boolean run = true;
		double[] new_center_l = new double[centerPoints_a.length];
		double[] new_center_a = new double[centerPoints_a.length];
		double[] new_center_b = new double[centerPoints_b.length];
		int[] measurements_acCluster = new int[w * h];
		
		while (run) {
			for (int i = 0; i < distclasses_a.length; i++) {
				distclasses_l[i] = 0f;
				distclasses_a[i] = 0f;
				distclasses_b[i] = 0f;
			}
			int[] n_ab = new int[distclasses_a.length];
			for (int pix_idx = 0; pix_idx < w * h; pix_idx++) {
				int rgb = img1d[pix_idx];
				if (rgb == ImageOperation.BACKGROUND_COLORint)
					continue;
				int red = ((rgb >> 16) & 0xff);
				int green = ((rgb >> 8) & 0xff);
				int blue = (rgb & 0xff);
				
				double img_l = lc[red][green][blue];
				double img_a = lc[red][green][blue + 256];
				double img_b = lc[red][green][blue + 512];
				
				double mindist = Double.MAX_VALUE;
				
				int minidx = -1;
				
				for (int idx_cp = 0; idx_cp < centerPoints_a.length; idx_cp++) {
					// double cp_l = centerPoints_l[idx_cp] - img_l;
					// double cp_a = centerPoints_a[idx_cp] - img_a;
					// double cp_b = centerPoints_b[idx_cp] - img_b;
					
					double tempdist = ColorUtil.deltaE2000(centerPoints_l[idx_cp], centerPoints_a[idx_cp], centerPoints_b[idx_cp],
							img_l, img_a, img_b);
					// double tempdist = (1 - Math.abs(127 - cp_l) / 127) * cp_l + (Math.abs(127 - cp_l) / 127) * (cp_a * cp_a + cp_b * cp_b);
					
					if (tempdist < mindist) {
						mindist = tempdist;
						minidx = idx_cp;
					}
				}
				measurements_acCluster[pix_idx] = minidx;
				distclasses_l[minidx] += img_l;
				distclasses_a[minidx] += img_a;
				distclasses_b[minidx] += img_b;
				n_ab[minidx]++;
			}
			
			for (int i = 0; i < new_center_a.length; i++) {
				if (n_ab[i] == 0) {
					new_center_l[i] = distclasses_l[i];
					new_center_a[i] = distclasses_a[i];
					new_center_b[i] = distclasses_b[i];
				} else {
					new_center_l[i] = distclasses_l[i] / n_ab[i];
					new_center_a[i] = distclasses_a[i] / n_ab[i];
					new_center_b[i] = distclasses_b[i] / n_ab[i];
				}
			}
			
			run = false;
			
			if (initClusters) {
				for (int i = 0; i < new_center_a.length; i++) {
					// double ncpd_l = new_center_l[i] - centerPoints_l[i];
					// double ncpd_a = new_center_a[i] - centerPoints_a[i];
					// double ncpd_b = new_center_b[i] - centerPoints_b[i];
					//
					// double dist = ncpd_l * ncpd_l + ncpd_a * ncpd_a + ncpd_b * ncpd_b;
					
					double dist = ColorUtil.deltaE2000(new_center_l[i], new_center_a[i], new_center_b[i],
							centerPoints_l[i], centerPoints_a[i], centerPoints_b[i]);
					
					if (true || debugValues)
						System.out.print(StringManipulationTools.formatNumber(dist, "###.#####") + " ");
					if (dist > epsilon) {
						run = true;
						break;
					}
				}
				if (true || debugValues)
					System.out.println();
				centerPoints_l = new_center_l;
				centerPoints_a = new_center_a;
				centerPoints_b = new_center_b;
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
			for (int i = 0; i < centerPoints_a.length; i++) {
				if (Double.isNaN(centerPoints_a[i]) || Double.isNaN(centerPoints_b[i]))
					cl.add(ImageOperation.BACKGROUND_COLOR);
				else
					cl.add(clusterColors.get(i));
			}
			for (int i = 0; i < centerPoints_a.length; i++) {
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
