package de.ipk.ag_ba.gui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemOptions;

public class PipelineDesc {
	
	private final String iniFileName;
	
	public PipelineDesc(String iniFileName, String defName, String defDescription) {
		this.iniFileName = iniFileName;
		SystemOptions so = SystemOptions.getInstance(iniFileName);
		so.getString("DESCRIPTION", "pipeline_name", defName);
		so.getString("DESCRIPTION", "pipeline_description", defDescription);
	}
	
	public String getTooltip() {
		SystemOptions so = SystemOptions.getInstance(iniFileName);
		String description = so.getString("DESCRIPTION", "pipeline_description", "(pipeline description missing)");
		return description;
	}
	
	public String getName() {
		SystemOptions so = SystemOptions.getInstance(iniFileName);
		String name = so.getString("DESCRIPTION", "pipeline_name", "(pipeline name missing)");
		return name;
	}
	
	private static String barleyFN = StringManipulationTools.getFileSystemName("Barley Analysis") + ".pipeline.ini";
	
	public static PipelineDesc getPipelineDefault() {
		return new PipelineDesc(
				barleyFN,
				"Barley Analysis", "Analyze Phenotype (Barley)");
	}
	
	public static ArrayList<PipelineDesc> getKnownPipelines() {
		ArrayList<PipelineDesc> res = new ArrayList<PipelineDesc>();
		res.add(PipelineDesc.getPipelineDefault());
		FilenameFilter ff = new FilenameFilter() {
			@Override
			public boolean accept(File f, String name) {
				boolean a = name.endsWith(".pipeline.ini") && !name.equals(barleyFN);
				return a;
			}
		};
		for (String fn : new File(ReleaseInfo.getAppFolder()).list(ff)) {
			String fnt = StringManipulationTools.stringReplace(fn, ".pipeline.ini", "");
			res.add(new PipelineDesc(fn, fnt, fnt));
		}
		return res;
	}
	
	public String getIniFileName() {
		return iniFileName;
	}
}
