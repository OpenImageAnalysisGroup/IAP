package de.ipk.ag_ba.image.operations;

import ij.measure.ResultsTable;
import ij.plugin.filter.MaximumFinder;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;

import de.ipk.ag_ba.image.structures.FlexibleImage;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.importers.kgml.Vector2d;

public class BlueMarkerFinder {
	
	private final FlexibleImage input;
	private ResultsTable resultTable;
	
	public BlueMarkerFinder(FlexibleImage image) {
		this.input = image;
	}
	
	public void findCoordinates() {
		ImageOperation io1 = new ImageOperation(input);
		
		resultTable = io1.thresholdLAB(120, 190, 105, 138, 10, 95, 0)
				.convert2Grayscale().medianFilter8Bit()
				.threshold(40, Color.WHITE.getRGB(), Color.BLACK.getRGB())
				.findMax(10.0, MaximumFinder.LIST).getResultsTable();
		
	}
	
	private ArrayList<Vector2d> getCoordinates() {
		ArrayList<Vector2d> result = new ArrayList<Vector2d>();
		
		for (int i = 0; i < resultTable.getCounter(); i++) {
			int a = (int) resultTable.getValueAsDouble(0, i);
			int b = (int) resultTable.getValueAsDouble(1, i);
			
			Vector2d temp = new Vector2d(a, b);
			result.add(temp);
		}
		return result;
	}
	
	public ArrayList<MarkerPair> getResultCoordinates(int tolerance) {
		
		ArrayList<Vector2d> coordinatesUnfiltered = getCoordinates();
		
		ArrayList<Vector2d> coordinatesLeft = searchLeft(coordinatesUnfiltered,
				getMinX(coordinatesUnfiltered) + input.getWidth() * 0.08);
		ArrayList<Vector2d> coordinatesRight = searchRight(
				coordinatesUnfiltered, input.getWidth() * 0.1);
		
		ArrayList<MarkerPair> result = new ArrayList<MarkerPair>();
		
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
	
	private boolean sameYposition(Vector2d l, Vector2d r, int tolerance) {
		return l != null && r != null && Math.abs(l.y - r.y) < tolerance;
	}
	
	public ArrayList<Vector2d> searchRight(ArrayList<Vector2d> coordinates,
			double sizeOfROI) {
		ArrayList<Vector2d> result = new ArrayList<Vector2d>();
		double minX = getMaxX(coordinates);
		
		for (int index = 0; index < coordinates.size(); index++) {
			if (coordinates.get(index).x > minX - sizeOfROI) {
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
			double sizeOfROI) {
		
		ArrayList<Vector2d> result = new ArrayList<Vector2d>();
		double minX = getMinX(coordinates);
		
		for (int index = 0; index < coordinates.size(); index++) {
			if (coordinates.get(index).x < minX + sizeOfROI) {
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
