package de.ipk.ag_ba.commands.experiment.process;

import org.IniIoProvider;
import org.SystemAnalysis;
import org.SystemOptions;
import org.apache.commons.lang3.StringEscapeUtils;
import org.graffiti.editor.MainFrame;

import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;

public class ExperimentAnalysisSettingsIOprovder implements IniIoProvider {
	private final ExperimentReference experimentReference;
	private final MongoDB m;
	
	public ExperimentAnalysisSettingsIOprovder(ExperimentReference experimentReference, MongoDB m) {
		this.experimentReference = experimentReference;
		this.m = m;
	}
	
	private SystemOptions i;
	private long storedLastUpdateTime;
	
	@Override
	public String getString() {
		try {
			m.updateAndGetExperimentHeaderInfoFromDB(experimentReference.getHeader());
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		String ini = experimentReference.getHeader().getSettings();
		StringEscapeUtils.unescapeXml(ini);
		return ini;
	}
	
	@Override
	public Long setString(String value) {
		Long res = null;
		String ini = StringEscapeUtils.escapeXml(value);
		experimentReference.getHeader().setSettings(ini);
		
		if (m != null) {
			try {
				res = m.saveExperimentHeader(experimentReference.getHeader());
				System.out.println(SystemAnalysis.getCurrentTime()
						+ ">Saved changed settings for "
						+ experimentReference.getExperimentName()
						+ " in storage location "
						+ m.getDatabaseName() + ".");
			} catch (Exception e) {
				e.printStackTrace();
				MainFrame.showMessageDialog("Could not save changed settings: " + e.getMessage(), "Error");
			}
		} else {
			System.err.println("WARNING: Could not store changed INI-Provider settings!");
		}
		return res;
	}
	
	@Override
	public void setInstance(SystemOptions i) {
		this.i = i;
	}
	
	@Override
	public SystemOptions getInstance() {
		return i;
	}
	
	@Override
	public Long lastModified() throws Exception {
		return m.getExperimentHeaderStorageTime(experimentReference.getHeader());
	}
	
	@Override
	public long storedLastUpdateTime() {
		return storedLastUpdateTime;
	}
	
	@Override
	public void setStoredLastUpdateTime(long storedLastUpdateTime) {
		this.storedLastUpdateTime = storedLastUpdateTime;
	}
}