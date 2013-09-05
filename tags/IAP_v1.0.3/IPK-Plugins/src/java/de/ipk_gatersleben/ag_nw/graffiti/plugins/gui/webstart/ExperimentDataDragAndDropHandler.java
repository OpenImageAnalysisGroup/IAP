package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataPresenter;

public interface ExperimentDataDragAndDropHandler extends DragAndDropHandler {
	
	public void setExperimentDataReceiver(ExperimentDataPresenter receiver);
}
