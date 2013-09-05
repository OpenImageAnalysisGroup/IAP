package de.ipk_gatersleben.ag_pbi.mmd.visualisations.fluxdata;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.swing.AbstractButton;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JToggleButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.AttributeHelper;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ConditionInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.GraphElementHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.ipk_graffitiview.IPKGraffitiView;

public class FluxDataView extends IPKGraffitiView {
	
	@Override
	public String getViewName() {
		return "Flux Data View";
	}
	
	private static final long serialVersionUID = 1L;
	private JPanel listpanel = null;
	
	@Override
	public Object getViewToolbarComponentTop() {
		return listpanel;
	}
	
	@Override
	public void setGraph(final Graph g) {
		super.setGraph(g);
		if (g != null) {
			if (listpanel == null) {
				listpanel = new JPanel();
				listpanel.setOpaque(false);
				listpanel.setLayout(new BoxLayout(listpanel, BoxLayout.X_AXIS));
				listpanel.add(new JLabel("empty"));
			}
			
			listpanel.removeAll();
			
			TreeSet<String> names = new TreeSet<String>();
			for (GraphElement ge : g.getEdges()) {
				GraphElementHelper geh = new GraphElementHelper(ge);
				for (SubstanceInterface md : geh.getDataMappings()) {
					for (ConditionInterface sd : md)
						names.add(sd.getName());
				}
			}
			ArrayList<JComponent> buttons = new ArrayList<JComponent>();
			for (final String series : names) {
				JToggleButton commandButton = new JToggleButton(series, false);
				commandButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						AttributeHelper.setAttribute(g, "flux", "selectedcondition", series);
						completeRedraw();
					}
				});
				buttons.add(commandButton);
			}
			
			double multiplicator = (Double) AttributeHelper.getAttributeValue(g, "flux", "multiplicator", new Double(1d), new Double(1d), false);
			
			if (buttons.size() == 0)
				listpanel.add(new JLabel("<html><font color=\"red\">There are no flux distributions in the active graph!</font>"));
			else {
				listpanel.add(new JLabel("Conditions: "));
				listpanel.add(TableLayout.getMultiSplit(buttons));
				ButtonGroup bgr = new ButtonGroup();
				for (JComponent c : buttons)
					bgr.add((AbstractButton) c);
				((JToggleButton) buttons.iterator().next()).doClick();
				final JSpinner multispin = new JSpinner();
				multispin.setValue(multiplicator);
				multispin.addChangeListener(new ChangeListener() {
					@Override
					public void stateChanged(ChangeEvent e) {
						AttributeHelper.setAttribute(g, "flux", "multiplicator", multispin.getValue());
					}
				});
				listpanel.add(multispin);
			}
		}
	}
	
}
