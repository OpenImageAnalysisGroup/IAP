package de.ipk.ag_ba.image.operations;

import ij.measure.ResultsTable;
import ij.plugin.filter.MaximumFinder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

import org.Vector2d;

import de.ipk.ag_ba.image.analysis.gernally.ImageProcessorOptions.CameraPosition;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;

public class BlueMarkerFinder {
	
	private final FlexibleImage input;
	private ResultsTable resultTable;
	private final double scale;
	private final CameraPosition typ;
	private boolean maize;
	
	public BlueMarkerFinder(FlexibleImage image, double scale, CameraPosition typ, boolean maize) {
		this.input = image;
		this.scale = scale;
		this.typ = typ;
		this.maize = maize;
	}
	
	public void findCoordinates(int background) {
		ImageOperation io1 = new ImageOperation(input);
		double scaleFactor = scale;
		boolean debug = false;
		if (debug)
			resultTable = io1
					// .thresholdLAB(0, 255, 0, 200, 10, 120, PhenotypeAnalysisTask.BACKGROUND_COLORint).printImage("nach lab")
					.thresholdLAB(0, 255, 0, 255, 10, 110, PhenotypeAnalysisTask.BACKGROUND_COLORint, typ,
							maize).printImage("nach lab")
					.opening((int) (0 * scaleFactor), (int) (1 * scaleFactor))
					.opening((int) (8 * scaleFactor), (int) (2 * scaleFactor))
					.printImage("nach opening")
					.convert2Grayscale().printImage("nach gray")
					// .medianFilter8Bit().printImage("nach8bit")
					.threshold(254, Color.WHITE.getRGB(), Color.BLACK.getRGB()).printImage("nach thresh")
					.findMax(10.0, MaximumFinder.SINGLE_POINTS).printImage("Single Point Search")
					.findMax(10.0, MaximumFinder.LIST).opening(10, 0).printImage("MARKIERT GROESSER")
					.getResultsTable();
		else
			resultTable = io1
					.thresholdLAB(0, 255, 0, 255, 10, 110, PhenotypeAnalysisTask.BACKGROUND_COLORint, typ, maize)
					.opening((int) (0 * scaleFactor), (int) (1 * scaleFactor))
					.opening((int) (8 * scaleFactor), (int) (2 * scaleFactor))
					.convert2Grayscale()
					// .medianFilter8Bit()
					.threshold(254, Color.WHITE.getRGB(), Color.BLACK.getRGB())
					// .findMax(10.0, MaximumFinder.SINGLE_POINTS)
					.findMax(10.0, MaximumFinder.LIST)
					.getResultsTable();
	}
	
	private ArrayList<Vector2d> getCoordinates() {
		ArrayList<Vector2d> result = new ArrayList<Vector2d>();
		
		for (int i = 0; i < resultTable.getCounter(); i++) {
			int x = (int) resultTable.getValueAsDouble(0, i);
			int y = (int) resultTable.getValueAsDouble(1, i);
			
			Vector2d temp = new Vector2d(x, y);
			result.add(temp);
		}
		
		return result;
	}
	
	public ArrayList<MarkerPair> getResultCoordinates(int tolerance) {
		
		ArrayList<Vector2d> coordinatesUnfiltered = getCoordinates();
		
		ArrayList<MarkerPair> result = new ArrayList<MarkerPair>();
		
		if (coordinatesUnfiltered == null) {
			System.out.println("ERROR: No blue marker coordinates (null)!");
			return result;
		}
		
		if (coordinatesUnfiltered.isEmpty()) {
			System.out.println("INFO: No blue marker coordinates (empty). Image " + input + " //" + typ + " // " + SystemAnalysisExt.getCurrentTime());
			return result;
		}
		
		Vector2d minLeftmaxRight = searchminLeftmaxRight(coordinatesUnfiltered);
		
		ArrayList<Vector2d> coordinatesLeft = searchLeft(coordinatesUnfiltered,
				minLeftmaxRight.x + (input.getWidth() * 0.2));
		ArrayList<Vector2d> coordinatesRight = searchRight(
				coordinatesUnfiltered, minLeftmaxRight.y - (input.getWidth() * 0.2));
		
		coordinatesLeft = getThree(coordinatesLeft);
		coordinatesRight = getThree(coordinatesRight);
		
		HashSet<Vector2d> added = new HashSet<Vector2d>();
		
		for (int indexLeft = 0; indexLeft < coordinatesLeft.size(); indexLeft++) {
			for (int indexRight = 0; indexRight < coordinatesRight.size(); indexRight++) {
				Vector2d l = coordinatesLeft.get(indexLeft);
				Vector2d r = coordinatesRight.get(indexRight);
				if (!added.contains(l) && !added.contains(r)) {
					if (sameYposition(l, r, tolerance)) {
						added.add(l);
						added.add(r);
						result.add(new MarkerPair(l, r));
					}
				}
			}
		}
		
		for (Vector2d l : coordinatesLeft) {
			if (!added.contains(l)) {
				int index = 0;
				for (MarkerPair mp : result) {
					if (mp.getMinY() >= l.y)
						break;
					else
						index++;
				}
				result.add(index, new MarkerPair(l, null));
			}
		}
		
		for (Vector2d r : coordinatesRight) {
			if (!added.contains(r)) {
				int index = 0;
				for (MarkerPair mp : result) {
					if (mp.getMinY() >= r.y)
						break;
					else
						index++;
				}
				result.add(index, new MarkerPair(null, r));
			}
		}
		
		return result;
	}
	
	private ArrayList<Vector2d> getThree(ArrayList<Vector2d> coordinates) {
		TreeSet<Double> valuesX = new TreeSet<Double>();
		TreeSet<Double> valuesY = new TreeSet<Double>();
		
		for (Vector2d c : coordinates) {
			valuesX.add(c.x);
			valuesY.add(c.y);
		}
		
		double min = valuesY.first();
		double max = valuesY.last();
		double mean = (max - min) / 2 + min;
		double distance = (max - min) / 4;
		
		TreeSet<Double> valuesX1 = new TreeSet<Double>();
		TreeSet<Double> valuesX2 = new TreeSet<Double>();
		TreeSet<Double> valuesX3 = new TreeSet<Double>();
		
		TreeSet<Double> valuesY1 = new TreeSet<Double>();
		TreeSet<Double> valuesY2 = new TreeSet<Double>();
		TreeSet<Double> valuesY3 = new TreeSet<Double>();
		
		for (Vector2d c : coordinates) {
			if (c.y - min < distance) {
				valuesX1.add(c.x);
				valuesY1.add(c.y);
			} else
				if (Math.abs(c.y - mean) < distance) {
					valuesX2.add(c.x);
					valuesY2.add(c.y);
				} else {
					valuesX3.add(c.x);
					valuesY3.add(c.y);
				}
		}
		
		ArrayList<Vector2d> res = new ArrayList<Vector2d>();
		
		if (valuesY3.size() > 0) {
			double mX3 = valuesX3.toArray(new Double[] {})[valuesX3.size() / 2];
			double mY3 = valuesY3.toArray(new Double[] {})[valuesY3.size() / 2];
			res.add(new Vector2d(mX3, mY3));
		}
		
		if (valuesY2.size() > 0) {
			double mX2 = valuesX2.toArray(new Double[] {})[valuesX2.size() / 2];
			double mY2 = valuesY2.toArray(new Double[] {})[valuesY2.size() / 2];
			res.add(new Vector2d(mX2, mY2));
		}
		
		if (valuesY1.size() > 0) {
			double mX1 = valuesX1.toArray(new Double[] {})[valuesX1.size() / 2];
			double mY1 = valuesY1.toArray(new Double[] {})[valuesY1.size() / 2];
			res.add(new Vector2d(mX1, mY1));
		}
		
		return res;
	}
	
	private Vector2d searchminLeftmaxRight(ArrayList<Vector2d> input) {
		int min = Integer.MAX_VALUE;
		int max = 0;
		
		for (int index = 0; index < input.size(); index++) {
			if (input.get(index).x < min)
				min = (int) input.get(index).x;
			if (input.get(index).x > max)
				max = (int) input.get(index).x;
		}
		return new Vector2d(min, max);
	}
	
	private boolean sameYposition(Vector2d l, Vector2d r, int tolerance) {
		return l != null && r != null && Math.abs(l.y - r.y) < tolerance;
	}
	
	public ArrayList<Vector2d> searchRight(ArrayList<Vector2d> coordinates,
			double xLimit) {
		ArrayList<Vector2d> result = new ArrayList<Vector2d>();
		double minX = getMaxX(coordinates);
		
		for (int index = 0; index < coordinates.size(); index++) {
			if (coordinates.get(index).x > xLimit) {
				result.add(coordinates.get(index));
			}
		}
		return result;
	}
	
	private double getMaxX(ArrayList<Vector2d> coordinates) {
		double result = coordinates.get(0).x;
		
		for (int index = 0; index < coordinates.size(); index++) {
			if (coordinates.get(index).x > result) {
				result = coordinates.get(index).x;
			}
		}
		return result;
	}
	
	private ArrayList<Vector2d> searchLeft(ArrayList<Vector2d> coordinates,
			double xLimit) {
		
		ArrayList<Vector2d> result = new ArrayList<Vector2d>();
		
		for (int index = 0; index < coordinates.size(); index++) {
			if (coordinates.get(index).x < xLimit) {
				result.add(coordinates.get(index));
			}
		}
		
		return result;
	}
	
	private double getMinX(ArrayList<Vector2d> coordinates) {
		
		double result = coordinates.get(0).x;
		
		for (int index = 0; index < coordinates.size(); index++) {
			if (coordinates.get(index).x < result) {
				result = coordinates.get(index).x;
			}
		}
		return result;
	}
}
