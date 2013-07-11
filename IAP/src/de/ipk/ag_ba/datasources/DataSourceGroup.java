package de.ipk.ag_ba.datasources;

import java.util.Collection;

import de.ipk.ag_ba.gui.interfaces.NavigationAction;

public interface DataSourceGroup {
	
	String getTitle();
	
	String getTooltip();
	
	String getIntroductionText();
	
	String getImage();
	
	String getNavigationImage();
	
	Collection<NavigationAction> getAdditionalActions();
	
}
