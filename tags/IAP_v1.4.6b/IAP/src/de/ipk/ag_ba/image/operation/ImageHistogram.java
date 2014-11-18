package de.ipk.ag_ba.image.operation;

/**
 * @author klukas
 */
public class ImageHistogram {
	
	private final ImageOperation imageOperation;
	
	int[] freqR, freqG, freqB;
	
	public ImageHistogram(ImageOperation imageOperation, boolean grayScale) {
		this.imageOperation = imageOperation;
		
		if (!grayScale)
			freqR = new int[256];
		if (!grayScale)
			freqG = new int[256];
		freqB = new int[256];
		
		int r, g, b;
		
		for (int c : imageOperation.getAs1D()) {
			if (!grayScale) {
				r = (c & 0xff0000) >> 16;
				g = (c & 0x00ff00) >> 8;
				freqR[r]++;
				freqG[g]++;
			}
			b = c & 0x0000ff;
			freqB[b]++;
		}
	}
	
	public int getMostCommonValueR() {
		return getMostCommon(freqR);
	}
	
	public int getMostCommonValueG() {
		return getMostCommon(freqG);
	}
	
	public int getMostCommonValueB() {
		return getMostCommon(freqB);
	}
	
	private int getMostCommon(int[] freq) {
		int idx = -1;
		int max = -1;
		for (int i = 0; i < freq.length; i++) {
			if (freq[i] > max) {
				idx = i;
				max = freq[i];
			}
		}
		return idx;
	}
	
}
