package de.ipk.ag_ba.server.analysis.image_analysis_tasks.reconstruction3d;

import de.ipk.ag_ba.image.operation.ImageOperation;

/**
 * @author klukas
 */
public class Voxel {
	
	public final int x;
	public final int y;
	public final int z;
	private final int color;
	
	public Voxel(int x, int y, int z, int color) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.color = color;
	}
	
	public Voxel(int px, int py, int pz) {
		this(px, py, pz, ImageOperation.BACKGROUND_COLORint);
	}
	
	public boolean isEmpty() {
		return color != ImageOperation.BACKGROUND_COLORint;
	}
	
}
