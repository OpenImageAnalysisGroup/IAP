/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.04.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml;

import java.util.Collection;

import org.jdom.Element;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.EntryType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.Id;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.IdRef;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.KeggId;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.SubtypeName;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.SubtypeValue;

public class Subtype {
	
	private SubtypeName name;
	private IdRef value;
	
	public Subtype(SubtypeName name, IdRef value) {
		assert name != null;
		// assert value!=null;
		if (name == null)
			return;
		this.name = name;
		this.value = value;
	}
	
	public SubtypeName getName() {
		return name;
	}
	
	public IdRef getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		if (value != null)
			return value.getRef().getName().getId(); // name.toString()+"--"+
		else
			return name.toString();
	}
	
	public static Subtype getSubtypeFromKgmlSubtypeElement(Element subtypeElement,
						Collection<Entry> entries,
						String clusterIdForHiddenCompounds) {
		String nameValue = KGMLhelper.getAttributeValue(subtypeElement, "name", null);
		if (nameValue == null)
			return null;
		if (nameValue != null && (nameValue.equals("compound") || nameValue.equals("hidden compound"))) {
			String valueIdRef = KGMLhelper.getAttributeValue(subtypeElement, "value", null);
			Entry refEntry = null;
			for (Entry e : entries) {
				if (e.getId().matches(valueIdRef)) {
					refEntry = e;
					break;
				}
			}
			if (refEntry == null) {
				Entry newEntry = new Entry(
									new Id(valueIdRef),
									new KeggId(valueIdRef),
									EntryType.hiddenCompound,
									null,
									null,
									null,
									null,
									null);
				newEntry.setSourcePathwayKeggId(clusterIdForHiddenCompounds);
				entries.add(newEntry);
				refEntry = newEntry;
			}
			IdRef id = new IdRef(refEntry, valueIdRef);
			SubtypeName name = SubtypeName.getSubtypeName(nameValue);
			return new Subtype(name, id);
		}
		SubtypeName name = SubtypeName.getSubtypeName(nameValue);
		return new Subtype(name, null);
	}
	
	public Element getKgmlSubtypeElement() {
		Element subtypeElement = new Element("subtype");
		KGMLhelper.addNewAttribute(subtypeElement, "name", name.toString());
		if (value == null)
			KGMLhelper.addNewAttribute(subtypeElement, "value", new SubtypeValue(name).toString());
		else {
			if (name == SubtypeName.hiddenCompound)
				KGMLhelper.addNewAttribute(subtypeElement, "value", value.getRef().getName().getId());
			else
				KGMLhelper.addNewAttribute(subtypeElement, "value", value.toString());
		}
		return subtypeElement;
	}
	
	public String getVisibleName() {
		if (value != null)
			return value.getRef().getVisibleName();
		else
			return name.toString();
	}
}
