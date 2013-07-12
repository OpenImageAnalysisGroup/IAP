/*
 * Created on 02.12.2003
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package org.graffiti.plugins.modes.defaultEditMode;

import java.util.ArrayList;

import org.graffiti.plugin.mode.AbstractMode;
import org.graffiti.plugin.mode.GraphConstraint;
import org.graffiti.plugin.tool.Tool;

/**
 * @author pick
 *         To change the template for this generated type comment go to
 *         Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class DefaultEditMode extends AbstractMode {
	public final static String sid = "org.graffiti.plugins.modes.defaultEditMode";
	
	public DefaultEditMode() {
		this.id = sid;
		this.constraints = new GraphConstraint[0];
		this.tools = new ArrayList<Tool>();
	}
}
