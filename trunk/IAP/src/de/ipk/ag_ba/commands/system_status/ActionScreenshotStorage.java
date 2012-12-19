package de.ipk.ag_ba.commands.system_status;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Timer;

import org.SystemAnalysis;
import org.SystemOptions;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.Other;
import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.gui.webstart.IAPmain;

public class ActionScreenshotStorage extends AbstractNavigationAction {
	public ActionScreenshotStorage(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public String getDefaultTitle() {
		boolean enabled = SystemOptions.getInstance().getBoolean("Watch-Service", "Screenshot//Publish Desktop", false);
		return enabled ? "Screenshot Storage Enabled" : "Screenshot Storage Disabled";
	}
	
	@Override
	public NavigationImage getImageIconInactive() {
		return getImageIconActive();
	}
	
	@Override
	public NavigationImage getImageIconActive() {
		return IAPmain.loadIcon(getDefaultImage());
	}
	
	@Override
	public String getDefaultImage() {
		boolean enabled = SystemOptions.getInstance().getBoolean("Watch-Service", "Screenshot//Publish Desktop", false);
		return enabled ? "img/ext/gpl2/Gnome-Camera-Web-64.png" : "img/ext/gpl2/Gnome-Camera-Web-64_off.png";
	}
	
	@Override
	public String getDefaultNavigationImage() {
		return getDefaultImage();
	}
	
	@Override
	public boolean requestTitleUpdates() {
		return true;
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
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		boolean enabled = SystemOptions.getInstance().getBoolean("Watch-Service", "Screenshot//Publish Desktop", false);
		enabled = !enabled;
		SystemOptions.getInstance().setBoolean("Watch-Service", "Screenshot//Publish Desktop", enabled);
		if (Other.globalScreenshotTimer == null) {
			int intervall = 1000 * SystemOptions.getInstance().getInteger("Watch-Service", "Screenshot//Screenshot-Intervall_sec", 60);
			if (intervall < 0) {
				SystemOptions.getInstance().setInteger("Watch-Service", "Screenshot//Screenshot-Intervall_sec", 60);
				intervall = 1000;
			}
			Timer t = new Timer(intervall, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					boolean enabled = SystemOptions.getInstance().getBoolean("Watch-Service", "Screenshot//Publish Desktop", false);
					if (enabled) {
						IAPservice.storeDesktopImage(true);
						System.out.println(SystemAnalysis.getCurrentTimeInclSec() + ">SCREENSHOT");
					}
				}
			});
			t.setInitialDelay(100);
			t.setRepeats(true);
			t.start();
			Other.globalScreenshotTimer = t;
		}
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
}