// ==============================================================================
//
// DefaultEditPanel.java
//
// Copyright (c) 2001-2004 Gravisto Team, University of Passau
//
// ==============================================================================
// $Id: DefaultEditPanel.java,v 1.2 2013-01-26 18:02:16 klukas Exp $

package org.graffiti.plugins.inspectors.defaults;

import info.clearthought.layout.TableLayout;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.undo.AbstractUndoableEdit;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoableEditSupport;

import org.AttributeHelper;
import org.ErrorMsg;
import org.FolderPanel;
import org.GuiRow;
import org.JLabelJavaHelpLink;
import org.JMButton;
import org.Release;
import org.ReleaseInfo;
import org.StringManipulationTools;
import org.graffiti.attributes.AbstractCollectionAttribute;
import org.graffiti.attributes.Attributable;
import org.graffiti.attributes.Attribute;
import org.graffiti.attributes.AttributeNotFoundException;
import org.graffiti.attributes.CollectionAttribute;
import org.graffiti.attributes.CompositeAttribute;
import org.graffiti.attributes.StringAttribute;
import org.graffiti.editor.MainFrame;
import org.graffiti.event.ListenerManager;
import org.graffiti.graph.Graph;
import org.graffiti.graph.GraphElement;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.ToolTipHelper;
import org.graffiti.plugin.editcomponent.StandardValueEditComponent;
import org.graffiti.plugin.editcomponent.ValueEditComponent;
import org.graffiti.plugin.inspector.EditPanel;
import org.graffiti.plugin.view.View;
import org.graffiti.undo.ChangeAttributesEdit;
import org.graffiti.util.InstanceCreationException;
import org.graffiti.util.InstanceLoader;
import org.graffiti.util.PluginHelper;

/**
 * Represents the edit panel in the inspector.
 * 
 * @version $Revision: 1.2 $
 */
@SuppressWarnings("unchecked")
public class DefaultEditPanel extends EditPanel {
	
	private static final long serialVersionUID = 1L;
	
	// ~ Static fields/initializers =============================================
	
	/** The logger for the current class. */
	static final Logger logger = Logger.getLogger(DefaultEditPanel.class
						.getName());
	
	// ~ Instance fields ========================================================
	
	/** Action for the apply button. */
	private Action applyAction;
	
	/** The attribute that was last specified by buildTable. */
	Attribute displayedAttr;
	
	// private DefaultMutableTreeNode rootNode;
	
	/**
	 * Mapping between a ValueEditComponent and the List of attributes that are
	 * linked to it.
	 */
	private HashMap<ValueEditComponent, Collection<Displayable>> mapValueEditComponent2AttributeList;
	
	JScrollPane attributeScrollPanel;
	
	List<ValueEditComponent> displayedValueEditComponents;
	
	private Collection<Attributable> graphElements;
	
	/** Holds the ListenerManager where the panel is registered. */
	ListenerManager listenerManager;
	
	/** Stores all edit components. */
	private Map editComponentsMap;
	
	private final String emptyMessage;
	
	private static HashSet<String> discardedRowIDs = new HashSet<String>();
	
	private boolean showCompleteRedrawCommand = true;
	
	// ~ Constructors ===========================================================
	
	/**
	 * Instantiates a new edit panel.
	 */
	public DefaultEditPanel(String emptyMessage) {
		super();
		this.emptyMessage = emptyMessage;
		if (discardedRowIDs == null || discardedRowIDs.size() <= 0) {
			discardedRowIDs = new HashSet<String>();
			// discardedRowIDs.add("linemode");
			discardedRowIDs.add("image");
			discardedRowIDs.add("tiled");
			discardedRowIDs.add("maximize");
			discardedRowIDs.add("refrence");
			discardedRowIDs.add("linetype");
			discardedRowIDs.add("image");
			discardedRowIDs.add("titled");
			discardedRowIDs.add("maximize");
			discardedRowIDs.add("reference");
			
			// discardedRowIDs.add("target");
			// discardedRowIDs.add("source");
			
			// discardedRowIDs.add("arrowhead");
			// discardedRowIDs.add("arrowtail");
			discardedRowIDs.add("data");
			// discardedRowIDs.add("fontSize");
			// discardedRowIDs.add("fontStyle");
			
			discardedRowIDs.add("alignment");
			discardedRowIDs.add("relVert");
			discardedRowIDs.add("relHor");
			discardedRowIDs.add("absVert");
			discardedRowIDs.add("absHor");
			discardedRowIDs.add("relAlign");
			discardedRowIDs.add("alignSegment");
			discardedRowIDs.add("localAlign");
			
			discardedRowIDs.add("type");
			discardedRowIDs.add("Edge:anchor");
			discardedRowIDs.add("Edge:rounding");
			discardedRowIDs.add("data");
			
			if (ReleaseInfo.getRunningReleaseStatus() == Release.KGML_EDITOR) {
				addDiscarded(discardedRowIDs, new String[] {
									"frameThickness", "linemode", "rounding", "Edge:fill", "Edge:color", "Edge:text",
									"shape", "fontName", "fontSize", "anchor",
									"clustergraph", "fontStyle", "thickness", "relVert", "relHor", "alignSegment",
									"outline", "directed", "empty_border_width", "empty_border_width_vert", "arrowhead", "arrowtail",
									"gradient",
									"background_coloring",
									"clusterbackground_fill_outer_region",
									"clusterbackground_space_fill",
									"clusterbackground_radius",
									"clusterbackground_low_alpha",
									"clusterbackground_grid" });
			}
			
		}
		displayedValueEditComponents = new LinkedList<ValueEditComponent>();
		
		// this.attributeTypeMap = new HashMap();
		this.editComponentsMap = new HashMap();
		
		initGUI();
	}
	
	public void initGUI() {
		/** Button used to apply all changes. */
		JButton applyButton;
		JButton applyRedrawButton;
		
		applyButton = new JMButton("Apply Changes");
		applyButton
							.setToolTipText("<html>Apply changes to graph.<br>If results get not visible use the button \"Apply & Redraw\" instead.<br>Hint: After changing a setting you may press [Enter] to apply the changes");
		applyButton.setDefaultCapable(true);
		applyButton.setMnemonic(1);
		applyAction = new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			public void actionPerformed(ActionEvent e) {
				applyChanges();
			}
		};
		applyButton.addActionListener(applyAction);
		
		applyRedrawButton = new JMButton("Complete Redraw");
		// applyRedrawButton.setToolTipText("<html>Apply changes & issue a complete redraw of the view.<br>Use this if the \"Apply\" command button gives no immediate result.<br>- this command requires more time to complete");
		// applyRedrawButton.setDefaultCapable(true);
		applyRedrawButton.setMnemonic(1);
		applyRedrawButton.setOpaque(false);
		applyRedrawButton.addActionListener(new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			public void actionPerformed(ActionEvent e) {
				if (MainFrame.getInstance().getActiveEditorSession() == null)
					return;
				applyChanges();
				issueCompleteRedrawForView(
									MainFrame.getInstance().getActiveEditorSession().getActiveView(),
									MainFrame.getInstance().getActiveEditorSession().getGraph());
			}
		}
							);
		
		attributeScrollPanel = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		attributeScrollPanel.getViewport().setOpaque(false);
		attributeScrollPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		// applyButtonPanel.add(applyButton, BorderLayout.WEST);
		// applyButtonPanel.add(applyRedrawButton, BorderLayout.EAST);
		
		double[][] size = new double[][] {
							new double[] { TableLayout.FILL },
							new double[] { TableLayout.PREFERRED, TableLayout.FILL }
		};
		setLayout(new TableLayout(size));
		
		// Do not show add attribute and remove attribute buttons
		// add(attributeButtonPanel, BorderLayout.NORTH);
		
		if (showCompleteRedrawCommand)
			add(TableLayout.getSplit(applyButton, applyRedrawButton, TableLayout.FILL, TableLayout.PREFERRED), "0,0");
		else
			add(applyButton, "0,0");
		
		add(attributeScrollPanel, "0,1");
		validate();
		
		// call apply when user hits enter
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
							KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0),
							"apply");
		getActionMap().put("apply", applyAction);
	}
	
	private void addDiscarded(HashSet<String> discardedRowIDs2, String[] strings) {
		for (String val : strings)
			discardedRowIDs2.add(val);
	}
	
	// ~ Methods ================================================================
	
	/**
	 * @param activeView
	 */
	public static void issueCompleteRedrawForView(final View activeView, final Graph g) {
		if (SwingUtilities.isEventDispatchThread()) {
			setDoubleBuffered((JComponent) activeView, true);
			activeView.setGraph(g);
		} else {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					if (activeView == null || g == null)
						return;
					setDoubleBuffered((JComponent) activeView, true);
					activeView.setGraph(g);
				}
			});
		}
	}
	
	private static void setDoubleBuffered(JComponent jc, boolean val) {
		if (jc == null)
			return;
		jc.setDoubleBuffered(false);
		Component[] comps = jc.getComponents();
		
		for (int i = 0; i < comps.length; i++) {
			if (comps[i] instanceof JComponent)
				setDoubleBuffered((JComponent) comps[i], val);
		}
	}
	
	/**
	 * Sets the map of editcomponents to the given map.
	 * 
	 * @param map
	 *           DOCUMENT ME!
	 */
	@Override
	public void setEditComponentMap(Map map) {
		this.editComponentsMap = map;
	}
	
	/**
	 * Sets the ListenerManager.
	 * 
	 * @param lm
	 *           DOCUMENT ME!
	 */
	@Override
	public void setListenerManager(ListenerManager lm) {
		this.listenerManager = lm;
	}
	
	/**
	 * Builds the table that is used for editing attributes from scratch.
	 * 
	 * @param treeNode
	 *           root attribute.
	 * @param graphElements
	 *           DOCUMENT ME!
	 */
	@Override
	public void buildTable(DefaultMutableTreeNode treeNode, Collection<Attributable> graphElements, String tabName) {
		// rootNode = treeNode;
		
		Attribute collAttr = ((BooledAttribute) treeNode.getUserObject())
							.getAttribute();
		
		displayedAttr = collAttr;
		this.graphElements = graphElements;
		
		printGraphElements();
		
		mapValueEditComponent2AttributeList = new HashMap<ValueEditComponent, Collection<Displayable>>();
		
		displayedValueEditComponents = new LinkedList<ValueEditComponent>();
		
		// attributePanel.setLayout(new TableLayout(mySizeArray));
		Collection<JComponent> rows = getValueEditComponents(treeNode, tabName);
		
		// extract information about categories
		JComponent leftComp = null;
		Set<String> groups = new HashSet<String>();
		for (JComponent jc : rows) {
			if (leftComp == null) {
				leftComp = jc;
				String desc = "";
				if (leftComp instanceof ClickableInspectorLabel)
					desc = ((ClickableInspectorLabel) leftComp).getLabel().getText();
				else
					desc = ((JLabel) leftComp).getText();
				String group = null;
				if (desc.contains(":")) {
					group = desc.substring(0, desc.indexOf(":")).trim();
					groups.add(group);
				}
			} else {
				leftComp = null;
			}
		}
		ArrayList<String> sortedGroups = new ArrayList<String>();
		TreeSet<String> listA = new TreeSet<String>();
		TreeSet<String> listB = new TreeSet<String>();
		String noGroup = tabName + " Attributes";
		groups.add(noGroup);
		for (String g : groups) {
			if (g.indexOf("<") >= 0)
				listB.add(g);
			else
				listA.add(g);
		}
		sortedGroups.addAll(listA);
		sortedGroups.addAll(listB);
		
		ArrayList<JComponent> panels = new ArrayList<JComponent>();
		
		HashMap<String, ArrayList<GuiRow>> hashGroup2GuiRows = new HashMap<String, ArrayList<GuiRow>>();
		// JPanel attributePanel = new JPanel();
		// attributePanel.setLayout(new SingleFiledLayout(SingleFiledLayout.COLUMN, SingleFiledLayout.FULL, 5));
		
		boolean allEmpty = true;
		
		for (String groupMatch : sortedGroups) {
			boolean matchOK = false;
			String groupName = null;
			for (JComponent jc : rows) {
				if (leftComp == null) {
					leftComp = jc;
					String txt = "";
					if (leftComp instanceof ClickableInspectorLabel)
						txt = ((ClickableInspectorLabel) leftComp).getLabel().getText();
					else
						txt = ((JLabel) leftComp).getText();
					if (txt.indexOf(":") > 0) {
						groupName = txt.substring(0, txt.indexOf(":")).trim();
						if (groupName.equals(groupMatch)) {
							String tt = leftComp.getToolTipText();
							if (leftComp instanceof ClickableInspectorLabel)
								leftComp = new ClickableInspectorLabel(txt.substring(txt.indexOf(":") + 1), (ClickableInspectorLabel) leftComp);
							else
								leftComp = new JLabel(txt.substring(txt.indexOf(":") + 1));
							leftComp.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));
							leftComp.setToolTipText(tt);
						}
					} else {
						groupName = noGroup;
					}
					matchOK = groupName.equals(groupMatch);
				} else {
					if (matchOK) {
						ArrayList<GuiRow> myRows = hashGroup2GuiRows.get(groupMatch);
						if (myRows == null) {
							myRows = new ArrayList<GuiRow>();
							hashGroup2GuiRows.put(groupMatch, myRows);
						}
						myRows.add(new GuiRow(leftComp, jc));
					}
					leftComp = null;
				}
			}
			
			String helpTopic = AttributeHelper.getHelpTopicFor(tabName, groupMatch);
			FolderPanel myPanel = new FolderPanel("<html><b>&nbsp;" + groupMatch, true,
								JLabelJavaHelpLink.getHelpActionListener(helpTopic), helpTopic);
			
			ArrayList<GuiRow> myRows = hashGroup2GuiRows.get(groupMatch);
			if (myRows != null) {
				for (GuiRow gr : myRows)
					myPanel.addGuiComponentRow(gr, false);
				myPanel.mergeRowsWithSameLeftLabel();
			}
			
			// myPanel.setFrameColor(new JTabbedPane().getBackground(), null, 2, 4);
			// myPanel.setBackground(Color.WHITE);
			myPanel.layoutRows();
			
			if (myPanel.getRowCount() > 0) {
				panels.add(myPanel.getBorderedComponent(0, 0, 7, 0));
				allEmpty = false;
			}
		}
		
		if (allEmpty)
			panels.add(new JLabel(emptyMessage));
		
		attributeScrollPanel.setViewportView(TableLayout.getMultiSplitVertical(panels));
		validate();
		repaint();
	}
	
	private void printGraphElements() {
		// System.out.print("GEs: ");
		// for (Iterator it = graphElements.iterator(); it.hasNext();) {
		// Object o = it.next();
		// System.out.print(o.toString()+"; ");
		// }
		// System.out.println();
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param attr
	 *           DOCUMENT ME!
	 */
	@Override
	public void updateTable(Attribute attr) {
		// if (this.graphElements.size() > 1) {
		// buildTable(this.rootNode, this.graphElements);
		// } else {
		updateVECs(attr);
		
		// }
	}
	
	/**
	 * Updates all attributes linked with the given ValueEditComponent to the
	 * value displayed by the ValueEditComponent.
	 * 
	 * @param valueEditComponent
	 *           DOCUMENT ME!
	 */
	void setValues(ValueEditComponent valueEditComponent) {
		if (!valueEditComponent.isEnabled()) {
			return;
		}
		Collection<Displayable> attributes = mapValueEditComponent2AttributeList.get(valueEditComponent);
		// System.out.print("#"+attributes.size()+" ");
		valueEditComponent.setValue(attributes);
		// System.out.println("");
	}
	
	/**
	 * Adds one row of the table.
	 * 
	 * @param idPanel
	 *           DOCUMENT ME!
	 * @param editFieldPanel
	 *           DOCUMENT ME!
	 * @param attribute
	 *           DOCUMENT ME!
	 * @param ecClass
	 *           DOCUMENT ME!
	 * @param showValue
	 *           DOCUMENT ME!
	 * @throws RuntimeException
	 *            DOCUMENT ME!
	 */
	private Collection<JComponent> getRow(final Attribute attribute, Class ecClass, boolean showValue,
						String tabName) {
		Collection<JComponent> result = new ArrayList<JComponent>();
		String id = attribute.getId();
		
		if (discardedRowIDs.contains(id) || discardedRowIDs.contains(tabName + ":" + id)) {
			return result;
		}
		if (ecClass == null) {
			ErrorMsg.addErrorMessage("Internal Error: ecClass is NULL!");
			return result;
		}
		
		id = AttributeHelper.getDefaultAttributeDescriptionFor(attribute.getId(), tabName, attribute);
		
		JComponent textField;
		
		final String delPath = AttributeHelper.getToBeDeletedPathFromAttributePath(attribute.getPath());
		if (delPath != null)
			textField = new ClickableInspectorLabel(id, new Runnable() {
				@Override
				public void run() {
					deleteAttribute(delPath);
				}
			});
		else
			textField = new JLabel(id);
		textField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));
		String tttext = attribute.getPath();
		
		if (attribute.getDescription() != null && !attribute.getDescription().equals("")) {
			tttext = "<html>" + tttext + ":<p>" + attribute.getDescription()
								+ "</html>";
		}
		
		textField.setToolTipText(tttext);
		if (textField instanceof ClickableInspectorLabel)
			((ClickableInspectorLabel) textField).getLabel().setHorizontalAlignment(JTextField.RIGHT);
		else
			((JLabel) textField).setHorizontalAlignment(JTextField.RIGHT);
		// textField.setEditable(false);
		
		ValueEditComponent editComponent = null;
		try {
			// editComp = (ValueEditComponent)ecClass.newInstance();
			editComponent = (ValueEditComponent) InstanceLoader.createInstance(
								ecClass, "org.graffiti.plugin.Displayable", attribute);
			
			JComponent addToolTipTo = editComponent.getComponent();
			ToolTipHelper.addToolTip(addToolTipTo, tttext);
		} catch (InstanceCreationException ice) {
			ErrorMsg.addErrorMessage("Could not create an instance of a ValueEditComponent class (" + ecClass.getCanonicalName() + ").<br>" + ice.getMessage());
			return result;
		}
		
		editComponent.setEditFieldValue();
		
		JComponent editComponentViewComponent = editComponent.getComponent();
		
		textField.setMinimumSize(new Dimension(0, editComponentViewComponent
							.getMinimumSize().height));
		textField.setMaximumSize(new Dimension(textField.getMaximumSize().width,
							editComponentViewComponent.getMaximumSize().height));
		// textField.setPreferredSize(new Dimension(
		// textField.getPreferredSize().width, editComponentViewComponent
		// .getPreferredSize().height));
		// textField.setSize(new Dimension(textField.getSize().width,
		// editComponentViewComponent.getSize().height));
		result.add(textField);
		result.add(editComponentViewComponent);
		synchronized (displayedValueEditComponents) {
			displayedValueEditComponents.add(editComponent);
		}
		
		/*
		 * when a spinner is used then its editor (or the
		 * textfield within the editor) must be connected to
		 * the action event
		 */
		JComponent inputComp = null;
		
		if (editComponentViewComponent instanceof JSpinner) {
			// get editor of spinner
			inputComp = ((JSpinner) editComponentViewComponent).getEditor();
			
			if (inputComp instanceof JSpinner.DefaultEditor) {
				// in this case, the TextField inside the editor has to be used
				inputComp = ((JSpinner.DefaultEditor) inputComp).getTextField();
			} else {
				inputComp = editComponentViewComponent;
			}
		} else {
			inputComp = editComponentViewComponent;
		}
		
		inputComp.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
							KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0),
							"apply");
		inputComp.getActionMap().put("apply", applyAction);
		inputComp.getInputMap(WHEN_FOCUSED).put(
							KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0),
							"apply");
		inputComp.getActionMap().put("apply", applyAction);
		inputComp.getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
							KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0),
							"apply");
		inputComp.getActionMap().put("apply", applyAction);
		
		// save which attributes are dependent on this vec
		Collection<Displayable> attrList = mapValueEditComponent2AttributeList.get(editComponent);
		
		if (attrList == null) {
			attrList = new HashSet<Displayable>();
			mapValueEditComponent2AttributeList.put(editComponent, attrList);
		}
		addAttributesToAttributeList(attribute, attrList);
		
		// editComp.setEnabled(showValue);
		editComponent.setShowEmpty(!showValue);
		return result;
	}
	
	protected void deleteAttribute(final String delPath) {
		
		AbstractUndoableEdit delAttrCmd = new AbstractUndoableEdit() {
			private static final long serialVersionUID = 1L;
			
			private HashMap<Attributable, ArrayList<Attribute>> deletedAttributes = new HashMap<Attributable, ArrayList<Attribute>>();
			final Collection<Attributable> attributables = graphElements;
			
			@Override
			public String getPresentationName() {
				return "Delete Attribute";
			}
			
			@Override
			public String getRedoPresentationName() {
				return "Redo Delete Attribute";
			}
			
			@Override
			public String getUndoPresentationName() {
				return "Undo Delete Attribute";
			}
			
			@Override
			public void redo() throws CannotRedoException {
				deletedAttributes = new LinkedHashMap<Attributable, ArrayList<Attribute>>();
				for (Attributable ge : attributables)
					for (String del : StringManipulationTools.splitSafe(delPath, "$")) {
						try {
							if (!deletedAttributes.containsKey(ge))
								deletedAttributes.put(ge, new ArrayList<Attribute>());
							deletedAttributes.get(ge).add(ge.removeAttribute(del));
						} catch (Exception e) {
							System.err.println("Could not delete attribute " + del + ", as it doesn't exist. ");
						}
					}
			}
			
			@Override
			public void undo() throws CannotUndoException {
				for (Attributable atbl : deletedAttributes.keySet())
					for (Attribute attr : deletedAttributes.get(atbl)) {
						String path = attr.getParent().getPath();
						if (!AttributeHelper.hasAttribute(atbl, path)) {
							AttributeHelper.addAttributeFolder(atbl, path);
						}
						AbstractCollectionAttribute parent = (AbstractCollectionAttribute) AttributeHelper.getAttribute(atbl, path);
						parent.add((Attribute) attr.copy());
					}
			}
		};
		
		delAttrCmd.redo();
		
		// if (gra == MainFrame.getInstance().getActiveSession().getGraph()) {
		UndoableEditSupport undo = MainFrame.getInstance().getUndoSupport();
		undo.beginUpdate();
		undo.postEdit(delAttrCmd);
		undo.endUpdate();
		
		// undoSupport.beginUpdate();
		// undoSupport.postEdit(delAttrCmd);
		// undoSupport.endUpdate();
		// }
		
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param attr
	 *           DOCUMENT ME!
	 * @param idPanel
	 *           DOCUMENT ME!
	 * @param editFieldPanel
	 *           DOCUMENT ME!
	 * @param showValue
	 *           DOCUMENT ME!
	 */
	private Collection<JComponent> getStandardRow(Attribute attr, boolean showValue,
						String tabName) {
		Collection<JComponent> result = new ArrayList<JComponent>();
		ValueEditComponent standardVEC = new StandardValueEditComponent(attr);
		standardVEC.setDisplayable(attr);
		standardVEC.setEditFieldValue();
		
		JComponent editComponent = standardVEC.getComponent();
		String id = attr.getId();
		if (discardedRowIDs.contains(id) || discardedRowIDs.contains(tabName + ":" + id)) {
			return result;
		}
		id = AttributeHelper.getDefaultAttributeDescriptionFor(attr.getId(), tabName, attr);
		
		if (id.length() >= 1) {
			id = id.substring(0, 1).toUpperCase() + id.substring(1);
		}
		JLabel textField = new JLabel(id); // JTextField
		textField.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 3));
		textField.setToolTipText(attr.getPath());
		textField.setHorizontalAlignment(JTextField.RIGHT);
		// textField.setEditable(false);
		
		textField.setMinimumSize(new Dimension(0,
							editComponent.getMinimumSize().height));
		textField.setMaximumSize(new Dimension(textField.getMaximumSize().width,
							editComponent.getMaximumSize().height));
		// textField.setPreferredSize(new Dimension(
		// textField.getPreferredSize().width, editComponent
		// .getPreferredSize().height));
		// textField.setSize(new Dimension(textField.getSize().width, editComponent
		// .getSize().height));
		result.add(textField);
		result.add(editComponent);
		synchronized (displayedValueEditComponents) {
			displayedValueEditComponents.add(standardVEC);
		}
		
		Collection<Displayable> attrList = mapValueEditComponent2AttributeList.get(standardVEC);
		
		if (attrList == null) {
			attrList = new ArrayList<Displayable>();
			mapValueEditComponent2AttributeList.put(standardVEC, attrList);
		}
		
		printGraphElements();
		addAttributesToAttributeList(attr, attrList);
		standardVEC.setShowEmpty(!showValue);
		return result;
	}
	
	private void addAttributesToAttributeList(Attribute attr, Collection<Displayable> attrList) {
		printGraphElements();
		// System.out.println("Check: "+attr.getPath());
		String attPath = attr.getPath().startsWith(".") ? (attr.getPath() + " ").substring(1).trim() : attr.getPath().trim();
		for (Attributable attbl : graphElements) {
			Attribute oAttr;
			try {
				oAttr = attbl.getAttribute(attPath);
			} catch (AttributeNotFoundException e) {
				oAttr = null;
			}
			if (oAttr != null) {
				attrList.add(oAttr);
			} else
				System.err.println("ERR ATTRIBUTE PROBLEM: PATH=" + attPath);
		}
	}
	
	/**
	 * Puts text fields for the IDs in a panel.
	 * 
	 * @param idPanel
	 * @param editFieldPanel
	 * @param treeNode
	 *           DOCUMENT ME!
	 * @param graphElements
	 *           DOCUMENT ME!
	 */
	private Collection<JComponent> getValueEditComponents(DefaultMutableTreeNode treeNode, String tabName) {
		
		Collection<JComponent> result = new ArrayList<JComponent>();
		
		BooledAttribute booledAttr = (BooledAttribute) treeNode.getUserObject();
		Attribute attr = booledAttr.getAttribute();
		
		if (attr instanceof CollectionAttribute) {
			/*
			 * if it is a CollectionAttribute, we check if there is a component
			 * registered.
			 */
			Class ecClass = (Class) this.editComponentsMap.get(attr.getClass());
			
			if (ecClass != null) {
				// if we have a registered component to display it, add it
				result.addAll(getRow(attr, ecClass, booledAttr.getBool(), tabName));
			} else {
				/*
				 * If no component is registered, we iterate through its
				 * collection and check if these attributes have a registered
				 * component
				 */

				DefaultMutableTreeNode child;
				BooledAttribute booledChild;
				
				for (int i = 0; i < treeNode.getChildCount(); i++) {
					child = (DefaultMutableTreeNode) treeNode.getChildAt(i);
					booledChild = (BooledAttribute) child.getUserObject();
					
					Attribute attribute = booledChild.getAttribute();
					
					ecClass = (Class) this.editComponentsMap.get(attribute
										.getClass());
					
					if (ecClass != null) {
						// if we have a registered component, add it
						result.addAll(getRow(attribute, ecClass, booledChild.getBool(), tabName));
					} else {
						// recursive call if no special component is registered
						result.addAll(getValueEditComponents(child, tabName));
					}
				}
			}
		} else
			if (attr instanceof CompositeAttribute) {
				/*
				 * nearly the same for CompositeAttributes. Check is a component
				 * is registered. If not, recursive call with its hierarchy form.
				 */

				Class ecClass = (Class) this.editComponentsMap.get(attr.getClass());
				
				if (ecClass != null) {
					result.addAll(getRow(attr, ecClass, booledAttr.getBool(), tabName));
				} else {
					DefaultMutableTreeNode child;
					BooledAttribute booledChild;
					
					if (treeNode.getChildCount() == 0) {
						result.addAll(getStandardRow(attr, booledAttr
											.getBool(), tabName));
					} else {
						for (int i = 0; i < treeNode.getChildCount(); i++) {
							child = (DefaultMutableTreeNode) treeNode.getChildAt(i);
							booledChild = (BooledAttribute) child.getUserObject();
							
							Attribute attribute = booledChild.getAttribute();
							
							ecClass = (Class) this.editComponentsMap.get(attribute
												.getClass());
							if (ecClass != null) {
								// if we have a registered component, add it
								result.addAll(getRow(attribute, ecClass, booledChild.getBool(), tabName));
							} else {
								// recursive call if no special component is registered
								result.addAll(getValueEditComponents(child, tabName));
							}
						}
					}
				}
			} else {
				/*
				 * for non CollectionAttributes and non CompositeAttributes
				 * check whether there exists a ValueEditComponent, if not use
				 * standard edit component
				 */
				if (attr != null) {
					Class ecClass = (Class) this.editComponentsMap.get(attr.getClass());
					
					if (ecClass != null) {
						// if we have a registered component to display it, add it
						result.addAll(getRow(attr, ecClass, booledAttr.getBool(), tabName));
					} else {
						result.addAll(getStandardRow(attr, booledAttr.getBool(), tabName));
					}
				}
			}
		return result;
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @param changedAttr
	 *           DOCUMENT ME!
	 */
	private void updateVECs(Attribute changedAttr) {
		ArrayList<ValueEditComponent> dl;
		synchronized (displayedValueEditComponents) {
			if (displayedValueEditComponents == null || displayedValueEditComponents.size() == 0)
				return;
			dl = new ArrayList<ValueEditComponent>(displayedValueEditComponents);
		}
		for (ValueEditComponent vec : dl) {
			Collection<Displayable> attrs = mapValueEditComponent2AttributeList.get(vec);
			if (changedAttr != null && attrs != null && attrs.contains(changedAttr)) {
				// if changedAttr and vec have something to do with each other ...
				Object attrValue = changedAttr.getValue();
				if (attrValue == null) {
					vec.setShowEmpty(true);
				} else {
					boolean allSame = true;
					for (Displayable checkAttr : attrs) {
						if (!(checkAttr).getValue().equals(attrValue)) {
							allSame = false;
							break;
						}
					}
					if (allSame) {
						vec.setShowEmpty(false);
					} else {
						vec.setShowEmpty(true);
					}
				}
			}
		}
	}
	
	// ~ Inner Classes ==========================================================
	
	private void applyChanges() {
		
		listenerManager.transactionStarted(this);
		try {
			HashMap<Displayable, Object> attributeToOldValueMap = new LinkedHashMap<Displayable, Object>();
			
			synchronized (displayedValueEditComponents) {
				for (ValueEditComponent valueEditComponent : displayedValueEditComponents) {
					// save original attributes for undo
					Collection<Displayable> attributes = mapValueEditComponent2AttributeList.get(valueEditComponent);
					
					for (Displayable attr : attributes) {
						Attribute a = (Attribute) attr;
						attributeToOldValueMap.put(attr, ((Attribute) a.copy()).getValue());
					}
					
					setValues(valueEditComponent);
				}
			}
			
			assert (geMap != null);
			
			if (MainFrame.getInstance().getActiveEditorSession() != null) {
				ChangeAttributesEdit aEdit = new ChangeAttributesEdit(
								MainFrame.getInstance().getActiveEditorSession().getGraph(),
								attributeToOldValueMap, geMap);
				MainFrame.getInstance().getUndoSupport().postEdit(aEdit);
			}
		} finally {
			listenerManager.transactionFinished(this);
		}
	}
	
	public static void setDiscardedRowIDs(HashSet<String> discardedRowIDs) {
		DefaultEditPanel.discardedRowIDs = discardedRowIDs;
	}
	
	public static Collection<String> getDiscardedRowIDs() {
		return discardedRowIDs;
	}
	
	@SuppressWarnings("unused")
	private class AddListener implements ActionListener {
		/**
		 * DOCUMENT ME!
		 * 
		 * @param e
		 *           DOCUMENT ME!
		 */
		public void actionPerformed(ActionEvent e) {
			if (!(displayedAttr instanceof CollectionAttribute)) {
				JOptionPane.showMessageDialog(DefaultEditPanel.this,
									"Can't add a sub attribute to a non "
														+ "CollectionAttribute like " + displayedAttr, "Error",
									JOptionPane.OK_OPTION);
			} else {
				AttributeSelector attrSelector = new AttributeSelector(null,
									displayedAttr.getName());
				String attrName = attrSelector.getAttributeLabel();
				String typeName = attrSelector.getAttributeClassname();
				
				if (typeName != null && !typeName.equals("")) {
					Attributable attributable = displayedAttr.getAttributable();
					Graph graph = null;
					
					if (attributable instanceof Graph) {
						graph = (Graph) attributable;
					} else {
						graph = ((GraphElement) attributable).getGraph();
					}
					
					graph.getListenerManager().transactionStarted(this);
					
					if (typeName.indexOf(".") == -1) {
						try {
							Attribute newAttr = (Attribute) InstanceLoader
												.createInstance("org.graffiti.graphics." + typeName,
																	attrName);
							
							String path = (displayedAttr.getPath() + " ").substring(1)
												.trim();
							
							for (Iterator geit = graphElements.iterator(); geit
												.hasNext();) {
								Attributable atbl = (Attributable) geit.next();
								atbl.addAttribute((Attribute) newAttr.copy(), path);
							}
							
							// ((CollectionAttribute) displayedAttr).add(newAttr);
						} catch (InstanceCreationException ice) {
							try {
								Attribute newAttr = (Attribute) InstanceLoader
													.createInstance("org.graffiti.attributes."
																		+ typeName, attrName);
								
								String path = (displayedAttr.getPath() + " ")
													.substring(1).trim();
								
								for (Iterator geit = graphElements.iterator(); geit
													.hasNext();) {
									Attributable atbl = (Attributable) geit.next();
									atbl.addAttribute((Attribute) newAttr.copy(), path);
								}
								
								// ((CollectionAttribute) displayedAttr).add(newAttr);
							} catch (InstanceCreationException ice2) {
								JOptionPane.showMessageDialog(DefaultEditPanel.this,
													"Could not instantiate class: " + ice2, "Error!",
													JOptionPane.OK_OPTION);
							}
						}
					} else {
						try {
							Attribute newAttr;
							if (typeName.equals(StringAttribute.class.getName())) {
								newAttr = StringAttribute
													.getTypedStringAttribute(attrName);
							} else {
								newAttr = (Attribute) InstanceLoader.createInstance(
													typeName, attrName);
							}
							
							String path = (displayedAttr.getPath() + " ").substring(1)
												.trim();
							
							for (Iterator geit = graphElements.iterator(); geit
												.hasNext();) {
								Attributable atbl = (Attributable) geit.next();
								atbl.addAttribute((Attribute) newAttr.copy(), path);
							}
							
							// ((CollectionAttribute) displayedAttr).add(newAttr);
						} catch (InstanceCreationException ice) {
							JOptionPane
												.showMessageDialog(
																	DefaultEditPanel.this,
																	"Attribute could not be created. Check Attribute-Type.",
																	"Error", JOptionPane.OK_OPTION);
						}
					}
					
					graph.getListenerManager().transactionFinished(this);
				}
			}
		}
		
		/**
		 * DOCUMENT ME!
		 * 
		 * @author $Author: klukas $
		 * @version $Revision: 1.2 $ $Date: 2013-01-26 18:02:16 $
		 */
		private class AttributeSelector extends JDialog {
			private static final long serialVersionUID = 1L;
			
			/** DOCUMENT ME! */
			private final JButton cancelButton;
			
			/** DOCUMENT ME! */
			private final JButton okButton;
			
			/** DOCUMENT ME! */
			// private JButton searchButton;
			/** DOCUMENT ME! */
			JComboBox attrComboBox;
			
			/** DOCUMENT ME! */
			private final JLabel selectACNText;
			
			/** DOCUMENT ME! */
			private final JLabel selectACNText2;
			
			/** DOCUMENT ME! */
			private final JLabel selectALNText;
			
			/** DOCUMENT ME! */
			private final JPanel buttons;
			
			/** DOCUMENT ME! */
			JTextField labelTextField;
			
			/** DOCUMENT ME! */
			String attrClassname;
			
			/** DOCUMENT ME! */
			String attrLabel;
			
			/**
			 * Creates a new AttributeSelector object.
			 * 
			 * @param frame
			 *           DOCUMENT ME!
			 * @param parentAttrName
			 *           DOCUMENT ME!
			 */
			public AttributeSelector(Frame frame, String parentAttrName) {
				super(frame, "Attribute creation", true);
				
				selectALNText = new JLabel(
									"<html>Please enter a <i>name (label)</i> for the new "
														+ "attribute:</html>");
				
				labelTextField = new JTextField();
				
				selectACNText = new JLabel(
									"<html>Please enter or select an <i>attribute class "
														+ "name</i> (type of the attribute, e.g. Double, Boolean, String).");
				String labelText2;
				if (parentAttrName != null && parentAttrName.length() > 0)
					labelText2 = "<html>It will be added to \"<i>" + parentAttrName
										+ "</i>\":";
				else
					labelText2 = "";
				selectACNText2 = new JLabel(labelText2);
				
				attrComboBox = new JComboBox(getAttributeList());
				attrComboBox.setEditable(true);
				
				okButton = new JButton("OK");
				okButton.setMnemonic(1);
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						attrLabel = labelTextField.getText();
						attrClassname = attrComboBox.getSelectedItem().toString();
						
						setVisible(false);
					}
				});
				
				cancelButton = new JButton("Cancel");
				cancelButton.setMnemonic(1);
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						// TODO: search if classname is correct
						attrLabel = "";
						attrClassname = "";
						
						// EditPanel.this.setVisible(false);
						setVisible(false);
					}
				});
				
				buttons = new JPanel();
				
				JPanel ocButtons = new JPanel();
				ocButtons.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 5));
				ocButtons.add(okButton);
				ocButtons.add(cancelButton);
				buttons.setLayout(new BorderLayout());
				// buttons.add(searchButton, BorderLayout.NORTH);
				buttons.add(ocButtons, BorderLayout.SOUTH);
				
				JPanel labelPanel = new JPanel();
				labelPanel.setLayout(new BorderLayout());
				labelPanel.add(selectALNText, BorderLayout.NORTH);
				labelPanel.add(labelTextField, BorderLayout.SOUTH);
				
				JPanel classPanel = new JPanel();
				classPanel.setLayout(new BorderLayout());
				classPanel.add(selectACNText, BorderLayout.NORTH);
				classPanel.add(selectACNText2, BorderLayout.CENTER);
				classPanel.add(attrComboBox, BorderLayout.SOUTH);
				
				getRootPane().setDefaultButton(okButton);
				getContentPane().setLayout(new BorderLayout());
				getContentPane().add(labelPanel, BorderLayout.NORTH);
				getContentPane().add(classPanel, BorderLayout.CENTER);
				getContentPane().add(buttons, BorderLayout.SOUTH);
				
				pack();
				setLocationRelativeTo(DefaultEditPanel.this);
				setVisible(true);
			}
			
			private String[] getAttributeList() {
				List r = PluginHelper.getAvailableAttributes();
				String[] result = new String[r.size()];
				int i = 0;
				for (Iterator it = r.iterator(); it.hasNext();) {
					Class c = (Class) it.next();
					result[i++] = c.getName();
				}
				return result;
			}
			
			/**
			 * DOCUMENT ME!
			 * 
			 * @return DOCUMENT ME!
			 */
			public String getAttributeClassname() {
				return attrClassname;
			}
			
			/**
			 * DOCUMENT ME!
			 * 
			 * @return DOCUMENT ME!
			 */
			public String getAttributeLabel() {
				return attrLabel;
			}
		}
	}
	
	/**
	 * DOCUMENT ME!
	 * 
	 * @author $Author: klukas $
	 * @version $Revision: 1.2 $ $Date: 2013-01-26 18:02:16 $
	 */
	@SuppressWarnings("unused")
	private class RemoveListener implements ActionListener {
		/**
		 * DOCUMENT ME!
		 * 
		 * @param e
		 *           DOCUMENT ME!
		 * @throws RuntimeException
		 *            DOCUMENT ME!
		 */
		public void actionPerformed(ActionEvent e) {
			if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(
								DefaultEditPanel.this, "Attribute that will be removed: \""
													+ displayedAttr.getPath() + "\"", "Remove an attribute",
								JOptionPane.YES_NO_OPTION)) {
				Attributable attributable = displayedAttr.getAttributable();
				Graph graph = null;
				
				if (!(attributable instanceof Graph)) {
					graph = ((GraphElement) attributable).getGraph();
				} else {
					graph = (Graph) attributable;
				}
				
				graph.getListenerManager().transactionStarted(this);
				
				try {
					String attrPath = (displayedAttr.getPath() + " ").substring(1)
										.trim();
					
					for (Iterator geit = graphElements.iterator(); geit.hasNext();) {
						Attributable atbl = (Attributable) geit.next();
						atbl.removeAttribute(attrPath);
					}
					
					// displayedAttr.getParent().remove(displayedAttr);
				} catch (AttributeNotFoundException anfe) {
					throw new RuntimeException("Impossible:" + anfe);
				} catch (NullPointerException nully) {
					JOptionPane.showMessageDialog(DefaultEditPanel.this,
										"Can't remove root attribute!", "Error!",
										JOptionPane.OK_OPTION);
					
					return;
				} finally {
					graph.getListenerManager().transactionFinished(this);
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.graffiti.plugin.inspector.EditPanel#showEmpty()
	 */
	@Override
	public void showEmpty() {
		this.displayedAttr = null;
		this.displayedValueEditComponents = null;
		this.editComponentsMap = null;
		this.graphElements = null;
		this.mapValueEditComponent2AttributeList = null;
		
		mapValueEditComponent2AttributeList = new HashMap<ValueEditComponent, Collection<Displayable>>();
		displayedValueEditComponents = new LinkedList<ValueEditComponent>();
		
		attributeScrollPanel.setViewportView(new JPanel());
	}
	
	public void setShowCompleteRedrawCommand(boolean showCompleteRedrawCommand) {
		this.showCompleteRedrawCommand = showCompleteRedrawCommand;
	}
}

// ------------------------------------------------------------------------------
// end of file
// ------------------------------------------------------------------------------
