package de.ipk.ag_ba.gui.picture_gui;

import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.TreeMap;

import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SampleInterface;

final class GetSamples implements Runnable {
	/**
	 * 
	 */
	private final ExperimentTreeModel expTreeModel;
	boolean readOnly;
	private final ConditionInterface condition;
	private final MongoTreeNode condNode;
	private final ExperimentReference experiment;
	private final ActionListener dataChangedListener;
	
	GetSamples(ExperimentTreeModel experimentTreeModel, MongoTreeNode condNode, ExperimentReference experiment, ConditionInterface condition,
			boolean readOnly, ActionListener dataChangedListener) {
		this.expTreeModel = experimentTreeModel;
		this.readOnly = readOnly;
		this.condition = condition;
		this.condNode = condNode;
		this.experiment = experiment;
		this.dataChangedListener = dataChangedListener;
	}
	
	@Override
	public void run() {
		int p = 0;
		ArrayList<DBEtreeNodeModelHelper> children = new ArrayList<DBEtreeNodeModelHelper>();
		
		TreeMap<String, ArrayList<SampleInterface>> samples = new TreeMap<String, ArrayList<SampleInterface>>();
		HashMap<String, MongoTreeNode> key2sampleNode = new HashMap<String, MongoTreeNode>();
		SimpleDateFormat sdf = new SimpleDateFormat();
		for (SampleInterface sample : condition.getSortedSamples()) {
			MongoTreeNode sampNode = new MongoTreeNode(condNode, dataChangedListener,
					experiment, sample, sample.toString(), readOnly);
			
			long firstTime = 0;
			long lastTime = 0;
			sampNode.setIsSample(true);
			sampNode.setIsLeaf(false);
			sampNode.setIndex(p++);
			String key = sample.getTime() + " / " + sample.getTimeUnit();
			if (!samples.containsKey(key)) {
				samples.put(key, new ArrayList<SampleInterface>());
				
				ArrayList<SampleInterface> sampleArray = samples.get(key);
				sampleArray.add(sample);
				
				sampNode.setGetChildrenMethod(new GetMeasurements(sampNode,
						experiment, sampleArray, readOnly,
						dataChangedListener, sdf));
				children.add(sampNode);
				key2sampleNode.put(key, sampNode);
			} else {
				ArrayList<SampleInterface> sampleArray = samples.get(key);
				sampleArray.add(sample);
			}
			for (SampleInterface si : samples.get(key)) {
				if (si.getSampleFineTimeOrRowId() == null)
					continue;
				if (si.getSampleFineTimeOrRowId() < firstTime || firstTime == 0)
					firstTime = si.getSampleFineTimeOrRowId();
				if (si.getSampleFineTimeOrRowId() > lastTime || lastTime == 0)
					lastTime = si.getSampleFineTimeOrRowId();
			}
			key2sampleNode.get(key).setTooltipInfo(sdf.format(new Date(firstTime)) + " to " + sdf.format(new Date(lastTime)));
			
		}
		
		condNode.setChildren(children.toArray(new DBEtreeNodeModelHelper[0]));
	}
}