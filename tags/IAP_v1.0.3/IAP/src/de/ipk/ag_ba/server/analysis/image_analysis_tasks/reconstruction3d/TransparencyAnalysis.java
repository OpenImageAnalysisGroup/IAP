package de.ipk.ag_ba.server.analysis.image_analysis_tasks.reconstruction3d;

import java.awt.Color;
import java.util.ArrayList;

import org.Colors;

/**
 * @author klukas
 */
public class TransparencyAnalysis {
	
	ArrayList<Color> transparentColors = new ArrayList<Color>();
	ArrayList<Float> transparentColorsH = new ArrayList<Float>();
	ArrayList<Float> transparentColorsS = new ArrayList<Float>();
	ArrayList<Float> transparentColorsB = new ArrayList<Float>();
	
	private double maxDiffHSV = 0.3;
	
	public TransparencyAnalysis(double backgroundDiff) {
		maxDiffHSV = backgroundDiff;
	}
	
	public void addColor(Color t) {
		if (isTransparent(t))
			return;
		transparentColors.add(t);
		float[] hsb = new float[3];
		Color.RGBtoHSB(t.getRed(), t.getGreen(), t.getBlue(), hsb);
		transparentColorsH.add(hsb[0]);
		transparentColorsS.add(hsb[1]);
		transparentColorsB.add(hsb[2]);
	}
	
	public void reset() {
		transparentColors.clear();
	}
	
	public boolean isTransparent(Color c) {
		int cr = c.getRed();
		int cg = c.getGreen();
		int cb = c.getBlue();
		return isTransparent(cr, cg, cb);
	}
	
	public boolean isTransparent(int cr, int cg, int cb) {
		float[] hsb = new float[3];
		Color.RGBtoHSB(cr, cg, cb, hsb);
		int idx = 0;
		for (float h : transparentColorsH) {
			float s = transparentColorsS.get(idx);
			float b = transparentColorsB.get(idx);
			idx++;
			double diffH = Math.abs(h - hsb[0]);
			double diffS = Math.abs(s - hsb[1]);
			double diffB = Math.abs(b - hsb[2]);
			if (diffH < maxDiffHSV && diffS < maxDiffHSV && diffB < maxDiffHSV)
				return true;
		}
		return false;
	}
	
	public void addColors(Color start, Color end) {
		float f = 0;
		for (int i = 1; i <= 30; i++) {
			Color c = Colors.getColor(f, 1, start, end);
			addColor(c);
			f += 1f / 30f;
		}
	}
	
}
