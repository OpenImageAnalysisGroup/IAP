package de.ipk_gatersleben.ag_nw.graffiti.plugins.addons;

import javax.swing.ImageIcon;

import org.ErrorMsg;
import org.graffiti.editor.GravistoService;
import org.graffiti.plugin.GenericPluginAdapter;

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
	
	@Override
	public ImageIcon getIcon() {
		try {
			ImageIcon icon = new ImageIcon(GravistoService.getResource(GenericPluginAdapter.class, "addon-icon", "png"));
			return icon;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return super.getIcon();
		}
	}
}