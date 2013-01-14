package de.ipk.ag_ba.gui;

import iap.pipelines.ImageProcessorOptions;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import org.IniIoProvider;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemOptions;

import de.ipk.ag_ba.server.analysis.image_analysis_tasks.arabidopsis.ArabidopsisAnalysisSmallBlueRubberTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.arabidopsis.ArabidopsisAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.barley.BarleyAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.MaizeAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.roots.RootsAnalysisTask;

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
		{
			PipelineDesc pRoot = new PipelineDesc(
					StringManipulationTools.getFileSystemName(RootsAnalysisTask.DEFAULT_NAME) + ".pipeline.ini",
					null,
					RootsAnalysisTask.DEFAULT_NAME,
					RootsAnalysisTask.DEFAULT_DESC);
			RootsAnalysisTask rt = new RootsAnalysisTask(pRoot);
			rt.getImageProcessor().getPipeline(new ImageProcessorOptions(pRoot.getOptions()));
		}
		{
			PipelineDesc pBarley = new PipelineDesc(
					StringManipulationTools.getFileSystemName(BarleyAnalysisTask.DEFAULT_NAME) + ".pipeline.ini",
					null,
					BarleyAnalysisTask.DEFAULT_NAME,
					BarleyAnalysisTask.DEFAULT_DESC);
			BarleyAnalysisTask bt = new BarleyAnalysisTask(pBarley);
			bt.getImageProcessor().getPipeline(new ImageProcessorOptions(pBarley.getOptions()));
		}
		{
			PipelineDesc pMaize = new PipelineDesc(
					StringManipulationTools.getFileSystemName(MaizeAnalysisTask.DEFAULT_NAME) + ".pipeline.ini",
					null,
					MaizeAnalysisTask.DEFAULT_NAME,
					MaizeAnalysisTask.DEFAULT_DESC);
			MaizeAnalysisTask mt = new MaizeAnalysisTask(pMaize);
			mt.getImageProcessor().getPipeline(new ImageProcessorOptions(pMaize.getOptions()));
		}
		{
			PipelineDesc pPhyto = new PipelineDesc(
					StringManipulationTools.getFileSystemName(ArabidopsisAnalysisTask.DEFAULT_NAME) + ".pipeline.ini",
					null,
					ArabidopsisAnalysisTask.DEFAULT_NAME,
					ArabidopsisAnalysisTask.DEFAULT_DESC);
			ArabidopsisAnalysisTask pt = new ArabidopsisAnalysisTask(pPhyto);
			pt.getImageProcessor().getPipeline(new ImageProcessorOptions(pPhyto.getOptions()));
		}
		{
			PipelineDesc pBrPhyto = new PipelineDesc(
					StringManipulationTools.getFileSystemName(ArabidopsisAnalysisSmallBlueRubberTask.DEFAULT_NAME) + ".pipeline.ini",
					null,
					ArabidopsisAnalysisSmallBlueRubberTask.DEFAULT_NAME,
					ArabidopsisAnalysisSmallBlueRubberTask.DEFAULT_DESC);
			ArabidopsisAnalysisSmallBlueRubberTask pbt = new ArabidopsisAnalysisSmallBlueRubberTask(pBrPhyto);
			pbt.getImageProcessor().getPipeline(new ImageProcessorOptions(pBrPhyto.getOptions()));
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
