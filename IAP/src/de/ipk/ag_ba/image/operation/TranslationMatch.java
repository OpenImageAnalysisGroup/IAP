package de.ipk.ag_ba.image.operation;

import java.util.ArrayList;
import java.util.Collections;

import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import de.ipk.ag_ba.image.structures.Image;

/**
 * Moves an image vertically or horizontally to match the given image (mainImage).
 * At first, calcOffset needs to be called. Then a number of images, e.g. the
 * main image and the reference image can be moved to match the main image with
 * the method translate.
 * 
 * @author Christian Klukas
 */
public class TranslationMatch {
	
	private final boolean debug;
	private final ArrayList<Double> mainImagePatternForImageLines;
	private final ArrayList<Double> mainImagePatternForImageColumns;
	private double offsetVertY, offsetHorX;
	private ArrayList<Double> corrsVert, corrsHor;
	
	public TranslationMatch(ImageOperation mainImage, boolean debug) {
		this.debug = debug;
		mainImagePatternForImageLines = mainImage.calculateVerticalPattern();
		mainImagePatternForImageColumns = mainImage.calculateHorizontalPattern();
	}
	
	public void calcOffsetVerticalY(Image img) {
		if (img == null) {
			offsetVertY = 0;
			return;
		}
		ImageOperation io = img.io();
		
		if (debug)
			io.copy().canvas().drawSideHistogram().io().show("USED FOR MATCHING (VERT)");
		
		corrsVert = new ArrayList<Double>();
		offsetVertY = -match(mainImagePatternForImageLines, io.calculateVerticalPattern(), corrsVert);
		
		if (debug) {
			int bestI = 0;
			double bestC = 0;
			for (int i = 0; i < corrsVert.size(); i++) {
				if (corrsVert.get(i) > bestC) {
					bestI = i;
					bestC = corrsVert.get(i);
				}
			}
			
			corrsVert.set(bestI, 6d);
			corrsVert.set(corrsVert.size() / 2, 3d);
			Collections.reverse(corrsVert);
		}
	}
	
	public void calcOffsetHorizontalX(Image img) {
		if (img == null) {
			offsetHorX = 0;
			return;
		}
		ImageOperation io = img.io();
		
		corrsHor = new ArrayList<Double>();
		offsetHorX = -match(mainImagePatternForImageColumns, io.calculateHorizontalPattern(), corrsHor);
		
		if (debug) {
			int bestI = 0;
			double bestC = 0;
			for (int i = 0; i < corrsHor.size(); i++) {
				if (corrsHor.get(i) > bestC) {
					bestI = i;
					bestC = corrsHor.get(i);
				}
			}
			
			corrsHor.set(bestI, 6d);
			corrsHor.set(corrsHor.size() / 2, 3d);
			Collections.reverse(corrsHor);
		}
	}
	
	public Image translate(Image toBeTransalated) {
		return toBeTransalated.io().translate(offsetHorX, offsetVertY).canvas().
				drawSideHistogram(corrsVert, debug).drawTopHistogram(corrsHor, debug).getImage();
	}
	
	private int match(ArrayList<Double> staticPattern, ArrayList<Double> movePattern, ArrayList<Double> corrs) {
		if (staticPattern.size() != movePattern.size()) {
			staticPattern = transformLength(staticPattern, movePattern.size());
		}
		int bestOffset = 0;
		double bestCorrelation = 0;
		int v = (int) (movePattern.size() * 0.25);
		for (int offY = -v; offY <= v; offY++) {
			double corr = correlate(staticPattern, movePattern, offY);
			if (corrs != null)
				corrs.add(corr);
			if (corr > bestCorrelation) {
				bestCorrelation = corr;
				bestOffset = offY;
			}
		}
		// the offset will be near the end, if no good fit is found (e.g. nearly empty NR image)
		if (Math.abs(bestOffset) < v * 0.05d || bestCorrelation < 0.4)
			return 0;
		else
			return bestOffset;
	}
	
	private double correlate(ArrayList<Double> staticPattern, ArrayList<Double> movePattern, int offY) {
		ArrayList<Double> values1 = staticPattern;
		ArrayList<Double> values2 = movePattern;
		SpearmansCorrelation pc = new SpearmansCorrelation();
		// PearsonsCorrelation pc = new PearsonsCorrelation();
		double[] xArray = new double[values1.size()];
		int i = 0;
		for (Double d : values1)
			xArray[i++] = d;
		double[] yArray = new double[values2.size()];
		i = 0;
		for (Double d : values2) {
			int idx = -offY + (i++);
			if (idx >= 0 && idx < yArray.length)
				yArray[idx] = d;
		}
		double r = pc.correlation(xArray, yArray);
		return r;
	}
	
	private ArrayList<Double> transformLength(ArrayList<Double> staticPattern, int size) {
		if (size == 0)
			return new ArrayList<Double>();
		ArrayList<Double> res = new ArrayList<Double>(size);
		for (int i = 0; i < size; i++) {
			int n = 0;
			double sum = 0;
			int idx = 0;
			for (double v : staticPattern) {
				if ((int) (idx / (double) staticPattern.size() * size) == i) {
					n++;
					sum += v;
				}
				idx++;
			}
			if (n > 0) {
				double v = sum / n;
				while (res.size() <= i)
					res.add(0d);
				res.set(i, v);
			}
		}
		return res;
	}
	
	public double getOffsetVerticalY() {
		return offsetVertY;
	}
	
	public void setOffsetVerticalY(double y) {
		offsetVertY = y;
	}
}
