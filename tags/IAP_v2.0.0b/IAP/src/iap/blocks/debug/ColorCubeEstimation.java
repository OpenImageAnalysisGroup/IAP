package iap.blocks.debug;

import de.ipk.ag_ba.image.operation.ImageOperation;
import de.ipk.ag_ba.image.operation.channels.Channel;
import de.ipk.ag_ba.image.structures.Image;

public class ColorCubeEstimation {
	
	private final double[][][] histCube;
	private final int numberOfBins;
	private double maxVal = 0.0;
	
	public ColorCubeEstimation(Image img, Image optImg2, Channel a, Channel b, Channel c, int numberOfBins) {
		histCube = new double[numberOfBins][numberOfBins][numberOfBins];
		this.numberOfBins = numberOfBins;
		int[] ch_a = img.io().channels().get(a).getAs1D();
		int[] ch_b = img.io().channels().get(b).getAs1D();
		int[] ch_c = img.io().channels().get(c).getAs1D();
		
		int[] ch_a2 = optImg2 != null ? optImg2.io().channels().get(a).getAs1D() : null;
		int[] ch_b2 = optImg2 != null ? img.io().channels().get(b).getAs1D() : null;
		int[] ch_c2 = optImg2 != null ? img.io().channels().get(c).getAs1D() : null;
		
		calcCube(ch_a, ch_b, ch_c, ch_a2, ch_b2, ch_c2);
	}
	
	private void calcCube(int[] ch_a, int[] ch_b, int[] ch_c, int[] ch_a2, int[] ch_b2, int[] ch_c2) {
		double sizeOfBins = 256 / (double) numberOfBins;
		for (int idx = 0; idx < ch_a.length; idx++) {
			if (ch_a[idx] != ImageOperation.BACKGROUND_COLORint) {
				int a = (int) ((ch_a[idx] & 0x0000ff) / sizeOfBins);
				int b = (int) ((ch_b[idx] & 0x0000ff) / sizeOfBins);
				int c = (int) ((ch_c[idx] & 0x0000ff) / sizeOfBins);
				
				histCube[a][b][c]++;
			}
		}
		
		if (ch_a2 != null)
			for (int idx = 0; idx < ch_a.length; idx++) {
				if (ch_a[idx] != ImageOperation.BACKGROUND_COLORint) {
					int a = (int) ((ch_a2[idx] & 0x0000ff) / sizeOfBins);
					int b = (int) ((ch_b2[idx] & 0x0000ff) / sizeOfBins);
					int c = (int) ((ch_c2[idx] & 0x0000ff) / sizeOfBins);
					
					histCube[a][b][c]--;
				}
			}
		
		for (int a = 0; a < histCube.length; a++)
			for (int b = 0; b < histCube[a].length; b++)
				for (int c = 0; c < histCube[a][b].length; c++) {
					if (Math.abs(histCube[a][b][c]) > maxVal)
						maxVal = Math.abs(histCube[a][b][c]);
				}
	}
	
	public double[][][] getHistogramCube() {
		return histCube;
	}
	
	public double getMaxValue() {
		return maxVal;
	}
}