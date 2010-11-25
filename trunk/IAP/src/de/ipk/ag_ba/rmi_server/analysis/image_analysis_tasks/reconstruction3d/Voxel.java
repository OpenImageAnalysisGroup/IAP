package de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.reconstruction3d;

import de.ipk.ag_ba.rmi_server.analysis.image_analysis_tasks.PhenotypeAnalysisTask;

/**
 * @author klukas
 * 
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
		this(px, py, pz, PhenotypeAnalysisTask.BACKGROUND_COLORint);
	}

	public boolean isEmpty() {
		return color != PhenotypeAnalysisTask.BACKGROUND_COLORint;
	}

}
