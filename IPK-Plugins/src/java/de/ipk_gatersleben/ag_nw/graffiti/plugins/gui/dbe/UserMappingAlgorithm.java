/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 17.01.2005 by Christian Klukas
 * (c) 2005 IPK Gatersleben, Group Network Analysis
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.ErrorMsg;
import org.Release;
import org.ReleaseInfo;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.algorithm.AbstractAlgorithm;

import com.wcohen.secondstring.Levenstein;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.CompoundEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.kegg.CompoundService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeEntry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.databases.sib_enzymes.EnzymeService;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.IdAndGraphNode;

/**
 * @author Christian Klukas
 *         (c) 2005 IPK Gatersleben, Group Network Analysis
 */
public class UserMappingAlgorithm extends AbstractAlgorithm {
	
	private static Levenstein jw = new Levenstein(); // new JaroWinklerTFIDF();
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#getName()
	 */
	public String getName() {
		if (ReleaseInfo.getRunningReleaseStatus() == Release.DEBUG)
			return "Levenstein";
		else
			return null;
	}
	
	@Override
	public String getCategory() {
		return "Analysis";
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.algorithm.Algorithm#execute()
	 */
	public void execute() {
	}
	
	// private static JFrame mappingDialog = new JFrame();
	
	/**
	 * @return False, if user mapping should be stopped ("abort all" was selected)
	 *         True, if all is allright
	 */
	public static boolean getUserSelectionFrom(
						final String substanceName,
						final List<IdAndGraphNode> possibleIdAndGraphNode,
						final ArrayList<IdAndGraphNode> result) {
		final SetAbleBoolean returnResult = new SetAbleBoolean(true);
		if (SwingUtilities.isEventDispatchThread()) {
			ArrayList<DoubleSortString> nodeNames = new ArrayList<DoubleSortString>();
			new HashMap<String, DoubleSortString>();
			for (IdAndGraphNode ign : possibleIdAndGraphNode) {
				String nodeName = ign.id;
				String nameWithHighestScore = ign.id;
				String desc = "";
				EnzymeEntry eze = EnzymeService.getEnzymeInformation(nodeName, false);
				double highestScore = jw.score(substanceName.toUpperCase(), ign.id.toUpperCase());
				
				if (eze != null) {
					desc = eze.getDE();
					double score = jw.score(substanceName.toUpperCase(), eze.getDE());
					if (score > highestScore) {
						highestScore = score;
						nameWithHighestScore = eze.getDE();
						desc = eze.getID();
					}
					
					for (String anno : eze.getAN()) {
						score = jw.score(substanceName.toUpperCase(), anno);
						if (score > highestScore) {
							highestScore = score;
							nameWithHighestScore = anno;
							desc = eze.getID();
						}
					}
				}
				CompoundEntry ce = CompoundService.getInformation(nodeName);
				if (ce != null) {
					desc = ce.getNames().get(0);
					double score = jw.score(substanceName.toUpperCase(), ce.getID());
					if (score > highestScore) {
						highestScore = score;
						nameWithHighestScore = ce.getID();
						desc = ce.getNames().get(0);
					}
					for (String name : ce.getNames()) {
						score = jw.score(substanceName.toUpperCase(), name);
						if (score > highestScore) {
							highestScore = score;
							nameWithHighestScore = name;
							desc = ce.getID();
						}
					}
				}
				nodeNames.add(new DoubleSortString(highestScore, nameWithHighestScore, ign, desc));
			}
			Collections.sort(nodeNames);
			Object res = JOptionPane.showInputDialog(
								MainFrame.getInstance(),
								"Please choose the mapping of substance " + substanceName + ":",
								"Data Mapping",
								JOptionPane.QUESTION_MESSAGE,
								null,
								nodeNames.toArray(),
								nodeNames.get(0));
			if (res != null)
				result.add(((DoubleSortString) res).ign);
			else {
				int res2 = JOptionPane.showConfirmDialog(MainFrame.getInstance(),
									"Select NO, to stop the user-given mapping procedure.",
									"Continue user-given mapping?",
									JOptionPane.YES_NO_OPTION);
				if (res2 == JOptionPane.NO_OPTION)
					returnResult.setValue(false);
			}
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						returnResult.setValue(getUserSelectionFrom(substanceName, possibleIdAndGraphNode, result));
					}
				});
			} catch (InterruptedException e) {
				ErrorMsg.addErrorMessage(e);
			} catch (InvocationTargetException e) {
				ErrorMsg.addErrorMessage(e);
			}
		}
		return returnResult.getValue();
	}
}

class DoubleSortString implements Comparable<Object> {
	private Double val;
	private String s;
	private String desc;
	IdAndGraphNode ign;
	
	public DoubleSortString(double sortVal, String s, IdAndGraphNode ign, String desc) {
		this.val = sortVal;
		this.s = s;
		this.ign = ign;
		this.desc = desc;
	}
	
	public Double getVal() {
		return val;
	}
	
	public IdAndGraphNode getIdAndGraphNode() {
		return ign;
	}
	
	public int compareTo(Object o) {
		if (val > ((DoubleSortString) o).getVal())
			return -1;
		else
			return 1;
	}
	
	@Override
	public String toString() {
		if (desc != null && desc.length() > 0)
			return s + " (" + desc + ")";
		else
			return s;
	}
}