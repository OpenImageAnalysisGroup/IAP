/*******************************************************************************
 * Copyright (c) 2003-2009 Plant Bioinformatics Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on Apr 21, 2010 by Christian Klukas
 */

package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map.Entry;

import javax.swing.JButton;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author klukas
 */
public class BridgeDBhelper {
	
	/**
	 * @param p
	 * @param md
	 * @param addIdentifiers
	 */
	public static void processData(AnnotationProvider p, ExperimentInterface substanceNodes, JButton addIdentifiers,
						BackgroundTaskStatusProviderSupportingExternalCall optStatus) {
		TableData annotation = new TableData();
		
		ArrayList<String> workload = new ArrayList<String>();
		for (SubstanceInterface xmlSubstanceNode : substanceNodes) {
			if (optStatus != null) {
				if (optStatus.wantsToStop())
					break;
			}
			String name = xmlSubstanceNode.getName();
			workload.add(name);
		}
		if (optStatus != null) {
			optStatus.setCurrentStatusValue(-1);
			optStatus.setCurrentStatusText1("Request annotation...");
			optStatus.setCurrentStatusText2("Please wait a few moments");
		}
		LinkedHashMap<String, LinkedHashSet<String>> result = p.processWorkload(workload);
		if (optStatus != null) {
			optStatus.setCurrentStatusValue(0);
			optStatus.setCurrentStatusText1("SOAP Call ended");
			optStatus.setCurrentStatusText2("Process results....");
		}
		
		int row = 0;
		for (Entry<String, LinkedHashSet<String>> e : result.entrySet()) {
			int col = 0;
			annotation.addCellData(col, row, e.getKey());
			for (String anno : e.getValue()) {
				col++;
				annotation.addCellData(col, row, anno);
			}
			row++;
		}
		
		HashSet<Integer> ignoreColumns = new HashSet<Integer>();
		boolean skipFirstRow = false;
		boolean processAllIDs = false;
		boolean processAllNewIDs = false;
		int optStartProgress = 0;
		int optEndProgress = 100;
		StringBuilder statusMessage = new StringBuilder();
		annotation.processAdditionaldentifiers(processAllIDs, processAllNewIDs, substanceNodes, optStatus,
							optStartProgress, optEndProgress, statusMessage, skipFirstRow, ignoreColumns);
		
	}
	
}
