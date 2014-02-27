package iap.blocks.segmentation;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;

import javax.vecmath.Vector2f;

import org.StringManipulationTools;

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
			ArrayList<Vector2f> seedPositions = new ArrayList<Vector2f>();
			
			Color[] initColor = new Color[] {
					new Color(78, 117, 41),// plant leaf green
					new Color(65, 56, 41),// dark green
					new Color(255, 255, 160),// bright yellow (flower)
					new Color(154, 116, 88),// maize stem
					Color.WHITE, // white background wall
					new Color(77, 108, 157), // from blue plant support
					new Color(41, 37, 42), // dark foil
					new Color(50, 50, 50) // pot / soil / foil color
			};
			
			int n = getInt(getSettingsNameForSeedColorCount(), initColor.length);
			for (int i = 0; i < n; i++) {
				Color col = getColor(getSettingsNameForSeedColor(i), (i + 1) < initColor.length ? initColor[i] : Color.BLACK);
				boolean foreground = getBoolean(getSettingsNameForForeground(i), (i + 1) < 5);
				// System.out.println("N=" + i + ", FG?=" + foreground);
				seedColors.add(col);
				seedPositions.add(new Vector2f(0.5f, 0.5f));
				clusterColors.add(foreground ? col : ImageOperation.BACKGROUND_COLOR);
			}
			
			if (debugValues) {
				clusterColors = seedColors;
			}
			
			float epsilon = (float) getDouble("epsilon", 0.001);
			
			res = kMeans(inp.copy().io().blur(getDouble("Blur", 0)).getImage(), seedColors, seedPositions, clusterColors, epsilon);
			
			res.show("segres", debug);
			
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
	
	private Image kMeans(Image img, ArrayList<Color> seedColors, ArrayList<Vector2f> seedPositions, ArrayList<Color> clusterColors, float epsilon) {
		int[] img1d = img.getAs1A();
		int w = img.getWidth();
		int h = img.getHeight();
		
		float[][][] lc = ImageOperation.getLabCubeInstance();
		
		double[] centerPoints_a = new double[seedColors.size()];
		double[] centerPoints_b = new double[seedColors.size()];
		int seed_idx = 0;
		for (Color c : seedColors) {
			int rgb = c.getRGB();
			int red = ((rgb >> 16) & 0xff);
			int green = ((rgb >> 8) & 0xff);
			int blue = (rgb & 0xff);
			
			centerPoints_a[seed_idx] = lc[red][green][blue + 256];
			centerPoints_b[seed_idx] = lc[red][green][blue + 512];
			
			seed_idx++;
		}
		
		double[] distclasses_a = new double[centerPoints_a.length];
		double[] distclasses_b = new double[centerPoints_b.length];
		
		boolean run = true;
		double[] new_center_a = new double[centerPoints_a.length];
		double[] new_center_b = new double[centerPoints_b.length];
		int[] measurements_acCluster = new int[w * h];
		
		while (run) {
			for (int i = 0; i < distclasses_a.length; i++) {
				distclasses_a[i] = 0f;
				distclasses_b[i] = 0f;
			}
			int[] n_ab = new int[distclasses_a.length];
			for (int pix_idx = 0; pix_idx < w * h; pix_idx++) {
				int rgb = img1d[pix_idx];
				int red = ((rgb >> 16) & 0xff);
				int green = ((rgb >> 8) & 0xff);
				int blue = (rgb & 0xff);
				
				double img_a = lc[red][green][blue + 256];
				double img_b = lc[red][green][blue + 512];
				
				double mindist = Double.MAX_VALUE;
				
				int minidx = -1;
				
				for (int idx_cp = 0; idx_cp < centerPoints_a.length; idx_cp++) {
					double cp_a = centerPoints_a[idx_cp] - img_a;
					double cp_b = centerPoints_b[idx_cp] - img_b;
					
					double tempdist = cp_a * cp_a + cp_b * cp_b;
					
					if (tempdist < mindist) {
						mindist = tempdist;
						minidx = idx_cp;
					}
				}
				measurements_acCluster[pix_idx] = minidx;
				distclasses_a[minidx] += img_a;
				distclasses_b[minidx] += img_b;
				n_ab[minidx]++;
			}
			
			for (int i = 0; i < new_center_a.length; i++) {
				new_center_a[i] = distclasses_a[i] / n_ab[i];
				new_center_b[i] = distclasses_b[i] / n_ab[i];
			}
			
			run = false;
			
			if (getBoolean(getSettingsNameForLoop(), true)) {
				for (int i = 0; i < new_center_a.length; i++) {
					double ncpd_a = new_center_a[i] - centerPoints_a[i];
					double ncpd_b = new_center_b[i] - centerPoints_b[i];
					
					double dist = ncpd_a * ncpd_a + ncpd_b * ncpd_b;
					
					if (debugValues)
						System.out.print(StringManipulationTools.formatNumber(dist, "###.#####") + " ");
					if (dist > epsilon) {
						run = true;
						break;
					}
				}
				if (debugValues)
					System.out.println();
				if (run) {
					double[] t_a = centerPoints_a;
					double[] t_b = centerPoints_b;
					centerPoints_a = new_center_a;
					centerPoints_b = new_center_b;
					new_center_a = t_a;
					new_center_b = t_b;
				}
			}
		}
		
		int[] result = new int[w * h];
		
		int[] cluCol = new int[clusterColors.size()];
		for (int i = 0; i < clusterColors.size(); i++)
			cluCol[i] = clusterColors.get(i).getRGB();
		for (int i = 0; i < measurements_acCluster.length; i++) {
			result[i] = cluCol[measurements_acCluster[i]];
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
