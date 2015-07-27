package iap.blocks.extraction;

import iap.blocks.data_structures.AbstractSnapshotAnalysisBlock;
import iap.blocks.data_structures.BlockType;
import iap.blocks.data_structures.CalculatedProperty;
import iap.blocks.data_structures.CalculatedPropertyDescription;
import iap.blocks.data_structures.CalculatesProperties;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operations.blocks.ResultsTableWithUnits;
import de.ipk.ag_ba.image.operations.skeleton.Limb;
import de.ipk.ag_ba.image.operations.skeleton.SkeletonProcessor2d;
import de.ipk.ag_ba.image.structures.CameraType;
import de.ipk.ag_ba.image.structures.Image;
import flanagan.math.FourierTransform;

/**
 * @author Christian Klukas
 */
public class BlLeafCurlingAnalysis extends AbstractSnapshotAnalysisBlock implements CalculatesProperties {
	@Override
	protected Image processVISmask() {
		
		boolean debug = getBoolean("debug", false);
		
		if (input().masks().vis() == null) {
			return null;
		}
		
		if (!getBoolean("enabled", false))
			return input().masks().vis();
		
		Image img1 = input().masks().vis().copy();
		
		ImageOperation dist = img1.io().skel()
				.calculateDistanceToBorder(true, ImageOperation.BACKGROUND_COLORint)
				.show("Distance", debug);
		
		ImageOperation skel = img1.io().copy().skeletonize()
				.show("Skeleton", debug);
		
		ImageOperation result = dist.applyMask(skel.getImage(), ImageOperation.BACKGROUND_COLORint).show("Result", debug);
		SkeletonProcessor2d skel2d = result.skel2d();
		skel2d.background = ImageOperation.BACKGROUND_COLORint;
		skel2d.createEndpointsAndBranchesLists(null);
		skel2d.calculateEndlimbsRecursive();
		ArrayList<Limb> limbs = skel2d.getEndlimbs();
		DescriptiveStatistics statsFFTfrequency = new DescriptiveStatistics();
		DescriptiveStatistics statsFFTamplitude = new DescriptiveStatistics();
		DescriptiveStatistics signChangeStat = new DescriptiveStatistics();
		for (Limb l : limbs) {
			if (l.length() < getInt("Minimum-Limb-Length", 100))
				continue;
			ArrayList<Point> points = l.getPoints();
			if (points.isEmpty())
				continue;
			ArrayList<Integer> distances = new ArrayList<Integer>(points.size());
			for (Point p : points) {
				distances.add(result.getPixel(p.x, p.y));
			}
			int nPoints = points.size(); // number of points
			double[] ydata = new double[nPoints];
			
			// Create wave form
			int signChanges = 0;
			int p, lastP = -1;
			boolean plus = false;
			boolean previousPlus = false;
			for (int i = 0; i < nPoints; i++) {
				p = distances.get(i);
				ydata[i] = p;
				if (lastP > 0) {
					if (p >= lastP) {
						plus = true;
					} else {
						plus = false;
					}
					if (plus != previousPlus)
						signChanges++;
					previousPlus = plus;
				}
				lastP = p;
			}
			
			FourierTransform ft0 = new FourierTransform(ydata);
			ft0.setDeltaT(1d / 100d);
			double[][] powerSpectrum = ft0.powerSpectrum();
			double averageFrequency = calculateAverageFrequency(powerSpectrum);
			double averageAmplitude = calculateAverageIntensity(powerSpectrum);
			
			double signChangesPerPixel = signChanges / nPoints;
			
			statsFFTfrequency.addValue(averageFrequency);
			statsFFTamplitude.addValue(averageAmplitude);
			
			signChangeStat.addValue(signChangesPerPixel);
		}
		
		if (statsFFTfrequency.getN() > 0) {
			ResultsTableWithUnits rt = new ResultsTableWithUnits();
			rt.incrementCounter();
			rt.addValue("leaf.curling.n", statsFFTfrequency.getN());
			rt.addValue("leaf.curling.frequency.mean", statsFFTfrequency.getMean());
			rt.addValue("leaf.curling.frequency.stddev", statsFFTfrequency.getStandardDeviation());
			rt.addValue("leaf.curling.amplitude.mean", statsFFTamplitude.getMean());
			rt.addValue("leaf.curling.amplitude.stddev", statsFFTamplitude.getStandardDeviation());
			
			rt.addValue("leaf.curling.signchanges.mean", statsFFTamplitude.getMean(), "changes/pixel");
			rt.addValue("leaf.curling.signchanges.stddev", statsFFTamplitude.getStandardDeviation(), "changes/pixel");
			
			getResultSet().storeResults(optionsAndResults.getCameraPosition(), CameraType.VIS, TraitCategory.GEOMETRY, rt,
					getBlockPosition(), this, input().images().getVisInfo());
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
		return BlockType.FEATURE_EXTRACTION;
	}
	
	@Override
	public String getName() {
		return "Leaf Curling Analysis";
	}
	
	@Override
	public String getDescription() {
		return "Analyzes the leaf curling structure.";
	}
	
	@Override
	public CalculatedPropertyDescription[] getCalculatedProperties() {
		return new CalculatedPropertyDescription[] {
				new CalculatedProperty("leaf.curling.n",
						"Number of considered leaves (with length above the minimum-length threshold) for curling analysis."),
				new CalculatedProperty("leaf.curling.frequency.mean",
						"Average curling frequency."),
				new CalculatedProperty("leaf.curling.frequency.stddev",
						"Standard deviation of the curling frequencies, based on the frequency of the considered leaves."),
				new CalculatedProperty("leaf.curling.amplitude.mean",
						"Average curling amplitude."),
				new CalculatedProperty("leaf.curling.amplitude.stddev",
						"Standard deviation of the curling amplitudes, based on the amplitudes of the considered leaves.")
		};
	}
}
