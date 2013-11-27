/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.07.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti;

import javax.swing.ImageIcon;

import org.ErrorMsg;
import org.graffiti.plugin.EditorPluginAdapter;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class DBE_EditorPluginAdapter extends EditorPluginAdapter {
	@Override
	public ImageIcon getIcon() {
		try {
			ClassLoader cl = this.getClass().getClassLoader();
			String path = IPK_PluginAdapter.class.getPackage().getName().replace('.', '/');
			ImageIcon icon = new ImageIcon(cl.getResource(path + "/dbe_logo_16x16.png"));
			return icon;
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
			return super.getIcon();
		}
	}
}
