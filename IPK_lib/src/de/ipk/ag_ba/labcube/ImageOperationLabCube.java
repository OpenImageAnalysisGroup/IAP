package de.ipk.ag_ba.labcube;

public class ImageOperationLabCube {
	
	public static ImageOperationLabCubeInterface getter;
	
	public static float[][][] labCube() {
		if (getter != null)
			return getter.getLabCube();
		else
			return null;
	}
}
