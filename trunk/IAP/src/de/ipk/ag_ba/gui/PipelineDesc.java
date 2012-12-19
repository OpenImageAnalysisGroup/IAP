package de.ipk.ag_ba.gui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import org.IniIoProvider;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemOptions;

import de.ipk.ag_ba.server.analysis.image_analysis_tasks.PhytochamberAnalysisBlueRubberTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.PhytochamberAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.barley.BarleyAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.maize.MaizeAnalysisTask;
import de.ipk.ag_ba.server.analysis.image_analysis_tasks.roots.RootsAnalysisTask;

public class PipelineDesc {
	
	private final String iniFileName;
	private final IniIoProvider iniIO;
	
	public PipelineDesc(String iniFileName, IniIoProvider iniIO, String defName,
			String defDescription) {
		this.iniFileName = iniFileName;
		this.iniIO = iniIO;
		SystemOptions so = SystemOptions.getInstance(iniFileName, iniIO);
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
	
	public static ArrayList<PipelineDesc> getSavedPipelineTemplates() {
		
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
	
	private static void writePipelineInis() {
		RootsAnalysisTask rt = new RootsAnalysisTask();
		PipelineDesc pRoot = new PipelineDesc(rt.getName(), null, rt.getName(), rt.getTaskDescription());
		
		BarleyAnalysisTask bt = new BarleyAnalysisTask();
		PipelineDesc pBarley = new PipelineDesc(bt.getName(), null, bt.getName(), bt.getTaskDescription());
		
		MaizeAnalysisTask mt = new MaizeAnalysisTask();
		PipelineDesc pMaize = new PipelineDesc(mt.getName(), null, mt.getName(), mt.getTaskDescription());
		
		PhytochamberAnalysisTask pt = new PhytochamberAnalysisTask();
		PipelineDesc pPhyto = new PipelineDesc(pt.getName(), null, pt.getName(), pt.getTaskDescription());
		
		PhytochamberAnalysisBlueRubberTask pbt = new PhytochamberAnalysisBlueRubberTask();
		PipelineDesc pBrPhyto = new PipelineDesc(pbt.getName(), null, pbt.getName(), pbt.getTaskDescription());
	}
	
	public String getIniFileName() {
		return StringManipulationTools.getFileSystemName(iniFileName);
	}
	
	public IniIoProvider getIniIO() {
		return iniIO;
	}
}
