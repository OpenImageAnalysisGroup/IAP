package de.ipk.ag_ba.commands.datasource;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.gui.URLprovider;

public abstract class AbstractUrlNavigationAction extends AbstractNavigationAction implements URLprovider {
	
	public AbstractUrlNavigationAction(String tooltip) {
		super(tooltip);
	}
	
}
