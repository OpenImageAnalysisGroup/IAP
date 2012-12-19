package de.ipk.ag_ba.gui;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import org.IoStringProvider;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.SystemOptions;

public class PipelineDesc {
	
	private final String iniFileName;
	private final IoStringProvider iniIO;
	
	public PipelineDesc(String iniFileName, IoStringProvider iniIO, String defName,
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
	
	private static String barleyFN = StringManipulationTools.getFileSystemName("Barley Analysis") + ".pipeline.ini";
	
	public static PipelineDesc getPipelineDefault() {
		return new PipelineDesc(
				barleyFN, null,
				"Barley Analysis", "Analyze Phenotype (Barley)");
	}
	
	public static ArrayList<PipelineDesc> getSavedPipelineTemplates() {
		ArrayList<PipelineDesc> res = new ArrayList<PipelineDesc>();
		// res.add(PipelineDesc.getPipelineDefault());
		FilenameFilter ff = new FilenameFilter() {
			@Override
			public boolean accept(File f, String name) {
				boolean a = name.endsWith(".pipeline.ini") && !name.equals(barleyFN);
				return a;
			}
		};
		for (String fn : new File(ReleaseInfo.getAppFolder()).list(ff)) {
			String fnt = StringManipulationTools.stringReplace(fn, ".pipeline.ini", "");
			res.add(new PipelineDesc(fn, null, fnt, fnt));
		}
		return res;
	}
	
	public String getIniFileName() {
		return iniFileName;
	}
	
	public IoStringProvider getIniIO() {
		return iniIO;
	}
}
