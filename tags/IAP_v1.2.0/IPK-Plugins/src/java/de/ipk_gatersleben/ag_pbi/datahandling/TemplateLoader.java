package de.ipk_gatersleben.ag_pbi.datahandling;

import java.io.File;
import java.util.List;

import javax.swing.JLabel;

import org.BackgroundTaskStatusProviderSupportingExternalCall;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.ExperimentDataPresenter;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.ExperimentDataDragAndDropHandler;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.GravistoMainHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskHelper;
import de.ipk_gatersleben.ag_nw.graffiti.services.task.BackgroundTaskStatusProviderSupportingExternalCallImpl;

public abstract class TemplateLoader implements ExperimentDataDragAndDropHandler { // ,DataDragAndDropHandler

	public TemplateLoader() {
		super();
	}
	
	public void registerLoader() {
		GravistoMainHelper.addDragAndDropHandler(this);
	}
	
	public boolean hasPriority() {
		return false;
	}
	
	public boolean canProcess(File f) {
		String name = f.getAbsolutePath();
		for (String ext : getValidExtensions())
			if (name.toLowerCase().endsWith(ext.toLowerCase()))
				return true;
		return false;
	}
	
	@Override
	public abstract String toString();
	
	protected abstract String[] getValidExtensions();
	
	protected ExperimentDataPresenter receiver;
	
	public boolean process(final List<File> files) {
		if (receiver != null) {
			final BackgroundTaskStatusProviderSupportingExternalCall status = new BackgroundTaskStatusProviderSupportingExternalCallImpl("Initialisation", "");
			Runnable r = new Runnable() {
				@Override
				public void run() {
					List<ExperimentInterface> l = process(files, receiver, status);
					if (l != null)
						for (ExperimentInterface exp : l)
							receiver.processReceivedData(null, exp.getName(), exp, new JLabel(toString()));
				}
			};
			BackgroundTaskHelper.issueSimpleTask("Load " + toString(), "Please wait a moment...", r, null, status);
			return true;
		} else
			return false;
	}
	
	protected abstract List<ExperimentInterface> process(List<File> files, ExperimentDataPresenter receiver,
						BackgroundTaskStatusProviderSupportingExternalCall status);
	
	public void setExperimentDataReceiver(ExperimentDataPresenter receiver) {
		this.receiver = receiver;
	}
}
