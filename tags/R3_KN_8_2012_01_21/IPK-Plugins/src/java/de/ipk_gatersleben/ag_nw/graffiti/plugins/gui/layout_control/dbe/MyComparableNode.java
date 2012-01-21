/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 12.08.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import org.w3c.dom.Node;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class MyComparableNode implements Comparable<Object> {
	
	public Node node;
	public int timeValue;
	
	/**
	 * @param i
	 * @param n
	 */
	public MyComparableNode(int timeValue, Node node) {
		this.node = node;
		this.timeValue = timeValue;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(Object n) {
		MyComparableNode cn = (MyComparableNode) n;
		if (cn.timeValue < timeValue)
			return 1;
		if (cn.timeValue > timeValue)
			return -1;
		return 0;
	}
	
}
