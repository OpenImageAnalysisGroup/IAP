/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 01.03.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.database_processing.go;

import java.util.Collection;

public class GOinformation {
	
	private String name;
	private String namespace;
	private String defStr;
	private Collection<String> parents;
	private Collection<String> part_of;
	private boolean isObsolete;
	
	public GOinformation(String name, String namespace, String defStr,
						Collection<String> parents_is_a, Collection<String> parents_part_of,
						boolean isObsolete) {
		this.name = name;
		this.namespace = namespace;
		this.defStr = defStr;
		this.parents = parents_is_a;
		this.part_of = parents_part_of;
		this.isObsolete = isObsolete;
	}
	
	public String getName() {
		return name;
	}
	
	public String getNamespace() {
		return namespace;
	}
	
	public String getDefStr() {
		return defStr;
	}
	
	public Collection<String> getDirectParents() {
		return parents;
	}
	
	public Collection<String> getPartOf() {
		return part_of;
	}
	
	public boolean isObsolete() {
		return isObsolete;
	}
}
