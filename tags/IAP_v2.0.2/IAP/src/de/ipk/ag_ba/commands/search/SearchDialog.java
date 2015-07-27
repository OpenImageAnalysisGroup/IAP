/*
 * Created on 02.10.2013 by Christian Klukas
 */
package de.ipk.ag_ba.commands.search;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.ErrorMsg;
import org.FolderPanel;
import org.GuiRow;
import org.ReleaseInfo;
import org.graffiti.editor.GravistoService;
import org.graffiti.editor.MainFrame;
import org.graffiti.editor.MessageType;
import org.graffiti.graph.Edge;
import org.graffiti.graph.GraphElement;
import org.graffiti.graph.Node;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;

import de.ipk_gatersleben.ag_nw.graffiti.GraphHelper;
import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.DefaultContextMenuManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.webstart.TextFile;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.AttributePathNameSearchType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.LogicConnection;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.NodeOrEdge;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchOperation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchOption;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.invert_selection.SearchOptionEditorGUI;

/**
 * Search for experiments based on experiment header annotation or condition meta data.
 * 
 * @author Christian Klukas
 */
public class SearchDialog extends JDialog {
	private static final long serialVersionUID = 1L;
	
	private final Collection<AttributePathNameSearchType> possibleAttributes;
	
	public SearchDialog(Frame owner, Collection<AttributePathNameSearchType> possibleAttributes) {
		super(owner);
		this.possibleAttributes = possibleAttributes;
		myDialogInit();
		
		getRootPane().getActionMap().put("escapeAction", new AbstractAction() {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent event) {
				SearchDialog.this.dispose();
			}
		});
		getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "escapeAction");
		
	}
	
	protected void myDialogInit() {
		super.dialogInit();
		setResizable(false);
		setAlwaysOnTop(true);
		setTitle("Search experiments");
		JLabel helpText = new JLabel("");
		String okDesc;
		okDesc = "<html>Add to result<br>collection";
		
		final FolderPanel replaceOption = new FolderPanel(
				"Search (and replace) the following text...",
				false, false, false, null);
		
		final JTextField searchText = new JTextField();
		final JTextField replaceText = new JTextField();
		final JCheckBox doRegularExpr = new JCheckBox("Use regular expressions", false);
		final JCheckBox setLabel = new JCheckBox("Set label", false);
		doRegularExpr.setOpaque(false);
		replaceOption.addGuiComponentRow(new JLabel("<html>Find&nbsp;&nbsp;&nbsp;"), searchText, false);
		replaceOption.addGuiComponentRow(new JLabel("<html>Replace With&nbsp;&nbsp;&nbsp;"), replaceText, false);
		replaceOption.addGuiComponentRow(new JLabel(), doRegularExpr, false);
		
		replaceOption.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
		replaceOption.layoutRows();
		
		JComponent replaceTextPanel = TableLayout.getSplitVertical(new JLabel(), replaceOption, TableLayout.PREFERRED, TableLayoutConstants.PREFERRED);
		
		final JButton searchButton = new JButton("Search Text");
		final JButton okButton = new JButton(okDesc);
		String description = "Modify search attributes...";
		final FolderPanel options = new FolderPanel(
				description,
				false, false, false,
				null);
		
		JButton removeFromSelButton = new JButton("<html>Remove from<br>result collection");
		removeFromSelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					ArrayList<SearchOption> searchOptions = new ArrayList<SearchOption>();
					for (GuiRow gr : options.getVisibleGuiRows()) {
						if (gr.left instanceof SearchOptionEditorGUI)
							searchOptions.add(((SearchOptionEditorGUI) gr.left).getSearchOption());
						if (gr.right instanceof SearchOptionEditorGUI)
							searchOptions.add(((SearchOptionEditorGUI) gr.right).getSearchOption());
					}
					doSearch(searchOptions.toArray(new SearchOption[] {}), false);
				} catch (Exception err) {
					ErrorMsg.addErrorMessage(err);
				}
			}
		});
		
		okButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					ArrayList<SearchOption> searchOptions = new ArrayList<SearchOption>();
					for (GuiRow gr : options.getVisibleGuiRows()) {
						if (gr.left instanceof SearchOptionEditorGUI)
							searchOptions.add(((SearchOptionEditorGUI) gr.left).getSearchOption());
						if (gr.right instanceof SearchOptionEditorGUI)
							searchOptions.add(((SearchOptionEditorGUI) gr.right).getSearchOption());
					}
					// complex search dialog
					doSearch(searchOptions.toArray(new SearchOption[] {}));
				} catch (Exception err) {
					ErrorMsg.addErrorMessage(err);
				}
			}
			
		});
		
		searchButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					// find text
					ArrayList<SearchOption> searchOptions = new ArrayList<SearchOption>();
					for (GuiRow gr : options.getVisibleGuiRows()) {
						if (gr.left instanceof SearchOptionEditorGUI)
							searchOptions.add(((SearchOptionEditorGUI) gr.left).getSearchOption());
						if (gr.right instanceof SearchOptionEditorGUI)
							searchOptions.add(((SearchOptionEditorGUI) gr.right).getSearchOption());
					}
					SearchOption so = searchOptions.get(0);
					String path = so.getSearchAttributePath();
					String id = so.getSearchAttributeName();
					
					MainFrame.getInstance().getActiveEditorSession().getSelectionModel().setActiveSelection(new Selection("empty"));
					MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
					
					ArrayList<GraphElement> foundElements = new ArrayList<GraphElement>();
					
					String findtext = searchText.getText();
					if (findtext != null && findtext.length() >= 0) {
						if (so.getSearchNodeOrEdge() == NodeOrEdge.Nodes || so.getSearchNodeOrEdge() == NodeOrEdge.NodesAndEdges)
							foundElements.addAll(searchNodeText(findtext, path, id));
						if (so.getSearchNodeOrEdge() == NodeOrEdge.Edges || so.getSearchNodeOrEdge() == NodeOrEdge.NodesAndEdges)
							foundElements.addAll(searchEdgeText(findtext, path, id));
						
						MainFrame.getInstance().getActiveEditorSession().getSelectionModel().setActiveSelection(
								new Selection("search " + searchText.getText(), foundElements));
						MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
						
						MainFrame.showMessage(foundElements.size() + " nodes and edges selected", MessageType.INFO);
						
					}
				} catch (Exception err) {
					ErrorMsg.addErrorMessage(err);
				}
			}
		});
		final JButton addRowButton = new JButton("+ add search option");
		addRowButton.setOpaque(false);
		addRowButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JButton removeThisRowButton = new JButton("- remove");
				removeThisRowButton.setOpaque(false);
				SearchOption so = new SearchOption();
				JComponent soGUI = so.getSearchOptionEditorGUI(possibleAttributes, true);
				final GuiRow guiRow = new GuiRow(soGUI, removeThisRowButton);
				removeThisRowButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						options.removeGuiComponentRow(guiRow, true);
						pack();
					}
				});
				options.addGuiComponentRow(guiRow, true);
				pack();
			}
		});
		options.setBorder(BorderFactory.createEmptyBorder(5, 5, 10, 5));
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				SearchOption so = new SearchOption();
				JComponent soGUI = so.getSearchOptionEditorGUI(possibleAttributes, false);
				GuiRow guiRow = new GuiRow(soGUI, null);
				options.addGuiComponentRow(guiRow, true);
				pack();
			}
		});
		
		options.layoutRows();
		JButton cancelButton = new JButton("Close");
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
			}
		});
		
		JButton clearSelectionButton = new JButton("Clear selection");
		clearSelectionButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				EditorSession session =
						GravistoService
								.getInstance()
								.getMainFrame()
								.getActiveEditorSession();
				Selection selection = session.getSelectionModel().getActiveSelection();
				selection.clear();
				session.getSelectionModel().selectionChanged();
				MainFrame.showMessage("Graph-Elements unselected", MessageType.INFO);
			}
		});
		
		JComponent buttonPanel = TableLayout.get4Split(
				okButton, removeFromSelButton, clearSelectionButton, cancelButton,
				TableLayout.PREFERRED,
				5, 2);
		
		JButton saveButton = new JButton("<html><small>Create new<br>menu command");
		saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				ArrayList<SearchOption> searchOptions = new ArrayList<SearchOption>();
				for (GuiRow gr : options.getVisibleGuiRows()) {
					if (gr.left instanceof SearchOptionEditorGUI)
						searchOptions.add(((SearchOptionEditorGUI) gr.left).getSearchOption());
					if (gr.right instanceof SearchOptionEditorGUI)
						searchOptions.add(((SearchOptionEditorGUI) gr.right).getSearchOption());
				}
				setVisible(false);
				dispose();
				saveSearch(searchOptions);
			}
		});
		
		saveButton.setVisible(false);
		
		JComponent buttonBar = TableLayout.get3Split(buttonPanel, new JLabel(), saveButton, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED);
		
		JComponent topPanel = TableLayout.getSplitVertical(helpText, options, TableLayout.PREFERRED, TableLayout.PREFERRED);
		
		double border = 5;
		double[][] size = { { border, TableLayoutConstants.PREFERRED, border }, // Columns
				{ border, TableLayout.PREFERRED, border } }; // Rows
		setLayout(new TableLayout(size));
		add(TableLayout.get3SplitVertical(
				topPanel,
				null,
				buttonBar,
				TableLayout.PREFERRED, TableLayout.PREFERRED, TableLayout.PREFERRED),
				"1,1");
		pack();
		
		validate();
	}
	
	private List<GraphElement> searchNodeText(String find, String path, String id) {
		List<Node> nodes = GraphHelper.getSelectedOrAllNodes();
		List<GraphElement> foundnodes = new ArrayList<GraphElement>();
		if (nodes != null && nodes.size() > 0) {
			try {
				allnodes: for (Node nd : nodes) {
					String currentValue = (String) AttributeHelper.getAttributeValue(nd, path, id, null, "", false);
					if (currentValue != null && currentValue.toLowerCase().contains(find.toLowerCase())) {
						foundnodes.add(nd);
						continue allnodes;
					}
					
					// search all attributes with the same path, but ending with a different number (eg labelgraphics3 -> search also in labelgraphics1, ...)
					for (String newpath : getAlternativePaths(nd, path)) {
						currentValue = (String) AttributeHelper.getAttributeValue(nd, newpath, id, null, "", false);
						if (currentValue != null && currentValue.toLowerCase().contains(find.toLowerCase())) {
							foundnodes.add(nd);
							continue allnodes;
						}
					}
				}
				return foundnodes;
			} catch (Exception ex) {
				ErrorMsg.addErrorMessage(ex);
			}
		}
		return new ArrayList<GraphElement>();
	}
	
	public static ArrayList<String> getAlternativePaths(Node nd, String path) {
		if (path.length() <= 0)
			return new ArrayList<String>();
		
		int index = path.length() - 1;
		while (Character.isDigit(path.charAt(index)))
			index--;
		
		ArrayList<String> ap = new ArrayList<String>();
		
		if (index != path.length() - 1) {
			String shortPath = path.substring(0, index + 1);
			int actualNumber = Integer.parseInt(path.substring(index + 1));
			for (int i = 0; i < actualNumber; i++) {
				if (AttributeHelper.hasAttribute(nd, shortPath + i))
					ap.add(shortPath + i);
			}
			int i = actualNumber + 1;
			while (AttributeHelper.hasAttribute(nd, shortPath + i))
				ap.add(shortPath + (i++));
		}
		return ap;
	}
	
	private List<GraphElement> searchEdgeText(String find, String path, String id) {
		Collection<Edge> edges = GraphHelper.getSelectedOrAllEdges();
		List<GraphElement> foundedges = new ArrayList<GraphElement>();
		if (edges != null && edges.size() > 0) {
			try {
				// edges have no annotation labels or similar, so we dont want to search for alternative paths
				for (Edge ed : edges) {
					String currentValue = (String) AttributeHelper.getAttributeValue(ed, path, id, null, "", false);
					if (currentValue != null && currentValue.contains(find))
						foundedges.add(ed);
				}
				return foundedges;
			} catch (Exception ex) {
				ErrorMsg.addErrorMessage(ex);
			}
		}
		return new ArrayList<GraphElement>();
	}
	
	private void saveSearch(ArrayList<SearchOption> searchOptions) {
		Object[] input = MyInputHelper.getInput(
				"With this command a new menu item may be created,<br>" +
						"which allows to perform the search and select operation<br>" +
						"with the current search parameters quickly again.<br><br>" +
						"Please specify the (new or existing) target menu<br>" +
						"and the title of the newly created menu item.<br><br>" +
						"The default menus <b>File</b>, <b>Edit</b> and <b>Window</b> may be used<br>" +
						"as a target with the special menu titles \"menu.file\",<br>" +
						"\"menu.edit\" and \"menu.window\".<br><br>",
				"Create New Search-Command",
				new Object[] {
						"Target menu", new String("menu.edit"),
						"Command name", new String("Quick-Search 1"),
						"<html>" +
								"Add or remove results to selection?<br>" +
								"Selected = add to selection<br>" +
								"Deselected = remove from selection", new Boolean(true)
				});
		if (input != null) {
			String menu = (String) input[0];
			String title = (String) input[1];
			boolean addElementsTrue = (Boolean) input[2];
			// String menuCheck = (String) input[0];
			// String titleCheck = (String) input[1];
			boolean charOk = true;
			if (menu.length() > 0 && title.length() > 0) {
				charOk = true;
				if (containsChars(menu, "@", ":", "\\", "\"", "?", "*"))
					charOk = false;
				if (containsChars(title, "@", ":", "\\", "\"", "?", "*"))
					charOk = false;
				if (!charOk) {
					MainFrame.showMessageDialog(
							"<html>The command name or menu item contains invalid characters!<br><br>" +
									"Invalid characters: <b>@ : \\ \" ? *</b>", "Error");
					saveSearch(searchOptions);
				} else {
					TextFile tf = new TextFile();
					try {
						String fileName =
								ReleaseInfo.getAppFolderWithFinalSep() + title + ".bsh";
						System.out.println("Attempt to create " + fileName + "");
						boolean ok = false;
						if (new File(fileName).exists()) {
							if (JOptionPane.showConfirmDialog(MainFrame.getInstance(),
									"<html>Do you want to overwrite the existing file <i>" +
											fileName + "</i>?</html>", "Overwrite File?",
									JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
								ok = true;
							}
						} else
							ok = true;
						if (ok) {
							tf.add("//@" + menu + ":" + title + "");
							tf.add(SearchOption.getImportStatements() + "");
							SearchOption.getSearchScriptCommands(searchOptions, addElementsTrue, tf, "");
							tf.write(fileName);
							String tm = menu;
							if (tm.equals("menu.file"))
								tm = "File";
							if (tm.equals("menu.edit"))
								tm = "Edit";
							if (tm.equals("menu.window"))
								tm = "Window";
							MainFrame.showMessageDialog("<html>A new script command was created: <b>\"" + tm + "/" + title + "\"</b>!<br>" +
									"Delete the newly created command file \"" + fileName + "\" to remove this command.", "New command created");
						}
					} catch (IOException e) {
						ErrorMsg.addErrorMessage(e);
					}
				}
			}
		}
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				// add main menu scripts
				JMenu dummyScipt = new JMenu("Dummy Script");
				DefaultContextMenuManager.returnScriptMenu(dummyScipt);
			}
		});
	}
	
	private boolean containsChars(String check, String... invalid) {
		boolean result = false;
		for (String i : invalid) {
			if (check.indexOf(i) > 0)
				result = true;
		}
		return result;
	}
	
	public static void doSearch(final SearchOption[] searchOptions) {
		doSearch(searchOptions, true);
	}
	
	public static void doSearch(final SearchOption[] searchOptions, boolean trueAddfalseRemove) {
		final ArrayList<GraphElement> validGraphElements = new ArrayList<GraphElement>();
		final EditorSession session = GravistoService.getInstance().getMainFrame().getActiveEditorSession();
		ArrayList<GraphElement> gel = null;
		try {
			gel = new ArrayList<GraphElement>(session.getGraph().getGraphElements());
		} catch (NullPointerException npe) {
			MainFrame.showMessageDialog("No active graph editor window found!", "Error");
			return;
		}
		final ArrayList<GraphElement> graphElements = gel;
		
		final HashMap<SearchOption, HashMap<GraphElement, Integer>> searchOption2positionMemory = new HashMap<SearchOption, HashMap<GraphElement, Integer>>();
		for (SearchOption so : searchOptions) {
			if (so.getSearchOperation() == SearchOperation.topN || so.getSearchOperation() == SearchOperation.bottomN) {
				HashMap<GraphElement, Integer> ge2pos = so.getPositionsOfGraphElementsForThisSearchOption(graphElements);
				searchOption2positionMemory.put(so, ge2pos);
			}
		}
		final ArrayList<SearchOption> sortCriteria = new ArrayList<SearchOption>();
		for (GraphElement ge : graphElements) {
			boolean addToSelection = false;
			for (SearchOption so : searchOptions) {
				if (so.getSearchOperation() == SearchOperation.topN || so.getSearchOperation() == SearchOperation.bottomN)
					sortCriteria.add(so);
				int idxOfGraphElement = -1;
				if (searchOption2positionMemory.containsKey(so)) {
					HashMap<GraphElement, Integer> ge2pos = searchOption2positionMemory.get(so);
					if (ge2pos.containsKey(ge))
						idxOfGraphElement = ge2pos.get(ge);
				}
				if (addToSelection && (so.getLogicalConnection() == LogicConnection.AND))
					addToSelection = addToSelection && so.doesMatch(ge, graphElements, idxOfGraphElement);
				if (so.getLogicalConnection() == LogicConnection.OR)
					addToSelection = addToSelection || so.doesMatch(ge, graphElements, idxOfGraphElement);
			}
			if (addToSelection) {
				validGraphElements.add(ge);
			}
		}
		if (sortCriteria.size() > 0) {
			// sort selection elements...
			Collections.sort(validGraphElements, new Comparator<GraphElement>() {
				@Override
				public int compare(GraphElement ge1, GraphElement ge2) {
					for (SearchOption so : sortCriteria) {
						HashMap<GraphElement, Integer> ge2idx = searchOption2positionMemory.get(so);
						Integer a = ge2idx.get(ge1);
						Integer b = ge2idx.get(ge2);
						int r = 0;
						if (a != null && b != null)
							r = a.compareTo(b);
						if (r != 0)
							return r;
					}
					return 0;
				}
			});
		}
		session.getGraph().getListenerManager().transactionStarted(MainFrame.getInstance());
		Selection selection = session.getSelectionModel().getActiveSelection();
		int ndCnt = 0;
		int edCnt = 0;
		for (GraphElement ge : validGraphElements) {
			if (!trueAddfalseRemove && !selection.contains(ge))
				continue;
			if (trueAddfalseRemove && selection.contains(ge))
				continue;
			if (ge instanceof Node)
				ndCnt++;
			else
				edCnt++;
			if (trueAddfalseRemove) {
				selection.add(ge);
			} else {
				selection.remove(ge);
			}
		}
		session.getSelectionModel().selectionChanged();
		session.getGraph().getListenerManager().transactionFinished(MainFrame.getInstance());
		MainFrame.showMessage(ndCnt + " node(s) and " + edCnt + " edges " + (trueAddfalseRemove ? "added to" : "removed from") +
				" selection", MessageType.INFO);
	}
	
}
