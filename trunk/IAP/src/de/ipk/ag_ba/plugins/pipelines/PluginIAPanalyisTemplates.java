package de.ipk.ag_ba.plugins.pipelines;

import de.ipk.ag_ba.plugins.AbstractIAPplugin;
import de.ipk.ag_ba.plugins.pipelines.arabidopsis.ArabidopsisPipeline;
import de.ipk.ag_ba.plugins.pipelines.barley.BarleyPipeline;
import de.ipk.ag_ba.plugins.pipelines.maize.MaizePipeline;
import de.ipk.ag_ba.plugins.pipelines.roots.RootScannPipeline;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

/**
 * @author Christian Klukas
 */
public class PluginIAPanalyisTemplates extends AbstractIAPplugin {
	
	public PluginIAPanalyisTemplates() {
		System.out.println(SystemAnalysis.getCurrentTime() + ">INFO: IAP analysis templates plugin is beeing loaded");
	}
	
	@Override
	public AnalysisPipelineTemplate[] getAnalysisTemplates() {
		return new AnalysisPipelineTemplate[] {
				new MaizePipeline(),
				new BarleyPipeline(),
				new ArabidopsisPipeline(),
				new RootScannPipeline()
		};
	}
}