package tests.JMP.leaf_clustering;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import javax.vecmath.Point2d;

import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.junit.Test;

import tests.JMP.leaf_clustering.LeafTipMatcher.Vismode;
import tests.JMP.methods.HelperMethods;
import tests.JMP.methods.Statusbar;
import de.ipk.ag_ba.gui.picture_gui.BackgroundThreadDispatcher;
import de.ipk.ag_ba.gui.picture_gui.LocalComputeJob;
import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.structures.Image;

public class TestExtractLeafMovementFromTopImage {
	boolean debug = false;
	static final double pi = 3.14159;
	ArrayList<Point2d> centroidsForEachSnapshot = new ArrayList<Point2d>();
	
	@Test
	public void test() throws Exception {
		final String pathin = "/Schreibtisch/tobaccoTop/images_full";
		String pathout = "/Schreibtisch/tobaccoTop/out/";
		
		String completepath = System.getProperty("user.home") + pathin;
		File directory = new File(completepath);
		int numfolders = directory.list().length;
		File[] includingFolders = directory.listFiles();
		ArrayList<Plant> analyzedPlants = new ArrayList<Plant>();
		
		for (int folder = 0; folder < numfolders; folder++) {
			String tempPath = includingFolders[folder].getAbsolutePath();
			// TODO get last beginning from /
			String id = tempPath.substring(tempPath.length() - 3);
			int days = HelperMethods.getNumOfFilesAbsPath(tempPath, "png");
			final double[] avgLeafMovement = new double[days];
			
			HashMap<Integer, LinkedList<BorderFeature>> tipPositionsForEachDay = calcLeafTipsFromTimeSeries(tempPath, days, avgLeafMovement);
			
			HelperMethods.write(pathout, "out", avgLeafMovement);
			Point2d cen = getAvgCentroid(centroidsForEachSnapshot);
			Plant tempPlant = matchPlant(tipPositionsForEachDay, Integer.MAX_VALUE);
			tempPlant.setID(id);
			tempPlant.calcMovementForEachLeaf(cen);
			tempPlant.saveLeafCoordinates(pathout);
			tempPlant.saveLeafTipFeature(pathout, "disttocenter");
			Thread.sleep(1000000);
			analyzedPlants.add(tempPlant);
		}
	}
	
	private Point2d getAvgCentroid(ArrayList<Point2d> centroidsForEachSnapshot2) {
		Point2d sum = new Point2d();
		for (Point2d v : centroidsForEachSnapshot2) {
			sum.x += v.x;
			sum.y += v.y;
		}
		int s = centroidsForEachSnapshot2.size();
		return new Point2d(sum.x / s, sum.y / s);
	}
	
	private Plant matchPlant(HashMap<Integer, LinkedList<BorderFeature>> tipPositionsForEachDay, int minDist) throws InterruptedException {
		LeafTipMatcher ltm = new LeafTipMatcher(tipPositionsForEachDay.values());
		ltm.setMinDist(minDist);
		ltm.matchLeafTips();
		// TODO get dim
		ltm.draw(Vismode.PERDAY, 1000, 1300);
		ltm.draw(Vismode.PERLEAF, 1000, 1300);
		return ltm.getMatchedPlant();
	}
	
	private HashMap<Integer, LinkedList<BorderFeature>> calcLeafTipsFromTimeSeries(final String pathin, int days, final double[] avgLeafMovement)
			throws InterruptedException {
		final HashMap<Integer, LinkedList<BorderFeature>> tipPositionsForEachDay = new HashMap<Integer, LinkedList<BorderFeature>>();
		ArrayList<LocalComputeJob> wait = new ArrayList<LocalComputeJob>();
		Statusbar stat = new Statusbar(0, days);
		File f = new File(pathin);
		final File[] fileList = f.listFiles();
		Arrays.sort(fileList);
		for (int i = 0; i < days; i++) {
			final int idx = i;
			Runnable r = new Runnable() {
				
				@Override
				public void run() {
					Image img = HelperMethods.readImageAbsPath(fileList[idx].getAbsolutePath());
					img = preProcessImg(img);
					LinkedList<BorderFeature> tipList;
					try {
						tipList = getLeafTipsFromTop(img);
						synchronized (tipPositionsForEachDay) {
							avgLeafMovement[idx] = calcAvgLeafMovement(tipList, img);
							if (tipList != null)
								tipPositionsForEachDay.put(idx, tipList);
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			};
			wait.add(BackgroundThreadDispatcher.addTask(r, "t" + i));
			
			stat.update(wait);
		}
		BackgroundThreadDispatcher.waitFor(wait);
		return tipPositionsForEachDay;
	}
	
	private double calcAvgLeafMovement(LinkedList<BorderFeature> tipList, Image img) {
		Point2d temp = img.io().getCentroidAsPoint2d(ImageOperation.BACKGROUND_COLORint);
		centroidsForEachSnapshot.add(temp);
		Vector2D CoG = new Vector2D(temp.x, temp.y);
		double distanceAvg = 0.0;
		
		for (BorderFeature p : tipList) {
			if (p != null) {
				Vector2D vtemp = p.getPosition();
				distanceAvg += CoG.distance(vtemp);
			}
		}
		
		return distanceAvg / tipList.size();
	}
	
	private LinkedList<BorderFeature> getLeafTipsFromTop(Image img) throws InterruptedException {
		BorderAnalysis ba = new BorderAnalysis(img);
		int searchRadius = 38;
		int geometricThresh = (int) (0.35 * (pi * searchRadius * searchRadius));
		ba.calcSUSAN(searchRadius, geometricThresh);
		ba.getPeaksFromBorder(2, 10, "susan");
		ba.approxDirection(searchRadius * 2);
		return ba.getPeakList();
	}
	
	private Image preProcessImg(Image img) {
		Integer[] values = { 0, 254, 0, 120, 124, 254 };
		Image mask = img.io()
				.filterRemoveLAB(values, ImageOperation.BACKGROUND_COLORint, false)
				.bm()
				.dilate(14)
				.erode(12)
				.getImage();
		return img.io().applyMask(mask).getImage();
	}
}
