package de.ipk.ag_ba.image.operations.skeleton;

public class Mask {
	
	int[][] mask;
	
	public Mask(int[][] inp) {
		this.mask = inp;
	}
	
	public void mirrorX() {
		int w = mask.length;
		int h = mask[0].length;
		int[][] res = new int[w][h];
		
		for (int x = 0; x < mask.length; x++) {
			for (int y = 0; y < mask[0].length; y++) {
				res[w - x][y] = mask[x][y];
			}
		}
		mask = res;
	}
	
	public void mirrorY() {
		int w = mask.length;
		int h = mask[0].length;
		int[][] res = new int[w][h];
		
		for (int x = 0; x < mask.length; x++) {
			for (int y = 0; y < mask[0].length; y++) {
				res[x][h - y] = mask[x][y];
			}
		}
		mask = res;
	}
}
