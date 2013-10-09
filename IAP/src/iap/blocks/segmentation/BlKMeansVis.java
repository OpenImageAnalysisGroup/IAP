package iap.blocks.segmentation;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;

import javax.vecmath.Vector2f;

import org.StringManipulationTools;

import de.ipk.ag_ba.image.operation.FeatureVector;
import de.ipk.ag_ba.image.operation.SumFeatures;
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
				seedColors.add(col);
				seedPositions.add(new Vector2f(0.5f, 0.5f));
				clusterColors.add(foreground ? col : Color.WHITE);
			}
			
			clusterColors.add(Color.WHITE);
			clusterColors.add(Color.WHITE);
			
			if (debugValues) {
				clusterColors = seedColors;
			}
			
			double epsilon = getDouble("epsilon", 0.01);
			
			res = kMeans(inp, seedColors, seedPositions, clusterColors, epsilon);
			
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
	
	private Image kMeans(Image img, ArrayList<Color> seedColors, ArrayList<Vector2f> seedPositions, ArrayList<Color> clusterColors, double epsilon) {
		// get feature vector (norm everything between 0 -1)
		int[] img1d = img.copy().getAs1A();
		int w = img.getWidth();
		int h = img.getHeight();
		
		FeatureVector[] measurements = getFeaturesFromImage(img1d, w, h);
		
		// create initials center
		ArrayList<FeatureVector> centerPoints = new ArrayList<FeatureVector>();
		
		for (int i = 0; i < seedColors.size(); i++) {
			centerPoints.add(new FeatureVector(seedColors.get(i), seedPositions.get(i).x, seedPositions.get(i).y));
		}
		
		// run optimization
		ArrayList<SumFeatures> distclasses = new ArrayList<SumFeatures>();
		
		for (int i = 0; i < centerPoints.size(); i++) {
			distclasses.add(new SumFeatures(centerPoints.get(0).numFeatures.size()));
		}
		
		boolean run = true;
		
		while (run) {
			for (int aa = 0; aa < measurements.length; aa++) {
				FeatureVector i = measurements[aa];
				double mindist = Double.MAX_VALUE;
				
				int minidx = -1;
				int idx = 0;
				for (FeatureVector cp : centerPoints) {
					double tempdist = i.euclidianDistance(cp);
					
					if (tempdist < mindist) {
						mindist = tempdist;
						minidx = idx;
					}
					idx++;
				}
				i.acCluster = minidx;
				distclasses.get(minidx).sumUp(i);
			}
			
			ArrayList<FeatureVector> newCenterPoints = new ArrayList<FeatureVector>();
			for (SumFeatures so : distclasses) {
				newCenterPoints.add(new FeatureVector(so));
			}
			
			run = false;
			
			for (int i = 0; i < newCenterPoints.size(); i++) {
				double dist = newCenterPoints.get(i).euclidianDistance(centerPoints.get(i));
				if (debugValues)
					System.out.print(StringManipulationTools.formatNumber(dist, "###.##") + " ");
				if (dist > epsilon)
					run = true;
			}
			if (debugValues)
				System.out.println();
			if (run)
				centerPoints = newCenterPoints;
		}
		
		int[] result = new int[w * h];
		
		int px = 0;
		for (FeatureVector i : measurements) {
			result[px++] = clusterColors.get(i.acCluster).getRGB();
		}
		
		return new Image(w, h, result);
	}
	
	private FeatureVector[] getFeaturesFromImage(int[] img1d, int w, int h) {
		FeatureVector[] measurements = new FeatureVector[w * h];
		int x = 0, y = 0;
		for (int k = 0; k < img1d.length; k++) {
			
			int rgb = img1d[k];
			
			FeatureVector temp = new FeatureVector(new Color(rgb), x / (float) w, y / (float) h);
			
			measurements[k] = temp;
			x++;
			if (x == w) {
				x = 0;
				y++;
			}
		}
		return measurements;
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
	
}
