package de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.add_attributes;

public enum ModeOfAttributeOperation {
	createAttribute, replaceLabel, appendLabel;
	
	@Override
	public String toString() {
		switch (this) {
			case createAttribute:
				return "Create Attribute";
			case replaceLabel:
				return "Set Label";
			case appendLabel:
				return "Append Label";
			default:
				return "undefined (internal error)";
		}
	}
}
