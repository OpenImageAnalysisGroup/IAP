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

/**
 * @author klukas
 */
public interface AnnotationProvider {
	
	/**
	 * @return
	 */
	String getTitle();
	
	/**
	 * @param workload
	 */
	LinkedHashMap<String, LinkedHashSet<String>> processWorkload(ArrayList<String> workload);
	
	/**
	 * @return
	 */
	boolean requestUserData();
	
}
