/*
 * ===========================================================
 * JFreeChart : a free chart library for the Java(tm) platform
 * ===========================================================
 * (C) Copyright 2000-2004, by Object Refinery Limited and Contributors.
 * Project Info: http://www.jfree.org/jfreechart/index.html
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation;
 * either version 2.1 of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * library; if not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc.
 * in the United States and other countries.]
 * ------------------
 * KeyToGroupMap.java
 * ------------------
 * (C) Copyright 2004, by Object Refinery Limited and Contributors.
 * Original Author: David Gilbert (for Object Refinery Limited);
 * Contributor(s): -;
 * $Id: KeyToGroupMap.java,v 1.1 2011-01-31 09:02:13 klukas Exp $
 * Changes
 * -------
 * 29-Apr-2004 : Version 1 (DG);
 */

package org.jfree.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jfree.util.ObjectUtils;
import org.jfree.util.PublicCloneable;

/**
 * A class that maps keys (instances of <code>Comparable</code> to groups.
 */
public class KeyToGroupMap implements Cloneable, PublicCloneable, Serializable {

	/** The default group. */
	private Comparable defaultGroup;

	/** A mapping between keys and groups. */
	private Map keyToGroupMap;

	/**
	 * Creates a new map with a default group named 'Default Group'.
	 */
	public KeyToGroupMap() {
		this("Default Group");
	}

	/**
	 * Creates a new map with the specified default group.
	 * 
	 * @param defaultGroup
	 *           the default group (<code>null</code> not permitted).
	 */
	public KeyToGroupMap(Comparable defaultGroup) {
		if (defaultGroup == null) {
			throw new IllegalArgumentException("Null 'defaultGroup' argument.");
		}
		this.defaultGroup = defaultGroup;
		this.keyToGroupMap = new HashMap();
	}

	/**
	 * Returns the groups (always including the default group) in the map.
	 * 
	 * @return The groups.
	 */
	public List getGroups() {
		Collection values = this.keyToGroupMap.values();
		List result = new ArrayList();
		result.add(this.defaultGroup);
		Iterator iterator = values.iterator();
		while (iterator.hasNext()) {
			Comparable group = (Comparable) iterator.next();
			if (!result.contains(group)) {
				result.add(group);
			}
		}
		return result;
	}

	/**
	 * Returns the number of groups in the map.
	 * 
	 * @return The number of groups in the map.
	 */
	public int getGroupCount() {
		return getGroups().size();
	}

	/**
	 * Returns the index for the group.
	 * 
	 * @param group
	 *           the group.
	 * @return The group index.
	 */
	public int getGroupIndex(Comparable group) {
		return getGroups().indexOf(group);
	}

	/**
	 * Returns the group that a key is mapped to.
	 * 
	 * @param key
	 *           the key.
	 * @return The group (never <code>null</code>, returns the default group if there
	 *         is no mapping for the specified key).
	 */
	public Comparable getGroup(Comparable key) {
		Comparable result = this.defaultGroup;
		Comparable group = (Comparable) this.keyToGroupMap.get(key);
		if (group != null) {
			result = group;
		}
		return result;
	}

	/**
	 * Maps a key to a group.
	 * 
	 * @param key
	 *           the key (<code>null</code> not permitted).
	 * @param group
	 *           the group (<code>null</code> permitted, replaced by default group).
	 */
	public void mapKeyToGroup(Comparable key, Comparable group) {
		if (key == null) {
			throw new IllegalArgumentException("Null 'key' argument.");
		}
		if (group == null) {
			group = this.defaultGroup;
		}
		this.keyToGroupMap.put(key, group);
	}

	/**
	 * Tests the map for equality against an arbitrary object.
	 * 
	 * @param obj
	 *           the object to test against (<code>null</code> permitted).
	 * @return A boolean.
	 */
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (obj instanceof KeyToGroupMap) {
			KeyToGroupMap m = (KeyToGroupMap) obj;
			boolean b0 = ObjectUtils.equal(this.defaultGroup, m.defaultGroup);
			boolean b1 = this.keyToGroupMap.equals(m.keyToGroupMap);
			return b0 && b1;
		}
		return false;
	}

	/**
	 * Returns a clone of the map.
	 * 
	 * @return A clone.
	 * @throws CloneNotSupportedException
	 *            if there is a problem cloning the map.
	 */
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
