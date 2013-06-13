package de.ipk.ag_ba.gui;

import iap.pipelines.ImageProcessorOptions;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import org.IniIoProvider;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemOptions;

import de.ipk.ag_ba.plugins.IAPpluginManager;
import de.ipk.ag_ba.plugins.pipelines.AnalysisPipelineTemplate;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.all.AbstractPhenotypingTask;

public class PipelineDesc {
	
	private final String iniFileName;
	private final IniIoProvider iniIO;
	private final SystemOptions so;
	
	public PipelineDesc(String iniFileName, IniIoProvider iniIO, String defName,
			String defDescription) {
		this.iniFileName = iniFileName;
		this.iniIO = iniIO;
		so = SystemOptions.getInstance(iniFileName, iniIO);
		so.getString("DESCRIPTION", "pipeline_name", defName);
		so.getString("DESCRIPTION", "pipeline_description", defDescription);
	}
	
	public String getTooltip() {
		SystemOptions so = SystemOptions.getInstance(iniFileName, iniIO);
		String description = so.getString("DESCRIPTION", "pipeline_description", "(pipeline description missing)");
		return description;
	}
	
	public String getName() {
		SystemOptions so = SystemOptions.getInstance(iniFileName, iniIO);
		String name = so.getString("DESCRIPTION", "pipeline_name", "(pipeline name missing)");
		return name;
	}
	
	// private static String barleyFN = "Barley_Analysis.pipeline.ini";
	// public static PipelineDesc getPipelineDefault() {
	// return new PipelineDesc(
	// barleyFN, null,
	// "Barley Analysis", "Analyze Phenotype (Barley)");
	// }
	
	public static ArrayList<PipelineDesc> getSavedPipelineTemplates() throws Exception {
		
		writePipelineInis();
		
		ArrayList<PipelineDesc> res = new ArrayList<PipelineDesc>();
		// res.add(PipelineDesc.getPipelineDefault());
		FilenameFilter ff = new FilenameFilter() {
			@Override
			public boolean accept(File f, String name) {
				boolean a = name.endsWith(".pipeline.ini");
				return a;
			}
		};
		for (String fn : new File(ReleaseInfo.getAppFolder()).list(ff)) {
			String fnt = StringManipulationTools.stringReplace(fn, ".pipeline.ini", "");
			res.add(new PipelineDesc(fn, null, fnt, fnt));
		}
		return res;
	}
	
	private static void writePipelineInis() throws Exception {
		for (final AnalysisPipelineTemplate template : IAPpluginManager.getInstance().getAnalysisTemplates()) {
			PipelineDesc pd = new PipelineDesc(
					StringManipulationTools.getFileSystemName(template.getTitle()) + ".pipeline.ini",
					null,
					template.getTitle(),
					template.getDescription());
			AbstractPhenotypingTask pt = new TemplatePhenotypingTask(pd, template);
			pt.getImageProcessor().getPipeline(new ImageProcessorOptions(pd.getOptions()));
		}
	}
	
	public SystemOptions getOptions() {
		return so;
	}
	
	public String getIniFileName() {
		return StringManipulationTools.getFileSystemName(iniFileName);
	}
	
	public IniIoProvider getIniIO() {
		return iniIO;
	}
}
