package de.ipk.ag_ba.gui.webstart;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

public enum IAP_RELEASE {
	RELEASE_IAP_IMAGE_ANALYSIS_ROOTS("R41_2012-09-27_roots"),
	RELEASE_IAP_IMAGE_ANALYSIS_BARLEY("R40_2012-09-07_barley"),
	RELEASE_IAP_IMAGE_ANALYSIS_MAIZE("R40_2012-09-07_maize"),
	RELEASE_IAP_IMAGE_ANALYSIS_ARABIDOPSIS("R40_2012-09-07_arabidopsis"),
	RELEASE_IAP_IMAGE_ANALYSIS_3D("R40_2012-09-07_3D"),
	RELEASE_IAP_IMAGE_ANALYSIS_OTHER("R40_2012-09-07_other");
	
	// RELEASE_IAP_IMAGE_ANALYSIS_BARLEY("R39_2012-08-20_barley"),
	// RELEASE_IAP_IMAGE_ANALYSIS_MAIZE("R39_2012-08-20_maize"),
	// RELEASE_IAP_IMAGE_ANALYSIS_ARABIDOPSIS("R39_2012-08-20_arabidopsis"),
	// RELEASE_IAP_IMAGE_ANALYSIS_3D("R39_2012-08-20_3D"),
	// RELEASE_IAP_IMAGE_ANALYSIS_OTHER("R39_2012-08-20_other");
	
	// RELEASE_IAP_IMAGE_ANALYSIS_BARLEY("R38_2012-08-05_barley"),
	// RELEASE_IAP_IMAGE_ANALYSIS_MAIZE("R38_2012-08-05_maize"),
	// RELEASE_IAP_IMAGE_ANALYSIS_ARABIDOPSIS("R38_2012-08-05_arabidopsis"),
	// RELEASE_IAP_IMAGE_ANALYSIS_3D("R38_2012-08-05_3D"),
	// RELEASE_IAP_IMAGE_ANALYSIS_OTHER("R38_2012-08-05_other");
	
	// RELEASE_IAP_IMAGE_ANALYSIS_MAIZE("R37_2012-08-02_maize"),
	// RELEASE_IAP_IMAGE_ANALYSIS_MAIZE("R37_2012-07-30_maize"),
	// RELEASE_IAP_IMAGE_ANALYSIS_MAIZE("R37_2012-07-10_maize"),
	// public static final String RELEASE_IAP_IMAGE_ANALYSIS = "R34_2012-06-26_color_stats_pc1";
	// public static final String RELEASE_IAP_IMAGE_ANALYSIS = "R33_2012-06-22_release_test3_arabidopsis";
	// public static final String RELEASE_IAP_IMAGE_ANALYSIS = "R32_2012-06-20_release_test2";
	// public static final String RELEASE_IAP_IMAGE_ANALYSIS = "R31_2012-05-27_release_test1";
	// public static final String RELEASE_IAP_IMAGE_ANALYSIS = "R30_2012-05-24_optimized2";
	// public static final String RELEASE_IAP_IMAGE_ANALYSIS = "R28_2012-05-11_barley__in_maize_improved";
	// public static final String RELEASE_IAP_IMAGE_ANALYSIS = "R25_2012-04-27_multi_tray_barley_in_maize";
	// public static final String RELEASE_IAP_IMAGE_ANALYSIS = "R24_2012-04-16_remove_not_needed_histograms";
	// public static final String RELEASE_IAP_IMAGE_ANALYSIS = "R23_2012-04-14_relative_leaf_length";
	// public static final String RELEASE_IAP_IMAGE_ANALYSIS = "R22_2012-04-13_ratio_fix";
	// public static final String RELEASE_IAP_IMAGE_ANALYSIS = "R21_2012-03-17_trimmed_fluo_vis_hist";
	
	private final String title;
	
	private IAP_RELEASE(String title) {
		this.title = title;
	}
	
	@Override
	public String toString() {
		return title;
	}
	
	public static IAP_RELEASE getReleaseFromDescription(ExperimentHeaderInterface e) {
		String rem = e.getRemark();
		if (rem != null)
			for (IAP_RELEASE ir : values()) {
				if (rem.contains(ir.toString()))
					return ir;
			}
		return null;
	}
}
