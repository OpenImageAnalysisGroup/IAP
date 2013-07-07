/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Apr 21, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import javax.swing.JButton;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

/**
 * @author klukas
 */
public interface AnnotationProvider {
	
	String getTitle();
	
	LinkedHashMap<String, LinkedHashSet<String>> processWorkload(ArrayList<String> workload);
	
	boolean requestUserData(ExperimentInterface expdata);
	
	JButton getButton(ExperimentInterface md, ExperimentDataInfoPane resultPane);
	
}
