/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Jun 2, 2010 by Christian Klukas
 */
package de.ipk.ag_ba.gui.interfaces;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * @author klukas
 */
public interface RunnableWithExperimentInfo {
	
	void run(ExperimentHeaderInterface newProperties) throws Exception;
	
}
