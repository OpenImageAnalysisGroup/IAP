/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Dec 3, 2009 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.pathway_references;

import java.util.ArrayList;

import org.StringManipulationTools;

/**
 * @author klukas
 */
public class DrawingGroup {
	public String group;
	public ArrayList<Boolean> draw;
	
	DrawingGroup(String group, ArrayList<Boolean> draw) {
		this.group = pretifyPathwayName(group);
		this.draw = draw;
	}
	
	private String pretifyPathwayName(String pathwayName) {
		pathwayName = StringManipulationTools.stringReplace(pathwayName, "filepath|", "");
		pathwayName = StringManipulationTools.stringReplace(pathwayName, "%32", " ");
		pathwayName = StringManipulationTools.stringReplace(pathwayName, "%40", "(");
		pathwayName = StringManipulationTools.stringReplace(pathwayName, "%41", ")");
		pathwayName = StringManipulationTools.stringReplace(pathwayName, "%43", "+");
		pathwayName = StringManipulationTools.stringReplace(pathwayName, "%46", ".");
		pathwayName = StringManipulationTools.stringReplace(pathwayName, "%44", ",");
		pathwayName = StringManipulationTools.stringReplace(pathwayName, "%45", "-");
		pathwayName = StringManipulationTools.stringReplace(pathwayName, ".gml", "");
		return pathwayName.trim();
	}
}
