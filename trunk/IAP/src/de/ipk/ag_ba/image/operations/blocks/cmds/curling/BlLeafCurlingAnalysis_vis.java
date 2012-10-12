package de.ipk.ag_ba.image.operations.blocks.cmds.curling;

import java.awt.Point;
import java.util.ArrayList;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import de.ipk.ag_ba.image.operations.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.operations.blocks.cmds.data_structures.AbstractSnapshotAnalysisBlockFIS;
import de.ipk.ag_ba.image.operations.skeleton.Limb;
import de.ipk.ag_ba.image.operations.skeleton.SkeletonProcessor2d;
import de.ipk.ag_ba.image.structures.FlexibleImage;
import flanagan.math.FourierTransform;

public class BlLeafCurlingAnalysis_vis extends AbstractSnapshotAnalysisBlockFIS {
	@Override
	protected FlexibleImage processVISmask() {
		
		boolean debug = false;
		
		if (input().masks().vis() == null) {
			return null;
		}
		FlexibleImage img1 = input().masks().vis().copy();
		
		ImageOperation dist = img1.io().skel()
				.calculateDistanceToBorder(true, ImageOperation.BACKGROUND_COLORint)
				.print("Distance", debug);
		// img1.copy().io().skel().skeletonize(Color.WHITE.getRGB()).print("INCORRECT RESULT 2");
		// img1.io().copy().skeletonize().print("CORRECT RESULT 1");
		// img2.io().copy().skeletonize().print("CORRECT RESULT 2");
		ImageOperation skel = img1.io().copy().skeletonize(true)
				.print("Skeleton", debug);
		
		ImageOperation result = dist.applyMask(skel.getImage(), ImageOperation.BACKGROUND_COLORint).print("Result", debug);
		SkeletonProcessor2d skel2d = result.skel2d();
		skel2d.background = ImageOperation.BACKGROUND_COLORint;
		skel2d.findEndpointsAndBranches();
		skel2d.calculateEndlimbsRecursive();
		ArrayList<Limb> limbs = skel2d.getEndlimbs();
		DescriptiveStatistics statsFFTfrequency = new DescriptiveStatistics();
		DescriptiveStatistics statsFFTamplitude = new DescriptiveStatistics();
		for (Limb l : limbs) {
			if (l.length() < 100)
				continue;
			ArrayList<Point> points = l.getPoints();
			ArrayList<Integer> distances = new ArrayList<Integer>(points.size());
			for (Point p : points) {
				distances.add(result.getPixel(p.x, p.y));
			}
			int nPoints = points.size(); // number of points
			double[] tdata = new double[nPoints];
			double[] ydata = new double[nPoints];
			
			// Create wave form
			for (int i = 0; i < nPoints; i++) {
				ydata[i] = distances.get(i);
				tdata[i] = i;
			}
			
			FourierTransform ft0 = new FourierTransform(ydata);
			ft0.setDeltaT(1d / 100d);
			double[][] powerSpectrum = ft0.powerSpectrum();
			double averageFrequency = calculateAverageFrequency(powerSpectrum);
			double averageAmplitude = calculateAverageIntensity(powerSpectrum);
			
			statsFFTfrequency.addValue(averageFrequency);
			statsFFTamplitude.addValue(averageAmplitude);
		}
		
		if (statsFFTfrequency.getN() > 0) {
			ResultsTableWithUnits rt = new ResultsTableWithUnits();
			rt.incrementCounter();
			rt.addValue("leaf.curling.n", statsFFTfrequency.getN());
			rt.addValue("leaf.curling.frequency.avg", statsFFTfrequency.getMean());
			rt.addValue("leaf.curling.frequency.stddev", statsFFTfrequency.getStandardDeviation());
			rt.addValue("leaf.curling.amplitude.avg", statsFFTamplitude.getMean());
			rt.addValue("leaf.curling.amplitude.stddev", statsFFTamplitude.getStandardDeviation());
			
			String pre = "RESULT_" + options.getCameraPosition();
			getProperties().storeResults(pre + ".", rt, getBlockPosition());
		}
		
		return input().masks().vis();
	}
	
	private static double calculateAverageFrequency(double[][] powerSpectrum) {
		double[] bins = powerSpectrum[0];
		double[] intensity = powerSpectrum[1];
		double totalIntensity = 0;
		double values = 0;
		for (int i = 0; i < bins.length; i++) {
			totalIntensity += intensity[i];
			values += bins[i] * intensity[i];
		}
		double averageFrequency = values / totalIntensity;
		return averageFrequency;
	}
	
	private static double calculateAverageIntensity(double[][] powerSpectrum) {
		double[] intensity = powerSpectrum[1];
		double totalIntensity = 0;
		for (int i = 0; i < intensity.length; i++) {
			totalIntensity += intensity[i];
		}
		double averageIntensity = totalIntensity / intensity.length;
		return averageIntensity;
	}
}
