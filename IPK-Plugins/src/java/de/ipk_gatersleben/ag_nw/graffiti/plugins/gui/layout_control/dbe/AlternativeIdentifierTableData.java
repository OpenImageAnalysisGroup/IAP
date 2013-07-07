/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 03.05.2006 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

public class AlternativeIdentifierTableData extends TableData {
	
	public static int processAdditionaldentifiers(
						boolean processAllExistingIDs,
						TableData myData, ExperimentInterface md,
						BackgroundTaskStatusProviderSupportingExternalCall optStatus,
						double optStartProgress, double optEndProgress, StringBuilder statusMessage,
						boolean skipFirstRow) {
		return myData.processAdditionaldentifiers(
							processAllExistingIDs, false, md, optStatus, optStartProgress, optEndProgress, statusMessage, skipFirstRow, null);
	}
	
	public static int processAdditionaldentifiers(
						boolean processAllExistingIDs, boolean processAllNewIDs,
						TableData myData, ExperimentInterface md,
						BackgroundTaskStatusProviderSupportingExternalCall optStatus,
						double optStartProgress, double optEndProgress, StringBuilder statusMessage,
						boolean skipFirstRow,
						HashSet<Integer> ignoreColumns) {
		return myData.processAdditionaldentifiers(
							processAllExistingIDs, processAllNewIDs, md, optStatus, optStartProgress, optEndProgress, statusMessage, skipFirstRow, ignoreColumns);
	}
	
	public static TableData getAlternativeTableData(ArrayList<String[][]> alternativeIDs) {
		HashMap<String, TreeSet<String>> mainId2alternativeIds = new HashMap<String, TreeSet<String>>();
		for (String[][] altIds : alternativeIDs)
			processAlternativeIds(mainId2alternativeIds, altIds);
		TableData result = new TableData();
		result.addCellData(0, 0, "ID");
		int row = 1;
		for (String mainId : mainId2alternativeIds.keySet()) {
			TreeSet<String> alternativeIds = mainId2alternativeIds.get(mainId);
			if (alternativeIds.size() > 0) {
				result.addCellData(0, row, mainId);
				int col = 1;
				for (String alternativeId : alternativeIds) {
					result.addCellData(col++, row, alternativeId);
				}
				row++;
			}
		}
		// result.showDataDialog();
		return result;
	}
	
	private static void processAlternativeIds(
						HashMap<String, TreeSet<String>> mainId2alternativeIds,
						String[][] alternativeIDs) {
		if (alternativeIDs == null)
			return;
		for (int i = 0; i < alternativeIDs.length; i++) {
			String[] info = alternativeIDs[i];
			if (info == null || info.length <= 0)
				continue;
			String mainId = info[0];
			if (mainId == null || mainId.length() <= 0)
				continue;
			if (!mainId2alternativeIds.containsKey(mainId))
				mainId2alternativeIds.put(mainId, new TreeSet<String>());
			TreeSet<String> knownAlternatives = mainId2alternativeIds.get(mainId);
			for (int ai = 1; ai < info.length; ai++) {
				String alternativeId = info[ai];
				if (alternativeId == null || alternativeId.length() <= 0)
					continue;
				knownAlternatives.add(alternativeId);
			}
		}
	}
}
