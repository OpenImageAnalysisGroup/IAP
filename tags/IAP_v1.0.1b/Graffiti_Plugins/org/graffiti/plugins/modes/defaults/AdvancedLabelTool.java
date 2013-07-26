// ==============================================================================
//
// AdvancedLabelTool.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: AdvancedLabelTool.java,v 1.2 2012-11-07 14:42:19 klukas Exp $

package org.graffiti.plugins.modes.defaults;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.AlignmentSetting;
import org.AttributeHelper;
import org.FolderPanel;
import org.LabelFrameSetting;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.editor.dialog.DefaultParameterDialog;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.graphics.GraphicAttributeConstants;
import org.graffiti.graphics.LabelAttribute;
import org.graffiti.graphics.NodeLabelAttribute;
import org.graffiti.plugin.view.GraphElementComponent;
import org.graffiti.plugin.view.GraphElementComponentInterface;

/**
 * A tool for creating and editing labels of graphelements.
 * 
 * @author Wirch
 * @version $Revision: 1.2 $
 */
public class AdvancedLabelTool extends MegaTools {
	// ~ Static fields/initializers =============================================
	
	/** The value of chosen label */
	protected static String labelValue = "";
	
	/** The standard label path for capacity */
	private final static String CAPACITY = ".capacity";
	
	/** The standard label path */
	private final static String LABEL = ".label";
	
	/** The standard label path for weight */
	private final static String WEIGHT = ".weight";
	
	/**
	 * The label path of chosen label.
	 */
	private static String labelPath = "";
	
	/** The array with all stadard label paths */
	private static Object[] standardLabelPaths = { LABEL, CAPACITY, WEIGHT };
	
	// ~ Methods ================================================================
	
	public AdvancedLabelTool() {
		super();
		// normCursor = new Cursor(Cursor.HAND_CURSOR);
		edgeCursor = new Cursor(Cursor.TEXT_CURSOR);
		nodeCursor = new Cursor(Cursor.TEXT_CURSOR);
		
	}
	
	/**
	 * Invoked if user presses mouse button.
	 * 
	 * @param e
	 *           the mouse event
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		if (!SwingUtilities.isLeftMouseButton(e)) {
			return;
		}
		
		Component clickedComp = findComponentAt(e, e.getX(), e.getY());
		
		if (clickedComp instanceof GraphElementComponent) {
			((GraphElementComponent) clickedComp).getGraphElement();
			processLabelEdit((GraphElementComponent) clickedComp);
		}
	}
	
	private static Runnable editLabelCmd = null;
	private static GraphElement editGraphElement = null;
	private static DeleteAction editDeleteCmd = null;
	
	public static void setEditCommand(Runnable r) {
		editLabelCmd = r;
	}
	
	public static GraphElement getEditGraphElement() {
		return editGraphElement;
	}
	
	public static void setEditDeleteCommand(DeleteAction r) {
		editDeleteCmd = r;
	}
	
	public static void processLabelEdit(GraphElementComponentInterface clickedComp) {
		GraphElement ge = clickedComp.getGraphElement();
		processLabelEdit(ge);
	}
	
	public static void processLabelEdit(GraphElement ge) {
		if (editLabelCmd != null) {
			editGraphElement = ge;
			editLabelCmd.run();
			return;
		}
		
		Object[] param = null;
		String label = AttributeHelper.getLabel(ge, "");
		JComponent nodeMainLabelAlignmentEditor = null;
		JComboBox nodeMainLabelFrameEditor = null;
		String tooltip;
		if (ge instanceof Node) {
			tooltip = AttributeHelper.getToolTipText((Node) ge);
			if (tooltip == null)
				tooltip = "";
		} else
			tooltip = null;
		
		String url = AttributeHelper.getReferenceURL(ge);
		if (url == null)
			url = "";
		
		HashMap<Integer, JComboBox> index2alignmentSetting = new HashMap<Integer, JComboBox>();
		HashMap<Integer, JComboBox> index2frameSetting = new HashMap<Integer, JComboBox>();
		HashMap<Integer, JTextField> index2labelInput = new HashMap<Integer, JTextField>();
		if (ge instanceof Node) {
			Node n = (Node) ge;
			FolderPanel fp = new FolderPanel("", true, true, false, null);
			fp.setMaximumRowCount(5);
			boolean hasAnno = false;
			for (int index = 1; index <= 20; index++) {
				String anno = AttributeHelper.getLabel(index, n, "");
				JTextField input = new JTextField(anno);
				input.setOpaque(false);
				JComboBox cb = getAlignmentEditor(index, n);
				JComboBox cbFrame = getFrameEditor(index, n);
				index2alignmentSetting.put(index, cb);
				index2frameSetting.put(index, cbFrame);
				index2labelInput.put(index, input);
				if (input.getText().length() > 0)
					hasAnno = true;
				fp.addGuiComponentRow(TableLayout.getSplit(cb, cbFrame, TableLayoutConstants.PREFERRED,
									TableLayoutConstants.PREFERRED), input, false, 1);
			}
			fp.setFrameColor(Color.WHITE, Color.BLACK, 1, 1);
			fp.addDefaultTextSearchFilter();
			fp.addCollapseListenerDialogSizeUpdate();
			fp.setCondensedState(!hasAnno);
			fp.layoutRows();
			if (!AttributeHelper.hasAttribute(n, GraphicAttributeConstants.LABELGRAPHICS) ||
					AttributeHelper.isLabelAlignmentKnownConstant(-1, n)) {
				nodeMainLabelAlignmentEditor = getAlignmentEditor(-1, n);
			} else {
				nodeMainLabelAlignmentEditor = new JLabel("<html><font color='gray'>Relative Position (" +
						AttributeHelper.getLabelPosition(n) + ")");
			}
			nodeMainLabelFrameEditor = getFrameEditor(-1, n);
			param = new Object[] { "Label", label, "Position", nodeMainLabelAlignmentEditor, "Frame",
								nodeMainLabelFrameEditor, "Tooltip", tooltip, "URL", url, "Annotation", fp };
		} else
			if (ge instanceof Edge) {
				String consumption = AttributeHelper.getLabelConsumption((Edge) ge, "");
				String production = AttributeHelper.getLabelProduction((Edge) ge, "");
				param = new Object[] { "Label", label, "Consumption", consumption, "Production", production, "URL", url };
			} else {
				MainFrame.showMessageDialog("Unknown graph element (neither edge nor node) - can't edit label!", "Error");
				return;
			}
		Object[] input = DefaultParameterDialog
							.getInput(
									"<html>Edit the node or edge labels.<small><br><br>"
																	+ "Hint: To display special characters or line breaks, start the label text with &quot;&lt;html&gt;&quot;.<br>"
																	+ "Use HTML codes for special characters such as &alpha;, &beta;, &gamma; (&quot;&amp;alpha;&quot, &quot;&amp;beta;&quot, &quot;&amp;gamma;&quot) "
																	+ "or line breaks (&quot;&lt;br&gt;&quot;).<br></small>", "Edit Label", param);
		if (input != null) {
			if (input.length > 1) {
				String s0 = (String) input[0];
				if (AttributeHelper.getLabel(ge, null) != null || s0.length() > 0)
					AttributeHelper.setLabel(ge, s0);
				if (ge instanceof Edge) {
					String s1 = (String) input[1];
					String s2 = (String) input[2];
					if (s1.length() <= 0)
						s1 = null;
					if (s2.length() <= 0)
						s2 = null;
					AttributeHelper.setLabelConsumption((Edge) ge, s1);
					AttributeHelper.setLabelProduction((Edge) ge, s2);
					String uu2 = (String) input[3];
					if (uu2 != url)
						AttributeHelper.setReferenceURL(ge, uu2);
				} else {
					Node n = (Node) ge;
					// nodeMainLabelFrameEditor
					String lbl = (String) input[0];
					if (nodeMainLabelAlignmentEditor != null && nodeMainLabelAlignmentEditor instanceof JComboBox) {
						AlignmentSetting align = (AlignmentSetting) ((JComboBox) nodeMainLabelAlignmentEditor).getSelectedItem();
						AttributeHelper.setLabelAlignment(-1, n, align);
					}
					LabelFrameSetting style = (LabelFrameSetting) nodeMainLabelFrameEditor.getSelectedItem();
					
					AttributeHelper.setLabelFrameStyle(-1, n, style);
					
					String tt2 = (String) input[3];
					if (tt2 != tooltip)
						AttributeHelper.setToolTipText(n, tt2);
					String uu2 = (String) input[4];
					if (uu2 != url)
						AttributeHelper.setReferenceURL(n, uu2);
					for (Integer i : index2alignmentSetting.keySet()) {
						JTextField in = index2labelInput.get(i);
						lbl = in.getText();
						if (lbl.length() <= 0)
							lbl = null;
						AttributeHelper.setLabel(i, n, lbl, null, null);
						JComboBox cb = index2alignmentSetting.get(i);
						if (lbl != null) {
							AttributeHelper.setLabelAlignment(i, n, (AlignmentSetting) cb.getSelectedItem());
							style = (LabelFrameSetting) index2frameSetting.get(i).getSelectedItem();
							AttributeHelper.setLabelFrameStyle(i, n, style);
						}
					}
				}
			} else
				AttributeHelper.setLabel(ge, (String) input[0]);
		}
	}
	
	private static JComboBox getFrameEditor(int i, Node n) {
		NodeLabelAttribute nla = AttributeHelper.getLabel(i, n);
		JComboBox cb = new JComboBox(LabelFrameSetting.values());
		if (nla != null) {
			String style = nla.getFontStyle();
			if (style != null) {
				int idx = 0;
				for (LabelFrameSetting lfs : LabelFrameSetting.values()) {
					if (idx > 0) {
						if (style.contains(lfs.toGMLstring())) {
							cb.setSelectedIndex(idx);
							break;
						}
					}
					idx++;
				}
			}
		}
		return cb;
	}
	
	private static JComboBox getAlignmentEditor(int index, Node n) {
		AlignmentSetting align = AttributeHelper.getLabelAlignment(index, n);
		JComboBox cb = new JComboBox(AlignmentSetting.values());
		cb.setOpaque(false);
		cb.setSelectedItem(align);
		return cb;
	}
	
	@Override
	public void activate() {
		super.activate();
		MainFrame.showMessage("Click onto a node or an edge to edit/create a label", MessageType.INFO);
	}
	
	/**
	 * Displays the label editing dialog and saves all done changes at a label.
	 * 
	 * @param initialText
	 *           the initial value of the label, which will be displayed as first
	 *           in a combo box of the dialog.
	 * @param initialLabelName
	 *           the label, which will be displayed as first in a combo box of
	 *           the dialog.
	 * @param labelNames
	 *           all label names which will appear in a combo box of the dialog.
	 */
	protected static boolean showEditDialog(String initialText, String initialLabelName, Object[] labelNames,
						final GraphElement ge) {
		
		final JDialog labelEditingDialog = new JDialog();
		
		double border = 5;
		double size[][] = { { border, TableLayoutConstants.FILL, border }, // Columns
				{ border,
												// TableLayoutConstants.PREFERRED,
						TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED, border } }; // Rows
		
		labelEditingDialog.getContentPane().setLayout(new TableLayout(size));
		
		/** The text field for putting in the label value. */
		final JTextField labelTextField = new JTextField();
		
		final JComboBox labelNamesComboBox = new JComboBox(labelNames);
		
		boolean[] isContainedInComboBox = new boolean[standardLabelPaths.length];
		
		for (int i = 0; i < isContainedInComboBox.length; i++) {
			isContainedInComboBox[i] = false;
		}
		
		for (int i = 0; i < labelNames.length; i++) {
			for (int j = 0; j < standardLabelPaths.length; j++) {
				if (labelNames[i].equals(standardLabelPaths[j])) {
					isContainedInComboBox[j] = true;
				}
			}
		}
		
		for (int i = 0; i < isContainedInComboBox.length; i++) {
			if (!isContainedInComboBox[i])
				labelNamesComboBox.addItem(standardLabelPaths[i]);
		}
		
		JLabel labelNameLabel = new JLabel();
		JLabel labelValueLabel = new JLabel();
		
		final JButton okButton = new javax.swing.JButton();
		JButton cancelButton = new javax.swing.JButton();
		
		labelEditingDialog.setAlwaysOnTop(true);
		labelEditingDialog.setTitle("Enter a new label");
		labelEditingDialog.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		labelEditingDialog.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent evt) {
				labelEditingDialog.setVisible(false);
				labelEditingDialog.dispose();
			}
		});
		
		labelNameLabel.setText("Label-Path    ");
		
		labelNamesComboBox.setEditable(true);
		labelNamesComboBox.setSelectedItem(initialLabelName);
		
		// labelEditingDialog.add(TableLayout.getSplit(labelNameLabel,
		// labelNamesComboBox,
		// TableLayout.PREFERRED, TableLayout.FILL), "1,1");
		
		labelValueLabel.setText("Label    ");
		
		labelTextField.setMinimumSize(new Dimension(300, labelTextField.getMinimumSize().height));
		labelTextField.setColumns(10);
		labelTextField.setText(initialText);
		labelTextField.setCaretPosition(0);
		labelTextField.moveCaretPosition(initialText.length());
		
		labelEditingDialog.add(TableLayout.getSplit(labelValueLabel, labelTextField, labelNameLabel.getPreferredSize()
							.getWidth(), TableLayoutConstants.FILL), "1,1");
		
		okButton.setText("OK");
		
		labelEditingDialog.getRootPane().setDefaultButton(okButton);
		
		okButton.setMnemonic(okButton.getText().charAt(0));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				labelValue = labelTextField.getText();
				labelPath = labelNamesComboBox.getSelectedItem().toString();
				
				okButton.setText("OK!");
				labelEditingDialog.setVisible(false);
				labelEditingDialog.dispose();
			}
		});
		okButton.registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				
				if (cmd.equals("PressedENTER")) {
					((JButton) e.getSource()).doClick();
				}
			}
		}, "PressedENTER", KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		cancelButton.setText("Cancel");
		cancelButton.setMnemonic(cancelButton.getText().charAt(0));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				labelEditingDialog.setVisible(false);
				labelEditingDialog.dispose();
			}
		});
		cancelButton.registerKeyboardAction(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String cmd = e.getActionCommand();
				
				if (cmd.equals("PressedESCAPE")) {
					((JButton) e.getSource()).doClick();
				}
			}
		}, "PressedESCAPE", KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
		
		labelEditingDialog.add(TableLayout.getSplit(okButton, cancelButton, TableLayoutConstants.FILL,
							TableLayoutConstants.FILL), "1,3");
		
		labelNamesComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					labelPath = labelNamesComboBox.getSelectedItem().toString();
					
					Attribute attribute = ge.getAttribute(labelPath);
					labelTextField.setText(((LabelAttribute) attribute).getLabel());
				} catch (AttributeNotFoundException e1) {
					labelTextField.setText("");
				}
			}
		});
		
		labelEditingDialog.setLocationRelativeTo(MainFrame.getInstance());
		labelEditingDialog.setModal(true);
		labelEditingDialog.pack();
		labelEditingDialog.validate();
		labelEditingDialog.setVisible(true);
		
		return okButton.getText().equalsIgnoreCase("OK!");
	}
	
	public String getToolName() {
		return "AdvancedLabelTool";
	}
	
	/**
	 * @return
	 */
	public static DeleteAction getEditDeleteAction() {
		return editDeleteCmd;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
