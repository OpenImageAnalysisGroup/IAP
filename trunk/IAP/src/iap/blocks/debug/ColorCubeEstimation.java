package iap.blocks.debug;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.channels.Channel;
import de.ipk.ag_ba.image.structures.Image;

public class ColorCubeEstimation {
	
	private final double[][][] histCube;
	private final int numberOfBins;
	private double maxVal = 0.0;
	
	public ColorCubeEstimation(Image img, Channel a, Channel b, Channel c, int numberOfBins) {
		histCube = new double[numberOfBins][numberOfBins][numberOfBins];
		this.numberOfBins = numberOfBins;
		int[] ch_a = img.io().channels().get(a).getAs1D();
		int[] ch_b = img.io().channels().get(b).getAs1D();
		int[] ch_c = img.io().channels().get(c).getAs1D();
		
		calcCube(ch_a, ch_b, ch_c);
	}
	
	private void calcCube(int[] ch_a, int[] ch_b, int[] ch_c) {
		double sizeOfBins = 256 / (double) numberOfBins;
		for (int idx = 0; idx < ch_a.length; idx++) {
			if (ch_a[idx] != ImageOperation.BACKGROUND_COLORint) {
				int a = (int) ((ch_a[idx] & 0x0000ff) / sizeOfBins);
				int b = (int) ((ch_b[idx] & 0x0000ff) / sizeOfBins);
				int c = (int) ((ch_c[idx] & 0x0000ff) / sizeOfBins);
				histCube[a][b][c]++;
				if (histCube[a][b][c] > maxVal)
					maxVal = histCube[a][b][c];
			}
		}
	}
	
	public double[][][] getHistogramCube() {
		return histCube;
	}
	
	public double getMaxValue() {
		return maxVal;
	}
}