package de.ipk.ag_ba.labcube;

public class ImageOperationLabCube {
	
	public static ImageOperationLabCubeInterface getter;
	
	public static float[][][] labCube() {
		return getter.getLabCube();
	}
}
