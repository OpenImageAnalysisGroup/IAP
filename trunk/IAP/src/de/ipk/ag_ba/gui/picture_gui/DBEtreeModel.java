/*
 * Created on 20.04.2004
 */
package de.ipk.ag_ba.gui.picture_gui;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Measurement;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author klukas
 */
public class DBEtreeModel implements TreeModel {
	
	private final class GetSubstances implements Runnable {
		boolean readOnly;
		private final ExperimentInterface experiment;
		private final MongoTreeNode projectNode;
		private final ActionListener dataChangedListener;
		
		private GetSubstances(MongoTreeNode projectNode, ExperimentInterface experiment, boolean readOnly,
							ActionListener dataChangedListener) {
			this.readOnly = readOnly;
			this.experiment = experiment;
			this.projectNode = projectNode;
			this.dataChangedListener = dataChangedListener;
		}
		
		public void run() {
			int p = 0;
			ArrayList<DBEtreeNodeModelHelper> children = new ArrayList<DBEtreeNodeModelHelper>();
			for (SubstanceInterface substance : experiment) {
				MongoTreeNode substNode = new MongoTreeNode(projectNode, dataChangedListener, experiment,
									substance, substance.getName(), readOnly); //$NON-NLS-1$//$NON-NLS-2$
				
				substNode.setIsLeaf(false);
				substNode.setIndex(p++);
				substNode.setGetChildrenMethod(new GetConditions(substNode, experiment, substance, readOnly,
									dataChangedListener));
				children.add(substNode);
			}
			projectNode.setChildren(children.toArray(new DBEtreeNodeModelHelper[0]));
		}
	}
	
	private final class GetConditions implements Runnable {
		boolean readOnly;
		private final SubstanceInterface substance;
		private final MongoTreeNode substNode;
		private final ExperimentInterface experiment;
		private final ActionListener dataChangedListener;
		
		private GetConditions(MongoTreeNode substNode, ExperimentInterface experiment, SubstanceInterface substance,
							boolean readOnly, ActionListener dataChangedListener) {
			this.readOnly = readOnly;
			this.substance = substance;
			this.substNode = substNode;
			this.experiment = experiment;
			this.dataChangedListener = dataChangedListener;
		}
		
		public void run() {
			int p = 0;
			ArrayList<DBEtreeNodeModelHelper> children = new ArrayList<DBEtreeNodeModelHelper>();
			for (ConditionInterface condition : substance) {
				final MongoTreeNode condNode = new MongoTreeNode(substNode, dataChangedListener, experiment, condition,
									condition.getConditionName(), readOnly);
				
				condNode.setIsLeaf(false);
				condNode.setIndex(p++);
				condNode
									.setGetChildrenMethod(new GetSamples(condNode, experiment, condition, readOnly, dataChangedListener));
				children.add(condNode);
			}
			substNode.setChildren(children.toArray(new DBEtreeNodeModelHelper[0]));
		}
	}
	
	private final class GetSamples implements Runnable {
		boolean readOnly;
		private final ConditionInterface condition;
		private final MongoTreeNode condNode;
		private final ExperimentInterface experiment;
		private final ActionListener dataChangedListener;
		
		private GetSamples(MongoTreeNode condNode, ExperimentInterface experiment, ConditionInterface condition,
							boolean readOnly, ActionListener dataChangedListener) {
			this.readOnly = readOnly;
			this.condition = condition;
			this.condNode = condNode;
			this.experiment = experiment;
			this.dataChangedListener = dataChangedListener;
		}
		
		public void run() {
			int p = 0;
			ArrayList<DBEtreeNodeModelHelper> children = new ArrayList<DBEtreeNodeModelHelper>();
			
			TreeMap<String, ArrayList<SampleInterface>> samples = new TreeMap<String, ArrayList<SampleInterface>>();
			
			for (SampleInterface sample : condition.getSortedSamples()) {
				MongoTreeNode sampNode = new MongoTreeNode(condNode, dataChangedListener,
						experiment, sample, sample.toString(), readOnly);
				
				sampNode.setIsLeaf(false);
				sampNode.setIndex(p++);
				String key = sample.getTime() + " / " + sample.getTimeUnit();
				if (!samples.containsKey(key)) {
					samples.put(key, new ArrayList<SampleInterface>());
					
					ArrayList<SampleInterface> sampleArray = samples.get(key);
					sampleArray.add(sample);
					
					sampNode.setGetChildrenMethod(new GetMeasurements(sampNode, experiment, sampleArray, readOnly,
							dataChangedListener));
					children.add(sampNode);
				} else {
					ArrayList<SampleInterface> sampleArray = samples.get(key);
					sampleArray.add(sample);
				}
			}
			
			condNode.setChildren(children.toArray(new DBEtreeNodeModelHelper[0]));
		}
	}
	
	private final class GetMeasurements implements Runnable {
		boolean readOnly;
		private final ArrayList<SampleInterface> samples;
		private final MongoTreeNode sampNode;
		private final ExperimentInterface experiment;
		private final ActionListener dataChangedListener;
		
		private GetMeasurements(MongoTreeNode sampNode, ExperimentInterface experiment, ArrayList<SampleInterface> samples,
							boolean readOnly, ActionListener dataChangedListener) {
			this.readOnly = readOnly;
			this.samples = samples;
			this.sampNode = sampNode;
			this.experiment = experiment;
			this.dataChangedListener = dataChangedListener;
		}
		
		public void run() {
			int p = 0;
			ArrayList<DBEtreeNodeModelHelper> children = new ArrayList<DBEtreeNodeModelHelper>();
			for (SampleInterface sample : samples) {
				for (Measurement meas : sample) {
					MongoTreeNode measNode = new MongoTreeNode(sampNode, dataChangedListener, experiment, meas,
								meas.toString(), readOnly);
					measNode.setIsLeaf(true);
					measNode.setIndex(p++);
					children.add(measNode);
				}
			}
			sampNode.setChildren(children.toArray(new DBEtreeNodeModelHelper[0]));
		}
	}
	
	MongoDB m;
	private final ExperimentInterface document;
	private final ActionListener dataChangedListener;
	private final boolean isReadOnly;
	
	public DBEtreeModel(ActionListener dataChangedListener, MongoDB m, ExperimentInterface doc,
						boolean readOnly) {
		this.m = m;
		this.document = doc;
		this.dataChangedListener = dataChangedListener;
		this.isReadOnly = readOnly;
	}
	
	public Object getRoot() {
		final MongoTreeNode expNode = new MongoTreeNode(null, dataChangedListener, document, document,
							document.getName(), isReadOnly);
		expNode.setSizeDirty(true);
		expNode.updateSizeInfo(m, dataChangedListener);
		expNode.setIndex(0);
		expNode.setIsLeaf(false);
		
		expNode.setGetChildrenMethod(new GetSubstances(expNode, document, isReadOnly, dataChangedListener));
		return expNode;
	}
	
	public int getChildCount(Object parent) {
		return ((DBEtreeNodeModelHelper) parent).getChildCount();
	}
	
	public boolean isLeaf(Object node) {
		return ((DBEtreeNodeModelHelper) node).isLeaf();
	}
	
	public void addTreeModelListener(TreeModelListener l) {
		// empty
	}
	
	public void removeTreeModelListener(TreeModelListener l) {
		// empty
	}
	
	public Object getChild(Object parent, int index) {
		return ((DBEtreeNodeModelHelper) parent).getChild(index);
	}
	
	public int getIndexOfChild(Object parent, Object child) {
		return ((DBEtreeNodeModelHelper) child).getIndex();
	}
	
	public void valueForPathChanged(TreePath path, Object newValue) {
		// empty
	}
}
