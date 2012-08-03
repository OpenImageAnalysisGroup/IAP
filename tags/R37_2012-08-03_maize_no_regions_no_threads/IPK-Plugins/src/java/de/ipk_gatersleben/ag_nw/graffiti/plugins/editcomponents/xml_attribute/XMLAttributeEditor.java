/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.xml_attribute;

import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import org.AttributeHelper;
import org.graffiti.attributes.Attribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.ContextMenuHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.xml_data_tree_table_model.XMLdataTablePane;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.helper_classes.Experiment2GraphHelper;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class XMLAttributeEditor
					extends AbstractValueEditComponent {
	protected JButton mappingButton;
	
	public XMLAttributeEditor(final Displayable disp) {
		super(disp);
		
		mappingButton = new JButton("Mapped Data");
		mappingButton.setOpaque(false);
		mappingButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				XMLAttributeEditor.showMappedDataForSelection(ContextMenuHelper.getActiveSelection(), disp.getName());
			}
		});
	}
	
	public JComponent getComponent() {
		return mappingButton;
	}
	
	public void setEditFieldValue() {
		if (showEmpty) {
			mappingButton.setText("Mapped Data (~)");
		} else {
			try {
				ExperimentInterface o = Experiment2GraphHelper.getMappedDataListFromGraphElement((GraphElement) ((Attribute) getDisplayable()).getAttributable());
				mappingButton.setText("Mapped Data (" + o.size() + ")");
			} catch (Exception e) {
				mappingButton.setText("Mapped Data");
			}
		}
	}
	
	public void setValue() {
		// ((XMLAttribute)displayable).setColor(jButtonFontAndColor.getForeground());
	}
	
	public static void showMappedDataForSelection(Collection<GraphElement> graphElements, String attributeName) {
		if (graphElements.size() <= 0) {
			MainFrame.showMessageDialog("No nodes or edges selected!", "Error");
		} else {
			final JFrame jf = new JFrame();
			jf.setTitle("Mapped Source Data (Measurements)");
			Rectangle r = MainFrame.getInstance().getBounds();
			if (r.height > 150 && r.width > 150)
				r.grow(-100, -100);
			jf.setBounds(r);
			jf.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
			jf.setLayout(new GridLayout(1, 1));
			jf.setLocationRelativeTo(MainFrame.getInstance());
			
			final JTabbedPane jtp = new JTabbedPane();
			jtp.addContainerListener(new ContainerListener() {
				public void componentAdded(ContainerEvent e) {
				}
				
				public void componentRemoved(ContainerEvent e) {
					if (jtp.getTabCount() <= 0) {
						jf.setVisible(false);
						jf.dispose();
					}
				}
			});
			
			for (GraphElement ge : graphElements) {
				if (ge instanceof org.graffiti.graph.Node) {
					String desc = AttributeHelper.getLabel(ge, null);
					if (desc == null)
						desc = "n/a";
					
					jtp.add("<html>" + desc, new XMLdataTablePane(
										Experiment2GraphHelper.getMappedDataListFromGraphElement(ge, attributeName), jtp));
				}
				if (ge instanceof org.graffiti.graph.Edge) {
					Node nA = ((org.graffiti.graph.Edge) ge).getSource();
					Node nB = ((org.graffiti.graph.Edge) ge).getTarget();
					String descA = AttributeHelper.getLabel(nA, null);
					String descB = AttributeHelper.getLabel(nB, null);
					String descE = AttributeHelper.getLabel(ge, null);
					if (descA == null)
						descA = "n/a";
					if (descB == null)
						descB = "n/a";
					if (descE == null)
						descE = descA + " -> " + descB;
					else
						descE = descA + " -" + descE + "-&gt; " + descB;
					
					if (Experiment2GraphHelper.getMappedDataListFromGraphElement(ge) != null)
						jtp.add("<html>" + descE, new XMLdataTablePane(
											Experiment2GraphHelper.getMappedDataListFromGraphElement(ge), jtp));
				}
			}
			
			jf.getContentPane().add(jtp);
			jf.getContentPane().validate();
			
			jf.setVisible(true);
		}
	}
}
