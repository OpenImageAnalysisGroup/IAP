package de.ipk_gatersleben.ag_nw.graffiti.plugins.addons;

import org.ErrorMsg;

import de.ipk_gatersleben.ag_nw.graffiti.IPK_EditorPluginAdapter;

/**
 * @author Hendrik Rohn, Christian Klukas
 */
public abstract class AddonAdapter extends IPK_EditorPluginAdapter {
	
	public AddonAdapter() {
		super();
		try {
			initializeAddon();
		} catch (Exception e) {
			ErrorMsg.addErrorMessage("<html>Exception occured when initializing Add-on \"" + getClass().getCanonicalName() + "\":<br>" + e.getLocalizedMessage());
			e.printStackTrace();
		}
	}
	
	protected abstract void initializeAddon();
}