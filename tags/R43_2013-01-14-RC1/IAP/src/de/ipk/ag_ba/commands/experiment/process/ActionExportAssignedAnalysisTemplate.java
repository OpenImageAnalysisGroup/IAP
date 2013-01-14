package de.ipk.ag_ba.commands.experiment.process;

import java.util.ArrayList;

import org.AttributeHelper;
import org.IniIoProvider;
import org.ReleaseInfo;
import org.SystemAnalysis;
import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.interfaces.NavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.gui.webstart.IAPrunMode;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

public class ActionExportAssignedAnalysisTemplate extends AbstractNavigationAction implements NavigationAction {
	
	private String title;
	private IniIoProvider ini;
	private String exportFileName;
	
	public ActionExportAssignedAnalysisTemplate(String tooltip) {
		super(tooltip);
	}
	
	public ActionExportAssignedAnalysisTemplate(IniIoProvider ini, String exportFileName,
			String tooltip, String title) {
		this(tooltip);
		this.ini = ini;
		this.exportFileName = exportFileName;
		this.title = title;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		String iniContent = SystemOptions.getInstance(null, ini).getIniValue();
		TextFile.write(ReleaseInfo.getAppFolderWithFinalSep() + exportFileName, iniContent);
		if (IAPmain.getRunMode() == IAPrunMode.SWING_APPLET || IAPmain.getRunMode() == IAPrunMode.SWING_MAIN)
			AttributeHelper.showInFileBrowser(ReleaseInfo.getAppFolder(), exportFileName);
		else
			System.out.println(SystemAnalysis.getCurrentTime() + ">Saved data in " + ReleaseInfo.getAppFolderWithFinalSep() + exportFileName);
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;// new ArrayList<NavigationButton>();
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent("Data has been exported to '" + exportFileName + "'.");
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
