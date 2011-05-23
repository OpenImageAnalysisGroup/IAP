package de.ipk.ag_ba.image.operations;

import ij.measure.ResultsTable;
import ij.plugin.filter.MaximumFinder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;

import org.Vector2d;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;

public class BlueMarkerFinder {
	
	private final FlexibleImage input;
	private ResultsTable resultTable;
	
	public BlueMarkerFinder(FlexibleImage image) {
		this.input = image;
	}
	
	public void findCoordinates(int background) {
		ImageOperation io1 = new ImageOperation(input);
		
		boolean debug = false;
		if (debug)
			resultTable = io1
					.thresholdLAB(0, 255, 0, 255, 10, 120, PhenotypeAnalysisTask.BACKGROUND_COLORint).printImage("nach lab").opening(0, 1)
					.opening(8, 2).printImage("nach opening")
					.convert2Grayscale().printImage("nach gray")
					// .medianFilter8Bit().printImage("nach8bit")
					.threshold(255 / 2, Color.WHITE.getRGB(), Color.BLACK.getRGB()).printImage("nach thresh")
					.findMax(10.0, MaximumFinder.SINGLE_POINTS).printImage("Single Point Search")
					.findMax(10.0, MaximumFinder.LIST).opening(10, 0).printImage("MARKIERT GROESSER")
					.getResultsTable();
		else
			resultTable = io1
					.thresholdLAB(0, 255, 0, 255, 10, 120, PhenotypeAnalysisTask.BACKGROUND_COLORint).opening(0, 1)
					.opening(8, 2)
					.convert2Grayscale()
					// .medianFilter8Bit()
					.threshold(255 / 2, Color.WHITE.getRGB(), Color.BLACK.getRGB())
					.findMax(10.0, MaximumFinder.SINGLE_POINTS)
					.findMax(10.0, MaximumFinder.LIST).opening(10, 0)
					.getResultsTable();
	}
	
	private ArrayList<Vector2d> getCoordinates() {
		ArrayList<Vector2d> result = new ArrayList<Vector2d>();
		
		for (int i = 0; i < resultTable.getCounter(); i++) {
			int a = (int) resultTable.getValueAsDouble(0, i);
			int b = (int) resultTable.getValueAsDouble(1, i);
			
			Vector2d temp = new Vector2d(a, b);
			result.add(temp);
			System.out.println("vectoren" + i + ": " + temp);
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
			System.out.println("INFO: No blue marker coordinates (empty).");
			return result;
		}
		
		Vector2d minLeftmaxRight = searchminLeftmaxRight(coordinatesUnfiltered);
		
		ArrayList<Vector2d> coordinatesLeft = searchLeft(coordinatesUnfiltered,
				minLeftmaxRight.x + (input.getWidth() * 0.05));
		ArrayList<Vector2d> coordinatesRight = searchRight(
				coordinatesUnfiltered, minLeftmaxRight.y - (input.getWidth() * 0.05));
		
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
