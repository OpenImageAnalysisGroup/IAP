package de.ipk.ag_ba.commands.about;

import java.util.ArrayList;

import javax.swing.JButton;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;

/**
 * @author Christian Klukas
 */
public class ActionAddOnManager extends AbstractNavigationAction {
	
	private final JButton addonManagerButton;
	
	public ActionAddOnManager(String tooltip, JButton addonManagerButton) {
		super(tooltip);
		this.addonManagerButton = addonManagerButton;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		// empty
		BackgroundTaskHelper.executeLaterOnSwingTask(200, new Runnable() {
			@Override
			public void run() {
				addonManagerButton.doClick();
			}
		});
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		ArrayList<String> ll = new ArrayList<String>();
		ll.add("<html><h2><font face=\"Sans,Tohama,Arial\">Add-on Manager</font></h2>" +
				"<font face=\"Sans,Tohama,Arial\">" +
				"IAP supports the development and loading of Add-ons. An Add-on is a bundle of plugins<br>" +
				"(technically a JAR file with content structured according to the IAP/VANTED API).<br>" +
				"Add-ons are installed and added to the system by the user, according to his/her needs.<br>" +
				"<br>" +
				"The IAP and the embedded VANTED system is comprised of system components, developed as<br>" +
				"individual plugins. An extensive list of included developer libraries can be used<br>" +
				"during plugin-development, including the embedded ImageJ functionality. In addition,<br>" +
				"Add-ons may bundle additional libraries.<br>" +
				"<br>" +
				"If you would like to develop a custom extension, consult the developer documentation (" +
				"<a href=\"http://iap.ipk-gatersleben.de/#development\">link</a>).</font>");
		
		MainPanelComponent mp = new MainPanelComponent(ll);
		return mp;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Add-on Manager";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/addon-icon2.png";
	}
	
	@Override
	public boolean isProvidingActions() {
		return false;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
}
