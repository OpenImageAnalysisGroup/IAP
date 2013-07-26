package de.ipk.ag_ba.commands;

import de.ipk.ag_ba.commands.datasource.AbstractUrlNavigationAction;

public abstract class AbstractGraphUrlNavigationAction extends AbstractUrlNavigationAction {
	
	protected boolean loadDirect = false;
	
	public AbstractGraphUrlNavigationAction(String tooltip) {
		super(tooltip);
	}
	
	public void setLoadDirectlyInNavigator() {
		this.loadDirect = true;
	}
	
}
