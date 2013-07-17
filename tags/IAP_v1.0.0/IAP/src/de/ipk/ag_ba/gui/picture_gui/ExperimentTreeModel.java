/*
 * Created on 20.04.2004
 */
package de.ipk.ag_ba.gui.picture_gui;

import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import de.ipk.ag_ba.gui.util.ExperimentReference;

/**
 * @author klukas
 */
public class ExperimentTreeModel implements TreeModel {
	
	private final ExperimentReference experiment;
	private final ActionListener dataChangedListener;
	private final boolean isReadOnly;
	
	public ExperimentTreeModel(
			ActionListener dataChangedListener,
			ExperimentReference exp,
			boolean readOnly) {
		this.experiment = exp;
		this.dataChangedListener = dataChangedListener;
		this.isReadOnly = readOnly;
	}
	
	@Override
	public Object getRoot() {
		final MongoTreeNode expNode = new MongoTreeNode(
				null, dataChangedListener,
				experiment,
				experiment.getExperiment(),
				experiment == null ? "NULL" : experiment.getExperimentName(), isReadOnly);
		expNode.setSizeDirty(true);
		try {
			expNode.updateSizeInfo(dataChangedListener);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		expNode.setIndex(0);
		expNode.setIsLeaf(false);
		
		Map<String, Object> attributes = new HashMap<String, Object>();
		if (experiment != null)
			experiment.getExperiment().fillAttributeMap(attributes);
		StringBuilder s = new StringBuilder();
		s.append("<html><table border='1'><th>Property</th><th>Value</th></tr>");
		for (String id : attributes.keySet()) {
			String idC = id;
			String v = "" + attributes.get(id);
			if (id.equals("settings"))
				s.append("<tr><td>" + idC + "</td><td>"
						+ (v != null && !v.equals("null") && !v.isEmpty() ? "(defined)" : "(not defined)")
						+ "</td></tr>");
			else
				s.append("<tr><td>" + idC + "</td><td>" + v + "</td></tr>");
		}
		s.append("<tr><td>Substance-Count</td><td>" + experiment.getExperiment().size() + "</td></tr>");
		s.append("</table></html>");
		expNode.setTooltipInfo(s.toString()); // "<html>" + experiment.toHTMLstring());//
		
		expNode.setGetChildrenMethod(
				new GetSubstances(null,
						expNode, experiment, isReadOnly, dataChangedListener, this));
		return expNode;
	}
	
	@Override
	public int getChildCount(Object parent) {
		return ((DBEtreeNodeModelHelper) parent).getChildCount();
	}
	
	@Override
	public boolean isLeaf(Object node) {
		return ((DBEtreeNodeModelHelper) node).isLeaf();
	}
	
	@Override
	public void addTreeModelListener(TreeModelListener l) {
		// empty
	}
	
	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		// empty
	}
	
	@Override
	public Object getChild(Object parent, int index) {
		return ((DBEtreeNodeModelHelper) parent).getChild(index);
	}
	
	@Override
	public int getIndexOfChild(Object parent, Object child) {
		return ((DBEtreeNodeModelHelper) child).getIndex();
	}
	
	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
		// empty
	}
}
