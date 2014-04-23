/*
 * Created on 02.12.2003
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.graffiti.plugins.modes.defaultEditMode;

import org.graffiti.plugin.EditorPluginAdapter;
import org.graffiti.plugin.mode.Mode;

/**
 * @author pick
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DefaultEditModePlugin extends EditorPluginAdapter {
	public DefaultEditModePlugin() {
		this.modes = new Mode[1];
		modes[0] = new DefaultEditMode();
	}
}
