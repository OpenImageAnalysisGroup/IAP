package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.launch_gui;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.FolderPanel;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.plugin.algorithm.AbstractEditorAlgorithm;
import org.graffiti.plugin.algorithm.Algorithm;
import org.graffiti.plugin.algorithm.ProvidesAccessToOtherAlgorithms;
import org.graffiti.plugin.view.View;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import scenario.ScenarioServiceIgnoreAlgorithm;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;

public abstract class LaunchGui extends AbstractEditorAlgorithm implements ScenarioServiceIgnoreAlgorithm,
		ProvidesAccessToOtherAlgorithms {
	
	/**
	 * All returned algorithms with getName()==null will be converted into free
	 * space to be able to visually cluster buttons.
	 * 
	 * @return A Collection of buttons for the execution of algorithms.
	 */
	protected abstract Collection<Algorithm> getAlgorithms();
	
	protected boolean modal = true;
	protected ButtonSize algBTsize = ButtonSize.DYNAMIC;
	
	@Override
	public void execute() {
		String desc = getLaunchGuiDescription();
		if (desc.equals("Select the command to be executed:") && !modal)
			desc = null;
		
		Object[] commands = getLaunchCommands();
		// if(getRealAlgorithmsSize()==1)
		// ((JButton)commands[1]).doClick();
		
		MyInputHelper.getInput("[" + (modal ? "" : "nonmodal") + "]<html>" + (desc == null ? "" : "<br>" + getLaunchGuiDescription() + "<br><br>"), getName(),
				commands);
	}
	
	public String getLaunchGuiDescription() {
		return "Select the command to be executed:";
	}
	
	@Override
	public String getName() {
		return null;
	}
	
	@Override
	public Collection<Algorithm> getAlgorithmList() {
		return getAlgorithms();
	}
	
	private Object[] getLaunchCommands() {
		Collection<Algorithm> algorithms = getAlgorithms();
		Object[] res = new Object[algorithms.size() * 2];
		Iterator<Algorithm> algIt = algorithms.iterator();
		for (int i = 0; i < res.length; i++) {
			Algorithm alg = algIt.next();
			res[i] = "";
			i++;
			if (alg == null)
				res[i] = new JLabel("<html>&nbsp;");
			else
				res[i] = getLaunchButton(alg);
		}
		return res;
	}
	
	private JComponent getLaunchButton(final Algorithm alg) {
		final Graph g = graph;
		final Selection s = selection;
		JButton res = new JButton();
		String sizetags = null;
		
		switch (algBTsize) {
			case DYNAMIC:
				sizetags = (getRealAlgorithmsSize() > 5 ? "" : "<br>");
				break;
			case LARGE:
				sizetags = "<br>";
				break;
			case SMALL:
				sizetags = "";
				break;
		}
		if (alg.getName() == null) {
			res.setText("<html>" + sizetags + "<b>" + alg.getClass().getSimpleName() + " (inactive)" + sizetags + sizetags);
			res.setEnabled(false);
		} else
			res.setText("<html>" + sizetags + "<b>" + alg.getName() + sizetags + sizetags);
		res.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (closeDialogBeforeExecution(alg))
					FolderPanel.closeParentDialog((JButton) e.getSource());
				
				// fix for the special case when an algorithm called by the launch-gui does not need an active graph (e.g. random graph generators
				EditorSession es = MainFrame.getInstance().getActiveEditorSession();
				Graph g = null;
				Selection s = null;
				if (es != null) {
					s = es.getSelectionModel().getActiveSelection();
					g = es.getGraph();
				}
				GravistoService.getInstance().runAlgorithm(alg, g, s, true, e);
			}
		});
		res.setToolTipText("<html>Click to execute algorithm,<br>dialog will " + (closeDialogBeforeExecution(alg) ? "" : "<b>not</b> ")
				+ "be closed afterwards.");
		return TableLayout.getSplitVertical(res, null, TableLayout.PREFERRED, 5);
	}
	
	private int getRealAlgorithmsSize() {
		int cnt = 0;
		for (Algorithm alg : getAlgorithms())
			if (alg != null)
				cnt++;
		return cnt;
	}
	
	protected enum ButtonSize {
		SMALL, LARGE, DYNAMIC
	}
	
	@Override
	public boolean closeDialogBeforeExecution(Algorithm algorithm) {
		return true;
	}
	
	@Override
	public boolean activeForView(View v) {
		return v != null;
	}
	
}
