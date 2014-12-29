package de.ipk.ag_ba.gui.picture_gui;

import java.awt.event.ActionListener;

import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.MappingDataEntity;

/**
 * @author Christian Klukas
 */
public class MongoTreeNode extends MongoTreeNodeBasis {
	private final MappingDataEntity tableName;
	private final String title;
	private final MongoTreeNode projectNode;
	private boolean sizeDirty = true;
	
	private String size = "";
	private final ExperimentReferenceInterface experiment;
	private final ActionListener sizeChangedListener;
	private String tooltip;
	private boolean isGroupNode = false;
	private boolean isSampleNode;
	
	public MongoTreeNode(MongoTreeNode projectNode, ActionListener sizeChangedListener,
			ExperimentReferenceInterface expRef,
			MappingDataEntity tableName, String title, boolean readOnly) {
		super(readOnly);
		this.experiment = expRef;
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
		return experiment != null ? experiment.getHeader().getExperimentName() : "NULL";
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}
	
	@Override
	public String toString() {
		// if (projectNode == null && !(getExperimentName() == null)) {
		// if (readOnly)
		// return title + (size.isEmpty() ? "" : " [" + size + "]");
		// else
		// return title + (size.isEmpty() ? "" : " [" + size + "]");
		// } else {
		// if (readOnly)
		// return title;
		// else
		return title;
		// }
	}
	
	public void updateSizeInfo(final ActionListener dataChangedListener) throws InterruptedException {
		if (getExperimentName() == null)
			return;
		if (!sizeDirty)
			return;
		sizeDirty = false;
		
		if (projectNode != null) {
			projectNode.updateSizeInfo(dataChangedListener);
		} else {
			LocalComputeJob infoThread = new LocalComputeJob(new Runnable() {
				@Override
				public void run() {
					getCurrentProjectSize();
					if (dataChangedListener != null)
						dataChangedListener.actionPerformed(null);
				}
			}, "determine project node size");
			// infoThread.setPriority(Thread.MIN_PRIORITY);
			BackgroundThreadDispatcher.addTask(infoThread);
		}
	}
	
	void getCurrentProjectSize() {
		try {
			int sz = DataExchangeHelperForExperiments.getSizeOfExperiment(experiment);
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
	
	public ExperimentReferenceInterface getExperiment() {
		return experiment;
	}
	
	public ActionListener getSizeChangedListener() {
		return sizeChangedListener;
	}
	
	public boolean mayContainData() {
		return true;
	}
	
	public void setTooltipInfo(String tooltip) {
		this.tooltip = tooltip;
	}
	
	public String getTooltipInfo() {
		return tooltip;
	}
	
	public void setIsGroup(boolean isGroupNode) {
		this.isGroupNode = isGroupNode;
	}
	
	public boolean isGroupNode() {
		return isGroupNode;
	}
	
	public void setIsSample(boolean isSampleNode) {
		this.isSampleNode = isSampleNode;
	}
	
	public boolean isSampleNode() {
		return isSampleNode;
	}
	
}
