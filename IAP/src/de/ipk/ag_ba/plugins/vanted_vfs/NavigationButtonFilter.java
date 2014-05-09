package de.ipk.ag_ba.plugins.vanted_vfs;

import de.ipk.ag_ba.gui.navigation_model.NavigationButton;

public interface NavigationButtonFilter {
	
	boolean accept(NavigationButton nb);
	
}
