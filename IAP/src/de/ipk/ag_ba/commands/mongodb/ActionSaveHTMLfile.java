package de.ipk.ag_ba.commands.mongodb;

import java.io.File;
import java.util.ArrayList;

import org.AttributeHelper;
import org.ReleaseInfo;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;

public class ActionSaveHTMLfile extends AbstractNavigationAction {
	
	private final String content;
	private final String fileName;
	
	public ActionSaveHTMLfile(String tooltip, String fileName, String content) {
		super(tooltip);
		this.fileName = fileName;
		this.content = content;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		TextFile tf = new TextFile();
		tf.add(content);
		String desktop = ReleaseInfo.getDesktopFolder();
		tf.write(desktop + File.separator + fileName);
		AttributeHelper.showInFileBrowser(desktop, fileName);
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
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
		return "Save Result Table";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Document-Save-64.png";
	}
}
