package de.ipk.ag_ba.gui.picture_gui;

import java.awt.event.ActionListener;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.MappingDataEntity;

/**
 * @author Christian Klukas
 */
public class MongoTreeNode extends MongoTreeNodeBasis {
	private final MappingDataEntity tableName;
	private final String title;
	private final MongoTreeNode projectNode;
	private boolean sizeDirty = true;

	private String size = "????????? kb";
	private final ExperimentInterface experiment;
	private final ActionListener sizeChangedListener;

	public MongoTreeNode(MongoTreeNode projectNode, ActionListener sizeChangedListener, ExperimentInterface doc,
						MappingDataEntity tableName, String title, boolean readOnly) {
		super(readOnly);
		this.experiment = doc;
		this.projectNode = projectNode;
		this.tableName = tableName;
		this.title = title;
		this.sizeChangedListener = sizeChangedListener;
	}

	public MongoTreeNode getProjectNode() {
		if (projectNode == null)
			return this;
		return projectNode;
	}

	public MappingDataEntity getTargetEntity() {
		return tableName;
	}

	public String getExperimentName() {
		return experiment.getHeader().getExperimentname();
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public String toString() {
		if (projectNode == null && !(getExperimentName() == null)) {
			if (readOnly)
				return title + " [" + size + "]";
			else
				return title + " [" + size + "]";
		} else {
			if (readOnly)
				return title;
			else
				return title;
		}
	}

	public void updateSizeInfo(final String user, final String pass, final ActionListener dataChangedListener) {
		if (getExperimentName() == null)
			return;
		if (!sizeDirty)
			return;
		sizeDirty = false;

		if (projectNode != null) {
			projectNode.updateSizeInfo(user, pass, dataChangedListener);
		} else {
			MyThread infoThread = new MyThread(new Runnable() {
				public void run() {
					getCurrentProjectSize(user, pass);
					if (dataChangedListener != null)
						dataChangedListener.actionPerformed(null);
				}
			}, "determine project node size");
			infoThread.setPriority(Thread.MIN_PRIORITY);
			BackgroundThreadDispatcher.addTask(infoThread, 1);
		}
	}

	void getCurrentProjectSize(String user, String pass) {
		try {
			int sz = DataExchangeHelperForExperiments.getSizeOfExperiment(user, pass, experiment);
			if (sz != -1) {
				if (sz < 1024)
					size = sz + " KB";
				else
					size = Math.round(10 * (double) sz / 1024.0) / 10.0 + " MB";
			} else
				size = " -      ";
		} catch (Exception e) {
			size = "ERR     ";
		}

	}

	/**
	 * Set the "need size info update flag" of the project node to the value
	 * specified.
	 */
	public void setSizeDirty(boolean dirty) {
		if (projectNode == null)
			this.sizeDirty = dirty;
		else
			projectNode.setSizeDirty(true);
	}

	@Override
	public int getChildCount() {
		if (getExperimentName() == null)
			return 0;
		return super.getChildCount();
	}

	@Override
	public boolean isLeaf() {
		if (getExperimentName() == null)
			return true;
		return super.isLeaf();
	}

	public ExperimentInterface getExperiment() {
		return experiment;
	}

	public ActionListener getSizeChangedListener() {
		return sizeChangedListener;
	}

	public boolean mayContainData() {
		return true;
	}
}
