/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes;

public enum RelationType
					implements ProvidesExplanation {
	ECrel, PPrel, GErel, PCrel, maplink;
	
	public String getExplanation() {
		if (this == ECrel)
			return "enzyme-enzyme relation, indicating two enzymes catalyzing successive reaction steps";
		if (this == PPrel)
			return "protein-protein interaction, such as binding and modification";
		if (this == GErel)
			return "gene expression interaction, indicating relation of transcription factor and target gene product";
		if (this == PCrel)
			return "protein-compound interaction";
		if (this == maplink)
			return "link to another map";
		return null;
	}
	
	public String getTypeDescription(boolean html) {
		if (html)
			return "<html>" +
								"The type attribute specifies one of three types of relations, so-called the generalized<br>" +
								"protein interactions in KEGG, and additional PCrel for interaction between a protein and<br>" +
								"a chemical compound, and maplink for linkage between a protein and a map. The maplink<br>" +
								"relation is provided for interaction between a protein and another in the specified map.";
		else
			return "The type attribute specifies one of three types of relations, so-called the generalized " +
								"protein interactions in KEGG, and additional PCrel for interaction between a protein and " +
								"a chemical compound, and maplink for linkage between a protein and a map. The maplink " +
								"relation is provided for interaction between a protein and another in the specified map.";
	}
	
	public static RelationType getRelationType(String typeValue) {
		if (typeValue.equals("ECrel"))
			return ECrel;
		if (typeValue.equals("PPrel"))
			return PPrel;
		if (typeValue.equals("PPRel"))
			return PPrel;
		if (typeValue.equals("GErel"))
			return GErel;
		if (typeValue.equals("PCrel"))
			return PCrel;
		if (typeValue.equals("maplink"))
			return maplink;
		return null;
	}
	
	@Override
	public String toString() {
		switch (this) {
			case ECrel:
				return "ECrel";
			case PPrel:
				return "PPrel";
			case GErel:
				return "GErel";
			case PCrel:
				return "PCrel";
			case maplink:
				return "maplink";
		}
		return null;
	}
}
