/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.plugin_settings;

import java.util.Enumeration;

import javax.swing.tree.DefaultMutableTreeNode;

public class SortJTree {
	public static void sortJTree(DefaultMutableTreeNode rootNode, boolean ascending) {
		for (Enumeration<?> e = rootNode.depthFirstEnumeration(); e.hasMoreElements();) {
			DefaultMutableTreeNode n = (DefaultMutableTreeNode) e.nextElement();
			sortChildren(n, ascending);
		}
	}
	
	// Sort code from:
	// http://groups.google.com/groups?hl=de&lr=&ie=UTF-8&oe=utf-8&selm=3569178B.6611%40Aston.no
	// (public domain)
	private static void sortChildren(DefaultMutableTreeNode node, boolean ascending) {
		int NmbChildren = node.getChildCount();
		int startAt = 0; // Could be modified for storting on not-first-char
		DefaultMutableTreeNode[] arrayOfNodes = new DefaultMutableTreeNode[NmbChildren];
		// add all of top's children to an array of nodes
		for (int i = 0; i < NmbChildren; i++) {
			arrayOfNodes[i] = (DefaultMutableTreeNode) node.getChildAt(i);
		}
		
		QuickSort(arrayOfNodes, 0, arrayOfNodes.length - 1, ascending,
							startAt);
		// Remove all children from top
		node.removeAllChildren();
		// Add the new array of nodes to top
		for (int i = 0; i < arrayOfNodes.length; i++) {
			node.add(arrayOfNodes[i]);
		}
	}
	
	private static void QuickSort(DefaultMutableTreeNode[] a, int lo0, int hi0, boolean
						ascending, int posInText) {
		int lo = lo0;
		int hi = hi0;
		DefaultMutableTreeNode mid;
		DefaultMutableTreeNode T;
		
		if (hi0 > lo0) {
			mid = a[(lo0 + hi0) / 2];
			while (lo <= hi) {
				if (ascending) {
					while ((lo < hi0) && (
										a[lo].toString().substring(posInText).compareTo(mid.toString().substring(posInText))
										< 0))
						++lo;
					while ((hi > lo0) && (
										a[hi].toString().substring(posInText).compareTo(mid.toString().substring(posInText))
										> 0))
						--hi;
				} else {
					while ((lo < hi0) && (
										a[lo].toString().substring(posInText).compareTo(mid.toString().substring(posInText))
										> 0))
						++lo;
					while ((hi > lo0) && (
										a[hi].toString().substring(posInText).compareTo(mid.toString().substring(posInText))
										< 0))
						--hi;
				}
				if (lo <= hi) {
					T = a[lo];
					a[lo] = a[hi];
					a[hi] = T;
					++lo;
					--hi;
				}
			}
			if (lo0 < hi)
				QuickSort(a, lo0, hi, ascending, posInText);
			if (lo < hi0)
				QuickSort(a, lo, hi0, ascending, posInText);
		}
	}
	
}