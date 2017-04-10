package de.ipk.ag_ba.commands.control;

import java.util.ArrayList;

import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;

public class SettingStringEditorAction extends AbstractNavigationAction {
	
	private String section;
	private String settingAuto;
	private String titlePrefix;
	private CaptureLiveView cl;
	
	public SettingStringEditorAction(String tooltip) {
		super(tooltip);
	}
	
	public SettingStringEditorAction(String section, String settingAuto, String titlePrefix, CaptureLiveView cl) {
		this("Modify barcode detection settings");
		this.section = section;
		this.settingAuto = settingAuto;
		this.titlePrefix = titlePrefix;
		this.cl = cl;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		Object[] res = MyInputHelper.getInput("Enable auto-barcode detection or enter a sample ID:", "Sample ID",
				new Object[] {
						"Sample ID", cl.getCurrentBarcode(),
						"Auto-detection", SystemOptions.getInstance().getBoolean(section, settingAuto, false)
				});
		if (res != null) {
			cl.setCurrentBarcode((String) res[0]);
			SystemOptions.getInstance().setBoolean(section, settingAuto, (boolean) res[1]);
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return true;
	}
	
	private String getBarcode() {
		return org.StringManipulationTools.trimString(cl.getCurrentBarcode(), 20);
	}
	
	@Override
	public String getDefaultTitle() {
		if (SystemOptions.getInstance().getBoolean(section, settingAuto, false))
			return titlePrefix + "<font color='#AA2222'>" + getBarcode();
		else
			return titlePrefix + getBarcode();
	}
	
	@Override
	public String getDefaultImage() {
		if (SystemOptions.getInstance().getBoolean(section, settingAuto, false))
			return "img/ext/gpl2/Gnome-Zoom-Fit-Best-64.png";
		else
			return "img/ext/gpl2/Gnome-Format-Text-Direction-Rtl-64.png";
	}
	
}
