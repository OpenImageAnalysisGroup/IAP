package de.ipk.ag_ba.commands.experiment.process;

import org.ExperimentHeaderHelper;
import org.IniIoProvider;
import org.SystemAnalysis;
import org.SystemOptions;
import org.apache.commons.lang3.StringEscapeUtils;
import org.graffiti.editor.MainFrame;

import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

public class ExperimentAnalysisSettingsIOprovder implements IniIoProvider {
	private final ExperimentHeaderInterface header;
	private final MongoDB m;
	private final ExperimentHeaderHelper headerHelper;
	
	public ExperimentAnalysisSettingsIOprovder(ExperimentHeaderInterface header, MongoDB m) {
		this.header = header;
		this.m = m;
		this.headerHelper = header.getExperimentHeaderHelper();
	}
	
	private SystemOptions i;
	private long storedLastUpdateTime;
	
	@Override
	public String getString() {
		try {
			if (m != null) {
				m.updateAndGetExperimentHeaderInfoFromDB(header);
			} else {
				if (headerHelper != null)
					headerHelper.readSourceForUpdate();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		String ini = header.getSettings();
		StringEscapeUtils.unescapeXml(ini);
		return ini;
	}
	
	@Override
	public Long setString(String value) {
		Long res = null;
		String ini = StringEscapeUtils.escapeXml(value);
		header.setSettings(ini);
		
		try {
			if (m != null) {
				res = m.saveExperimentHeader(header);
				System.out.println(SystemAnalysis.getCurrentTime()
						+ ">Saved changed settings for "
						+ header.getExperimentname()
						+ " in storage location "
						+ m.getDatabaseName() + ".");
			} else {
				if (header.getExperimentHeaderHelper() != null) {
					res = header.getExperimentHeaderHelper().saveUpdatedProperties(null);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			MainFrame.showMessageDialog("Could not save changed settings: " + e.getMessage(), "Error");
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
		try {
			if (m != null)
				return m.getExperimentHeaderStorageTime(header);
			else {
				if (headerHelper != null) {
					return headerHelper.getLastModified();
				} else
					return null;
			}
		} catch (Exception e) {
			return null;
		}
	}
	
	@Override
	public long storedLastUpdateTime() {
		return storedLastUpdateTime;
	}
	
	@Override
	public void setStoredLastUpdateTime(long storedLastUpdateTime) {
		this.storedLastUpdateTime = storedLastUpdateTime;
	}
	
	@Override
	public boolean isAbleToSaveData() {
		if (m != null)
			return true;
		else
			if (headerHelper != null)
				return headerHelper.isAbleToSaveData();
			else
				return false;
	}
}