package de.ipk.ag_ba.commands.experiment.scripts.clustering;

import iap.blocks.extraction.Trait;
import iap.blocks.extraction.TraitCategory;
import iap.pipelines.ImageProcessorOptionsAndResults.CameraPosition;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionScriptBasedDataProcessing;
import de.ipk.ag_ba.image.structures.CameraType;

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
				new Trait(CameraPosition.SIDE, CameraType.VIS, TraitCategory.GEOMETRY, "height.norm") + ":Height (zoom corrected) // " +
						new Trait(CameraPosition.SIDE, CameraType.VIS, TraitCategory.GEOMETRY, "height") + ":Height",
				new Trait(CameraPosition.SIDE, CameraType.VIS, TraitCategory.GEOMETRY, "area.norm") + ":Side Vis Area (normalized) // " +
						new Trait(CameraPosition.SIDE, CameraType.VIS, TraitCategory.GEOMETRY, "area") + ":Side Vis Area",
				new Trait(CameraPosition.SIDE, CameraType.FLUO, TraitCategory.INTENSITY, "intensity.classic.mean") + ":Fluorescence intensity (side)",
				new Trait(CameraPosition.SIDE, CameraType.NIR, TraitCategory.INTENSITY, "intensity.mean") + ":Near-infrared intensity (side)",
				new Trait(CameraPosition.TOP, CameraType.FLUO, TraitCategory.INTENSITY, "intensity.classic.mean") + ":Fluoresence intensity (top)",
				new Trait(CameraPosition.TOP, CameraType.NIR, TraitCategory.INTENSITY, "intensity.mean") + ":Near-Infrared intensity (top)",
				new Trait(CameraPosition.SIDE, CameraType.VIS, TraitCategory.INTENSITY, "hsv.h.mean") + ":Average hue (side)",
				new Trait(CameraPosition.TOP, CameraType.VIS, TraitCategory.INTENSITY, "hsv.h.mean") + ":Average hue (top)",
				new Trait(CameraPosition.SIDE, CameraType.VIS, TraitCategory.GEOMETRY, "width.norm") + ":Width (zoom corrected) // "
						+ new Trait(CameraPosition.SIDE, CameraType.VIS, TraitCategory.GEOMETRY, "width") + ":Width",
				new Trait(CameraPosition.TOP, CameraType.VIS, TraitCategory.GEOMETRY, "area.norm") + ":Top Vis Area (zoom corrected) // "
						+ new Trait(CameraPosition.TOP, CameraType.VIS, TraitCategory.GEOMETRY, "area") + ":Top Vis Area",
				new Trait(CameraPosition.COMBINED, CameraType.FLUO, TraitCategory.GEOMETRY, "volume.iap") + ":Volume estimation (fluo)",
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
