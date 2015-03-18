package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.sbgn;

public enum SBGNitem {
	UnspecifiedEntityNode, SimpleChemical, MacroMolecule, GeneticEntity,
	MultimerMacrolecule, MultimerSimpleChemical, MultimerGeneticEntity,
	SourceSink,
	TagRight, TagLeft, TagUp, TagDown,
	Observable, Pertubation,
	Transition, Omitted, Uncertain, Associaction, Dissociation,
	AND, OR, NOT,
	Complex, Compartment, Submap;
	
	@Override
	public String toString() {
		switch (this) {
			case UnspecifiedEntityNode:
				return "unspecified entity";
			case SimpleChemical:
				return "simple chemical";
			case MacroMolecule:
				return "macromolecule";
			case GeneticEntity:
				return "genetic entity";
			case MultimerMacrolecule:
				return "multimer macromolecule";
			case MultimerSimpleChemical:
				return "multimer simple chemical";
			case MultimerGeneticEntity:
				return "multimer genetic entity";
			case SourceSink:
				return "source / sink";
			case TagRight:
				return "right";
			case TagLeft:
				return "left";
			case TagUp:
				return "up";
			case TagDown:
				return "down";
			case Observable:
				return "observable";
			case Pertubation:
				return "pertubation";
			case Transition:
				return "transition";
			case Omitted:
				return "omitted";
			case Uncertain:
				return "uncertain";
			case Associaction:
				return "association";
			case Dissociation:
				return "dissociation";
			case AND:
				return "and";
			case OR:
				return "or";
			case NOT:
				return "not";
			case Complex:
				return "complex";
			case Compartment:
				return "compartment";
			case Submap:
				return "submap";
		}
		return "Unknown!";
	}
	
	public String getShapeClassName() {
		switch (this) {
			case UnspecifiedEntityNode:
				return "rectangle";
			case SimpleChemical:
				return "circle";
			case MacroMolecule:
				return "oval";
			case GeneticEntity:
				return "nucleic";
			case MultimerMacrolecule:
				return "multirectangle";
			case MultimerSimpleChemical:
				return "multioval";
			case MultimerGeneticEntity:
				return "multinucleic";
			case SourceSink:
				return "sourcesink";
			case TagRight:
				return "tag";
			case TagLeft:
				return "tagl";
			case TagUp:
				return "tagu";
			case TagDown:
				return "tagd";
			case Observable:
				return "observable";
			case Pertubation:
				return "pertubation";
			case Transition:
				return "transition";
			case Omitted:
				return "rectangle";
			case Uncertain:
				return "rectangle";
			case Associaction:
				return "oval";
			case Dissociation:
				return "doubleoval";
			case AND:
				return "oval";
			case OR:
				return "oval";
			case NOT:
				return "oval";
			case Complex:
				return "rectangle";
			case Compartment:
				return "rectangle";
			case Submap:
				return "rectangle";
		}
		return null;
	}
}
