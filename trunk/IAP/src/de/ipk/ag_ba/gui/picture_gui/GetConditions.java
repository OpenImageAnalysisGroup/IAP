package de.ipk.ag_ba.gui.picture_gui;

import java.awt.event.ActionListener;
import java.util.ArrayList;

import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

public class GetConditions implements Runnable {
	/**
	 * 
	 */
	private final ExperimentTreeModel getConditions;
	boolean readOnly;
	private final SubstanceInterface substance;
	private final MongoTreeNode substNode;
	private final ExperimentReferenceInterface experiment;
	private final ActionListener dataChangedListener;
	
	public GetConditions(ExperimentTreeModel experimentTreeModel, MongoTreeNode substNode,
			ExperimentReferenceInterface experiment,
			SubstanceInterface substance,
			boolean readOnly, ActionListener dataChangedListener) {
		getConditions = experimentTreeModel;
		this.readOnly = readOnly;
		this.substance = substance;
		this.substNode = substNode;
		this.experiment = experiment;
		this.dataChangedListener = dataChangedListener;
	}
	
	@Override
	public void run() {
		int p = 0;
		ArrayList<DBEtreeNodeModelHelper> children = new ArrayList<DBEtreeNodeModelHelper>();
		for (ConditionInterface condition : substance) {
			final MongoTreeNode condNode = new MongoTreeNode(
					substNode, dataChangedListener,
					experiment, condition,
					condition.getConditionName(), readOnly);
			
			condNode.setIsLeaf(false);
			condNode.setIndex(p++);
			condNode.setTooltipInfo(condition.getHTMLdescription());
			condNode.setGetChildrenMethod(
					new GetSamples(getConditions, condNode, experiment, condition, readOnly, dataChangedListener));
			children.add(condNode);
		}
		substNode.setChildren(children.toArray(new DBEtreeNodeModelHelper[0]));
	}
}