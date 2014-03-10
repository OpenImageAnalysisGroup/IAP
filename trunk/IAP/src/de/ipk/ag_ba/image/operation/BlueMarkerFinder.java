package de.ipk.ag_ba.image.operation;

import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;
import ij.measure.ResultsTable;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;

import org.Vector2d;
import org.Vector2i;

import de.ipk.ag_ba.image.structures.Image;

public class BlueMarkerFinder {
	
	boolean debug = false;
	
	private final Image input;
	private ResultsTable resultTable;
	private final CameraPosition typ;
	private final boolean maize;
	private final int inputImageWidth;
	private ImageOperation markerPositionsImage;
	private Vector2i[] regionPositions;
	
	public BlueMarkerFinder(Image image, CameraPosition typ, boolean maize, boolean debug) {
		this.input = image;
		this.typ = typ;
		this.maize = maize;
		this.inputImageWidth = this.input.getWidth();
		this.debug = debug;
	}
	
	public void findCoordinates(int background) {
		ImageOperation io1 = input.io().copy();
		double scaleFactor = 1 / 1.2d;
		int w = io1.getImage().getWidth();
		int h = io1.getImage().getHeight();
		io1 = io1.canvas().fillRect((int) (w * 0.35d), 0, (int) ((1 - 2 * 0.35) * w), h, ImageOperation.BACKGROUND_COLORint).getImage()
				.io();
		
		markerPositionsImage = io1
				.thresholdLAB(0, 255, 110, 140, 0, 110, ImageOperation.BACKGROUND_COLORint, typ, maize).show("nach lab", debug)
				.border((int) (8 * scaleFactor + 1))
				.bm().opening((int) (8 * scaleFactor), (int) (4 * scaleFactor)).io()
				.show("nach opening", debug);
		
		regionPositions = markerPositionsImage.findRegions(debug);
		
		markerPositionsImage = markerPositionsImage.bm()
				.dilate(15).io()
				.replaceColor(background, Color.BLUE.getRGB())
				.replaceColor(Color.BLACK.getRGB(), background)
				.replaceColor(Color.BLUE.getRGB(), Color.BLACK.getRGB())
				.show("Markers enlarged (b)", debug);
	}
	
	private ArrayList<Vector2d> getCoordinates() {
		ArrayList<Vector2d> result = new ArrayList<Vector2d>();
		
		if (regionPositions != null)
			for (Vector2i vec : regionPositions) {
				int x = vec.x;
				int y = vec.y;
				
				Vector2d temp = new Vector2d(x, y);
				result.add(temp);
			}
		result.remove(0); // remove first marker, this is the background
		return result;
	}
	
	public ArrayList<MarkerPair> getResultCoordinates(int verticalToleranceToDetectPairs) {
		
		ArrayList<Vector2d> coordinatesUnfiltered = getCoordinates();
		
		ArrayList<MarkerPair> result = new ArrayList<MarkerPair>();
		
		if (coordinatesUnfiltered == null) {
			// System.out.println(SystemAnalysisExt.getCurrentTime() + ">ERROR: No blue marker coordinates (null)!");
			return result;
		}
		
		if (coordinatesUnfiltered.isEmpty()) {
			// System.out.println(SystemAnalysisExt.getCurrentTime() + ">INFO: No blue marker coordinates (empty). Image " + input + " //" + typ);
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
					if (sameYposition(l, r, verticalToleranceToDetectPairs)) {
						added.add(l);
						added.add(r);
						result.add(new MarkerPair(l, r, inputImageWidth));
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
				result.add(index, new MarkerPair(l, null, inputImageWidth));
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
				result.add(index, new MarkerPair(null, r, inputImageWidth));
			}
		}
		
		double minX = Double.MAX_VALUE;
		double maxX = -Double.MAX_VALUE;
		
		for (MarkerPair mp : result) {
			if (mp.left != null && mp.left.x < minX)
				minX = mp.left.x;
			if (mp.right != null && mp.right.x > maxX)
				maxX = mp.right.x;
		}
		
		for (MarkerPair mp : result) {
			if (mp.left != null)
				mp.left.x = minX;
			if (mp.right != null)
				mp.right.x = maxX;
		}
		
		/**
		 * marker 1 needs later to be the lowest one
		 */
		Collections.sort(result, new Comparator<MarkerPair>() {
			@Override
			public int compare(MarkerPair o1, MarkerPair o2) {
				return o1.left.y < o2.left.y ? 1 : -1;
			}
		});
		
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
		for (int index = 0; index < coordinates.size(); index++) {
			if (coordinates.get(index).x > xLimit) {
				result.add(coordinates.get(index));
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
	
	public ImageOperation getClearedImage() {
		return markerPositionsImage;
	}
}
