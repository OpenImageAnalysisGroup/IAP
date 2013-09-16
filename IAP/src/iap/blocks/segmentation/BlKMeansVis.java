package iap.blocks.segmentation;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;

import java.awt.Color;
import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;

import javax.vecmath.Vector2f;

import org.StringManipulationTools;

import de.ipk.ag_ba.image.operation.FeatureVector;
import de.ipk.ag_ba.image.operation.SumFeatures;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;

public class BlKMeansVis extends AbstractSnapshotAnalysisBlock {
	
	@Override
	protected synchronized Image processVISmask() {
		Image res = null;
		boolean debug = getBoolean("debug", false);
		
		if (input().masks().vis() != null) {
			Image inp = input().masks().vis();
			
			ArrayList<Color> seedColors = new ArrayList<Color>();
			ArrayList<Color> clusterColors = new ArrayList<Color>();
			ArrayList<Vector2f> seedPositions = new ArrayList<Vector2f>();
			
			seedPositions.add(new Vector2f(0.5f, 0.5f));
			seedPositions.add(new Vector2f(0.5f, 0.5f));
			seedPositions.add(new Vector2f(0.5f, 0.5f));
			seedPositions.add(new Vector2f(0.5f, 0.5f));
			
			Integer[] back = getIntArray("color 1 (background)", new Integer[] { 255, 255, 255 });
			Integer[] plant = getIntArray("color 1 (plant)", new Integer[] { 0, 255, 0 });
			Integer[] blue = getIntArray("color 1 (blue)", new Integer[] { 0, 0, 255 });
			Integer[] carrier = getIntArray("color 1 (carrier)", new Integer[] { 0, 0, 0 });
			
			seedColors.add(new Color(back[0], back[1], back[2]));
			seedColors.add(new Color(plant[0], plant[1], plant[2]));
			seedColors.add(new Color(blue[0], blue[1], blue[2]));
			seedColors.add(new Color(carrier[0], carrier[1], carrier[2]));
			
			clusterColors.add(Color.WHITE);
			clusterColors.add(Color.GREEN);
			clusterColors.add(Color.WHITE);
			clusterColors.add(Color.WHITE);
			
			double epsilon = getDouble("epsilon", 0.01);
			
			try {
				res = KMEans(inp, seedColors, seedPositions, clusterColors, epsilon);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | IntrospectionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			res.show("segres", debug);
			
			res = inp.io().applyMask(res).getImage();
		}
		return res;
	}
	
	private Image KMEans(Image img, ArrayList<Color> seedColors, ArrayList<Vector2f> seedPositions, ArrayList<Color> clusterColors, double epsilon)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, IntrospectionException {
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
				System.out.print(StringManipulationTools.formatNumber(dist, "###.##") + " ");
				if (dist > epsilon)
					run = true;
			}
			
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
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public HashSet<CameraType> getCameraOutputTypes() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public BlockType getBlockType() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String getName() {
		return "Segmentation Visible (K-Means)";
	}
	
	@Override
	public String getDescription() {
		return "Segmentation based on the k-means clustering.";
	}
	
}
