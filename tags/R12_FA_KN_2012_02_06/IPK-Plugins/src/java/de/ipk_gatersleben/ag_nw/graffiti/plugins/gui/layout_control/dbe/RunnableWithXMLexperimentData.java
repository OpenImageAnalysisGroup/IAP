/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 08.10.2004
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

/**
 * @author klukas
 */
public interface RunnableWithXMLexperimentData extends Runnable {
	/**
	 * @param doc
	 */
	void setExperimenData(ExperimentInterface doc);
}
