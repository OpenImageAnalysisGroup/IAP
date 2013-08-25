package de.ipk.ag_ba.commands.experiment.process;

import java.util.ArrayList;

import org.SystemAnalysis;
import org.SystemOptions;
import org.apache.commons.lang3.StringEscapeUtils;
import org.graffiti.editor.MainFrame;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;

public class ActionAssignAnalysisTemplate extends AbstractNavigationAction implements NavigationAction {
	
	private String iniFileName;
	private String title;
	private ExperimentReference exp;
	
	public ActionAssignAnalysisTemplate(String tooltip) {
		super(tooltip);
	}
	
	public ActionAssignAnalysisTemplate(ExperimentReference exp,
			String iniFileName, String tooltip, String title) {
		this(tooltip);
		this.exp = exp;
		this.iniFileName = iniFileName;
		this.title = title;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		String ini = SystemOptions.getInstance(iniFileName, null).getIniValue();
		ini = StringEscapeUtils.escapeXml(ini);
		exp.getHeader().setSettings(ini);
		if (exp.m != null) {
			try {
				exp.m.saveExperimentHeader(exp.getHeader());
				System.out.println(SystemAnalysis.getCurrentTime()
						+ ">Saved changed settings for "
						+ exp.getExperimentName()
						+ " in storage location "
						+ exp.m.getDatabaseName() + ".");
				if (exp.getIniIoProvider() != null && exp.getIniIoProvider().getInstance() != null)
					exp.getIniIoProvider().getInstance().reload();
				else
					exp.setIniIoProvider(new ExperimentAnalysisSettingsIOprovder(exp.getHeader(), exp.m));
			} catch (Exception e) {
				e.printStackTrace();
				MainFrame.showMessageDialog("Could not save changed settings: " + e.getMessage(), "Error");
			}
		} else {
			if (exp.getHeader().getExperimentHeaderHelper() != null) {
				exp.getHeader().getExperimentHeaderHelper().saveUpdatedProperties(status);
			}
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;// new ArrayList<NavigationButton>();
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent("Data from '" + iniFileName + "' has been assigned to experiment '"
				+ exp.getExperimentName() + "'.");
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public boolean isProvidingActions() {
		return false;
	}
	
	@Override
	public String getDefaultTitle() {
		return title;
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Insert-Object-64.png";
	}
	
}
