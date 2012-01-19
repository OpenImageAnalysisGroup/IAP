/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.transpath;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import org.ErrorMsg;

public abstract class TranspathEntity implements TranspathEntityType {
	
	private static TreeSet<String> todo = new TreeSet<String>();
	int maxExamples = 4;
	private static HashMap<String, HashSet<String>> todo2examples = new HashMap<String, HashSet<String>>();
	
	public void processXMLentityValue(String environment, String value) {
		// System.out.println("TODO : "+environment+" : "+value);
		todo.add(environment);
		if (!todo2examples.containsKey(environment))
			todo2examples.put(environment, new HashSet<String>());
		if (todo2examples.get(environment).size() < maxExamples)
			todo2examples.get(environment).add(value);
	}
	
	public void printTodo() {
		System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		System.out.println("CLASS: " + this.getClass().getSimpleName());
		for (String s : todo) {
			System.out.print(s + "  [");
			for (String ss : todo2examples.get(s)) {
				System.out.print(ss + " / ");
			}
			System.out.println(".?.]");
		}
	}
	
	protected String checkAndSet(String priorValue, String value) {
		if (priorValue != null)
			ErrorMsg.addErrorMessage("Internal error: variable content has been set more than once!");
		return value;
	}
}
