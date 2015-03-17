package de.ipk.ag_ba.gui.picture_gui;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeMap;

import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

public class GetSubstances implements Runnable {
	boolean readOnly;
	private final ExperimentReferenceInterface experiment;
	private final MongoTreeNode projectNode;
	private final ActionListener dataChangedListener;
	private final ExperimentTreeModel treeModel;
	private final String validPrefix;
	
	public GetSubstances(
			String validPrefix,
			MongoTreeNode projectNode, ExperimentReferenceInterface experiment,
			boolean readOnly,
			ActionListener dataChangedListener,
			ExperimentTreeModel treeModel) {
		this.validPrefix = validPrefix;
		this.readOnly = readOnly;
		this.experiment = experiment;
		this.projectNode = projectNode;
		this.dataChangedListener = dataChangedListener;
		this.treeModel = treeModel;
	}
	
	@Override
	public void run() {
		int p = 0;
		ArrayList<DBEtreeNodeModelHelper> children = new ArrayList<DBEtreeNodeModelHelper>();
		if (experiment != null) {
			TreeMap<String, HashSet<SubstanceInterface>> substancePrefixNames = new TreeMap<String, HashSet<SubstanceInterface>>();
			HashSet<SubstanceInterface> substancesPutIntoSubGroup = new HashSet<SubstanceInterface>();
			for (SubstanceInterface substance : experiment.getExperiment()) {
				String sn = substance.getName();
				if (validPrefix == null || (validPrefix != null && sn.startsWith(validPrefix))) {
					if (validPrefix != null && sn.startsWith(validPrefix) && sn.length() > validPrefix.length())
						sn = sn.substring(validPrefix.length() + ".".length());
					if (sn.contains(".")) {
						String pre = (validPrefix != null ? validPrefix + "." : "") + sn.split("\\.")[0];
						if (!substancePrefixNames.containsKey(pre))
							substancePrefixNames.put(pre, new HashSet<SubstanceInterface>());
						substancePrefixNames.get(pre).add(substance);
						substancesPutIntoSubGroup.add(substance);
					}
				}
			}
			ArrayList<String> prefixNamesWithOnlyOneSubstance = new ArrayList<String>();
			for (String s : substancePrefixNames.keySet()) {
				if (substancePrefixNames.get(s).size() < 2)
					prefixNamesWithOnlyOneSubstance.add(s);
			}
			for (String s : prefixNamesWithOnlyOneSubstance) {
				for (SubstanceInterface ss : substancePrefixNames.get(s))
					substancesPutIntoSubGroup.remove(ss);
				substancePrefixNames.remove(s);
			}
			for (String prefix : substancePrefixNames.keySet()) {
				MongoTreeNode substPrefixNode = new MongoTreeNode(null, null, experiment,
						null,
						prefix.contains(".") ? prefix.substring(prefix.lastIndexOf(".") + ".".length()) : prefix,
						true);
				substPrefixNode.setIsGroup(true);
				substPrefixNode.setIsLeaf(false);
				substPrefixNode.setIndex(p++);
				substPrefixNode.setTooltipInfo(prefix);
				substPrefixNode.setGetChildrenMethod(
						new GetSubstances(
								prefix,
								substPrefixNode, experiment, readOnly, dataChangedListener, treeModel));
				children.add(substPrefixNode);
			}
			for (SubstanceInterface substance : experiment.getExperiment()) {
				String substanceName = substance.getName();
				if (substancesPutIntoSubGroup.contains(substance))
					continue;
				if (validPrefix != null && !substanceName.startsWith(validPrefix + "."))
					continue;
				String title = substanceName;
				if (validPrefix != null && title.contains("."))
					title = title.substring(projectNode.getTooltipInfo().length() + ".".length());
				if (substance.getName() != null && title.contains("|"))
					title = "<html>" + title.substring(0, title.lastIndexOf("|")) + " <font color='gray'><small>["
							+ title.substring(title.lastIndexOf("|") + "|".length()) + "]";
				if (substance.getInfo() != null && !substance.getInfo().isEmpty())
					title = "<html>" + title + " <font color='gray'><small>(" + substance.getInfo() + ")";
				MongoTreeNode substNode = new MongoTreeNode(projectNode, dataChangedListener, experiment,
						substance, title, readOnly); //$NON-NLS-1$//$NON-NLS-2$
				
				substNode.setIsLeaf(false);
				substNode.setTooltipInfo(substance.getHTMLdescription());
				substNode.setIndex(p++);
				substNode.setGetChildrenMethod(
						new GetConditions(
								treeModel,
								substNode, experiment,
								substance, readOnly,
								dataChangedListener));
				children.add(substNode);
			}
		}
		projectNode.setChildren(children.toArray(new DBEtreeNodeModelHelper[0]));
	}
}