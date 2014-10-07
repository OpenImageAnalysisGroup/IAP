package de.ipk.ag_ba.commands.experiment.scripts.clustering;

import de.ipk.ag_ba.commands.experiment.view_or_export.ActionScriptBasedDataProcessing;

/**
 * @author klukas
 */
public class ScriptPVCLUST implements ActionScriptBasedDataProcessing {
	
	@Override
	public String getFileName() {
		return getTitle();
	}
	
	@Override
	public String getTitle() {
		return "Hierarchical Clustering";
	}
	
	@Override
	public String getCommand() {
		return "Rscript";
	}
	
	@Override
	public String[] getParams() {
		return new String[] {
				"--vanilla",
				"--encoding=UTF-8",
				"calcClusters.R",
				"[int|bootstrap sample size|1000|"
						+
						"Computation of p-values via multiscale bootstrap resampling requires a " +
						"comparatively large bootstrap sample size. "
						+
						"It is recommend to use 1000 for testing and 10000 for smaller errors.|" +
						"If the bootstrap sample size is set to 0, hclust, a clustering approach which does not compute p-values, is performed.]" };
	}
	
	@Override
	public String getImage() {
		return "img/clustering.png";
	}
	
	@Override
	public String getTooltip() {
		return "hierarchical clustering with p-values (pvclust) or without p-values (hclust)";
	}
	
	@Override
	public String[] getWebURLs() {
		return new String[] {
				"http://www.is.titech.ac.jp/~shimo/prog/pvclust/",
				"http://stat.ethz.ch/R-manual/R-devel/library/stats/html/hclust.html" };
	}
	
	@Override
	public String[] getWebUrlTitles() {
		return new String[] {
				"pvclust",
				"hclust" };
	}
	
	@Override
	public String getExportDataFileName() {
		return "report.clustering.csv";
	}
	
	@Override
	public boolean allowGroupingColumnSelection() {
		return true;
	}
	
	@Override
	public boolean allowGroupingFiltering() {
		return true;
	}
	
	@Override
	public boolean allowSelectionOfDataColumns() {
		return true;
	}
	
	@Override
	public String[] getDesiredDataColumns() {
		return new String[] {
				"weight_before:Weight before watering",
				"water_weight:Water weight",
				"side.vis.height.norm:Height (zoom corrected) // side.height:Height",
				"side.vis.area.norm:Side Vis Area (normalized) // side.vis.area:Side Vis Area",
				"side.fluo.intensity.mean:Fluo intensity (side)",
				"side.nir.intensity.mean:NIR intensity (side)",
				"top.fluo.intensity.mean:Fluo intensity (top)",
				"top.nir.intensity.mean:NIR intensity (top)",
				"side.vis.hsv.h.mean:Average hue (side)",
				"top.vis.hsv.h.mean:Average hue (top)",
				"side.vis.width.mean:Width (zoom corrected) // side.vis.width:Width",
				"top.vis.area.norm:Top Vis Area (zoom corrected) // top.vis.area:Top Vis Area",
				"volume.fluo.iap:Volume estimation (fluo)",
		};
	}
	
	@Override
	public String[] getScriptFileNames() {
		return new String[] {
				"calcClusters.R"
		};
	}
	
	@Override
	public int getTimeoutInMin() {
		return -1;
	}
	
	@Override
	public boolean exportClusteringDataset() {
		return true;
	}
}
