package de.ipk.ag_ba.commands;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.Timer;

import de.ipk.ag_ba.datasources.http_folder.NavigationImage;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.gui.webstart.IAPmain;

class ActionScreenshotStorage extends AbstractNavigationAction {
	ActionScreenshotStorage(String tooltip) {
		super(tooltip);
	}
	
	@Override
	public String getDefaultTitle() {
		boolean enabled = Other.globalScreenshotStorage.getBval(0, false);
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
		boolean enabled = Other.globalScreenshotStorage.getBval(0, false);
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
	public boolean getProvidesActions() {
		return false;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return currentSet;
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		boolean enabled = Other.globalScreenshotStorage.getBval(0, false);
		enabled = !enabled;
		Other.globalScreenshotStorage.setBval(0, enabled);
		if (Other.globalScreenshotTimer == null) {
			Timer t = new Timer(1000, new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent arg0) {
					boolean enabled = Other.globalScreenshotStorage.getBval(0, false);
					if (enabled) {
						IAPservice.storeDesktopImage(true);
						System.out.println("SCREENSHOT");
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