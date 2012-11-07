package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.FolderPanel;
import org.apache.commons.collections.set.ListOrderedSet;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.view.View;

import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;

public class ExperimentDataProcessingManager {
	
	private static ListOrderedSet processors = new ListOrderedSet();
	private static ExperimentDataProcessingManager instance;
	
	public static void addExperimentDataProcessor(ExperimentDataProcessor edp) {
		synchronized (processors) {
			processors.add(edp);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static Collection<ExperimentDataProcessor> getExperimentDataProcessors() {
		synchronized (processors) {
			return processors.asList();
		}
	}
	
	public static ExperimentDataProcessingManager getInstance() {
		if (instance == null)
			instance = new ExperimentDataProcessingManager();
		return instance;
	}
	
	public void processIncomingData(final ExperimentInterface mdsOrDocuments) {
		processIncomingData(mdsOrDocuments, null);
	}
	
	public void processIncomingData(final ExperimentInterface mdsOrDocuments, Class<ExperimentDataProcessor> processor) {
		processIncomingData(mdsOrDocuments, null, null, null, processor);
	}
	
	@SuppressWarnings("unchecked")
	public void processIncomingData(final ExperimentInterface mdsOrDocuments, final JComponent optSupplementaryPanel,
						List<Class> optIgnoredProcessors,
						final HashMap<Class, List<Runnable>> postprocessors) {
		processIncomingData(mdsOrDocuments, optSupplementaryPanel, optIgnoredProcessors, postprocessors, null);
	}
	
	@SuppressWarnings("unchecked")
	public void processIncomingData(final ExperimentInterface mdsOrDocuments,
						final JComponent optSupplementaryPanel, List<Class> optIgnoredProcessors,
						final HashMap<Class, List<Runnable>> postprocessors,
						Class<ExperimentDataProcessor> optUseOnlyThisSpecificProcessor) {
		if (mdsOrDocuments == null || mdsOrDocuments.size() == 0) {
			MainFrame.showMessageDialog("Processing canceled, because experiment is empty.", "Processing Canceled");
			return;
		}
		if (processors == null)
			return;
		
		ArrayList<AbstractExperimentDataProcessor> validProcessors = new ArrayList<AbstractExperimentDataProcessor>();
		for (Object ep : processors.toArray())
			// check if ep is not ignored
			if (optIgnoredProcessors == null || !optIgnoredProcessors.contains(ep.getClass())) {
				// if not ignored, check if only a specific processor is valid, if
				// only specific, check if ep the right one
				if ((optUseOnlyThisSpecificProcessor != null && optUseOnlyThisSpecificProcessor.isInstance(ep)) || optUseOnlyThisSpecificProcessor == null)
					validProcessors.add((AbstractExperimentDataProcessor) ep);
			}
		
		if (validProcessors.size() == 0)
			return;
		if (validProcessors.size() == 1) {
			processData(mdsOrDocuments,
								((validProcessors.iterator().next())),
								null,
								optSupplementaryPanel,
								postprocessors != null ? postprocessors.get(validProcessors.iterator().next().getClass()) : null);
			return;
		}
		ArrayList<JButton> actions = new ArrayList<JButton>();
		for (Object o : validProcessors) {
			final AbstractExperimentDataProcessor pp = (AbstractExperimentDataProcessor) o;
			MappingButton bb = new MappingButton(pp);
			bb.setToolTipText("Click to choose experiment data processor");
			bb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					FolderPanel.closeParentDialog((JButton) e.getSource());
					processData(mdsOrDocuments, pp, e, optSupplementaryPanel, postprocessors != null ? postprocessors.get(pp.getClass()) : null);
				}
			});
			actions.add(bb);
		}
		
		Object[] res = new Object[actions.size() * 4 - 2];
		Iterator<JButton> buttonIt = actions.iterator();
		for (int i = 0; i < res.length;) {
			res[i++] = "";
			res[i++] = buttonIt.next();
			if (i < res.length) {
				res[i++] = "";
				res[i++] = new JLabel("<html><small>&nbsp;");
			}
		}
		
		MyInputHelper.getInput("[nonmodal]", "" + mdsOrDocuments.getName(), res);
	}
	
	public void processData(ExperimentInterface mdsOrDocuments, AbstractExperimentDataProcessor pp, ActionEvent e, JComponent optSupplementaryPanel,
						List<Runnable> postProcessors) {
		if (postProcessors != null)
			pp.addPostProcessor(postProcessors);
		try {
			ExperimentInterface docs = mdsOrDocuments;
			
			pp.setExperimentData(docs);
			pp.setComponent(optSupplementaryPanel);
			View v;
			try {
				v = MainFrame.getInstance().getActiveEditorSession().getActiveView();
			} catch (Exception err) {
				v = null;
			}
			if (pp.activeForView(v))
				GravistoService.getInstance().runAlgorithm(pp, e);
		} finally {
			if (postProcessors != null)
				pp.removePostProcessor(postProcessors);
		}
	}
	
}
