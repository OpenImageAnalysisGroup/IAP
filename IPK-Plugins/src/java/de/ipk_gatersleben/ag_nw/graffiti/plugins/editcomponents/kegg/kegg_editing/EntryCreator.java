/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_editing;

import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.AttributeHelper;
import org.FolderPanel;
import org.graffiti.editor.MainFrame;
import org.graffiti.graph.Graph;
import org.graffiti.graph.Node;
import org.graffiti.selection.Selection;
import org.graffiti.session.EditorSession;
import org.jdom.Document;

import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.kegg.kegg_type.pathway_kegg_operation.PathwayKeggLoading;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Entry;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Gml2PathwayErrorInformation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Gml2PathwayWarningInformation;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Graphics;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.KeggGmlHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.Pathway;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.EntryType;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.Id;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.IdRef;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.KeggId;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.Url;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.kgml.datatypes.kgmlGraphicsType;
import de.ipk_gatersleben.ag_nw.graffiti.services.GUIhelper;

public class EntryCreator {
	
	public static void processNewOrExistingNode(Node n, String optKeggId) {
		String kid = KeggGmlHelper.getKeggId(n);
		String gti = KeggGmlHelper.getKeggGraphicsTitle(n);
		boolean isMapNode = (kid != null && kid.startsWith("path:"));
		boolean isMapTitleNode = (kid != null && kid.startsWith("path:") && gti != null && (gti.startsWith("TITLE:") || gti.startsWith("<html>TITLE:")));
		final Graph graph = n.getGraph();
		if (isMapNode && !isMapTitleNode) {
			JLabel desc = new JLabel("<html>" +
								"This graph node represents a map link.<br>" +
								"You may either edit the entries, related to this graph node,<br>" +
								"or load this pathway into the editor window.");
			desc.setBackground(null);
			desc.setOpaque(false);
			int res = GUIhelper.showMessageDialog(
								desc,
								"Load Pathway Map or Edit Entries",
								new String[] { "Load Pathway", "Edit/Add (hidden) Entries", "Cancel" });
			if (res == 2 || res < 0)
				return;
			if (res == 0) {
				// graph.getListenerManager().transactionStarted(graph);
				try {
					PathwayKeggLoading.loadAndMergePathway(graph, kid, n, false);
				} finally {
					// graph.getListenerManager().transactionFinished(graph);
				}
				return;
			}
		}
		if (isMapTitleNode) {
			JLabel desc = new JLabel("<html>" +
								"This graph node represents a map title entry.<br>" +
								"You may either edit the Entries, related to this graph node,<br>" +
								"or collapse all graph elements of this pathway.<br>" +
								"A map link entry will then be created representing this pathway.");
			desc.setBackground(null);
			desc.setOpaque(false);
			int res = GUIhelper.showMessageDialog(
								desc,
								"Collapse Pathway Map or Edit Entries",
								new String[] { "Collapse Pathway", "Edit/Add (hidden) Entries", "Cancel" });
			if (res == 2 || res < 0)
				return;
			if (res == 0) {
				try {
					// graph.getListenerManager().transactionStarted(MainFrame.getInstance());
					PathwayKeggLoading.collapsePathway(graph, kid, n);
				} finally {
					// graph.getListenerManager().transactionFinished(MainFrame.getInstance());
				}
				return;
			}
		}
		Collection<Gml2PathwayWarningInformation> warnings = new ArrayList<Gml2PathwayWarningInformation>();
		Collection<Gml2PathwayErrorInformation> errors = new ArrayList<Gml2PathwayErrorInformation>();
		HashMap<Entry, Node> entry2graphNode = new HashMap<Entry, Node>();
		Pathway p = Pathway.getPathwayFromGraph(graph, warnings, errors, entry2graphNode);
		if (errors.size() > 0) {
			for (Gml2PathwayWarningInformation w : warnings) {
				w.printMessageToConsole();
			}
			for (Gml2PathwayErrorInformation e : errors) {
				e.printMessageToConsole();
			}
			
			MainFrame.showMessageDialog(
								"<html>" +
													"The current graph can not be error-free interpreted<br>" +
													"as an KEGG Pathway!<br><br>" + errors.size() + " errors have been found<br>" +
													"during graph analysis.<br><br>" +
													"Please choose <b>Cancel</b> in the following edit dialog,<br>" +
													"in order to leave the current graph unmodified.<br>" +
													"Then select the Pathway Editing side panel and locate and fix<br>" +
													"the problematic network elements.<br><br>" +
													"If you want to save the current work without loosing any network<br>" +
													"elements, select and use the <b>GML</b> format available with the " +
													"<b>Save As...</b> command.<br><br>" +
													"In case you select <b>OK</b> in the following edit dialog,<br>" +
													"only the network elements, which are error-free converted<br>" +
													"into a KEGG Pathway model will be conserved.", "Error");
		}
		
		ArrayList<Entry> validEntries = new ArrayList<Entry>();
		for (Entry e : entry2graphNode.keySet()) {
			if (optKeggId != null && optKeggId.length() > 0) {
				if (entry2graphNode.get(e) == n && e.getName().getId().equals(optKeggId))
					validEntries.add(e);
			} else
				if (entry2graphNode.get(e) == n)
					validEntries.add(e);
		}
		if (validEntries.size() <= 0) {
			MainFrame.showMessageDialog(
								"<html>" +
													"While interpreting the current graph as a KGML Pathway,<br>" +
													"no Entry in the resulting KGML Pathway is represented by or<br>" +
													"related to the selected graph node. Therefore the editing<br>" +
													"process may not be continued." +
													"", "Error");
			return;
		}
		
		processEntryEditOperation(n, isMapNode, graph, warnings, errors, entry2graphNode, p, validEntries);
	}
	
	public static void processEntryEditOperation(
						Node n, boolean isMapNode, final Graph graph,
						Collection<Gml2PathwayWarningInformation> warnings,
						Collection<Gml2PathwayErrorInformation> errors,
						HashMap<Entry, Node> entry2graphNode,
						Pathway p,
						ArrayList<Entry> validEntries) {
		Entry initialEntry = null;
		if (validEntries.size() > 1 || isMapNode) {
			JCheckBox addBackgroundEntry = new JCheckBox("<html>" +
								"Don't edit a existing entry - add a new entry and<br>" +
								"assign it to this map-node.", false);
			
			Object[] res = MyInputHelper
								.getInput(
													"<html>"
																		+
																		"More than one KEGG Pathway entry may be represented by the selected<br>"
																		+
																		"graph node. Please select the Entry you want to modify.<br>"
																		+
																		""
																		+
																		(isMapNode ?
																							"<br>This node represents a link to a KEGG pathway.<br>"
																												+
																												"You may add &quot;background&quot; or &quot;hidden&quot; entries to this<br>"
																												+
																												"map entry. If you wish to add and edit a new background-entry, select the corresponding<br>"
																												+
																												"checkbox. In this case the active entry selection is ignored.<br><br>"
																							: ""),
													"Select Entry",
													new Object[] {
																		"Entries", validEntries,
																		(isMapNode ? "Add Background-Entry" : null),
																		(isMapNode ? addBackgroundEntry : null)
								});
			if (res == null)
				return;
			if (isMapNode && addBackgroundEntry.isSelected()) {
				IdRef mapRef = null;
				for (Entry e : entry2graphNode.keySet()) {
					if (entry2graphNode.get(e) == n && e.getType() == EntryType.map)
						mapRef = new IdRef(e, e.getId().getValue());
				}
				Entry ne = new Entry(
									new Id(),
									new KeggId("unspecified"),
									EntryType.unspecified,
									new Url(""),
									mapRef,
									new ArrayList<KeggId>(), new ArrayList<IdRef>(), null);
				p.getEntries().add(ne);
				initialEntry = ne;
				validEntries.add(ne);
			} else
				initialEntry = (Entry) res[0];
		} else
			initialEntry = validEntries.get(0);
		
		if (initialEntry == null)
			return;
		
		boolean showDelCmd = false;
		if (isMapNode && (initialEntry.getType() != EntryType.map))
			showDelCmd = true;
		
		// //////////
		final JTextField searchInputField = new JTextField();
		final JComboBox entryTypeSelection = new JComboBox();
		final JTextField idInputField = new JTextField();
		final JTextField reference = new JTextField();
		final JTextField labelInputField = new JTextField();
		
		JCheckBox jCheckBoxDeleteEntry = new JCheckBox("Delete Background-Entry");
		jCheckBoxDeleteEntry.setEnabled(showDelCmd);
		
		String initId = initialEntry.getName().getId();
		if (initId != null)
			idInputField.setText(initId);
		String initTitle = null;
		if (initialEntry.getGraphics() != null)
			initTitle = initialEntry.getGraphics().getName();
		else
			labelInputField.setEnabled(false);
		if (initTitle != null)
			labelInputField.setText(initTitle);
		String initUrl = null;
		if (initialEntry.getLink() != null)
			initUrl = initialEntry.getLink().toString();
		if (initUrl != null)
			reference.setText(initUrl);
		Color currentBgColor = null;
		Color currentFgColor = null;
		if (initialEntry.getGraphics() != null) {
			currentBgColor = initialEntry.getGraphics().getBGcolor();
			currentFgColor = initialEntry.getGraphics().getFGcolor();
		}
		Boolean initIsPartOfGroup = initialEntry.isPartOfGroup();
		if (initialEntry.getGraphics() == null) {
			initIsPartOfGroup = null;
		}
		String initKeggType = initialEntry.getType().toString();
		
		JButton dbgetLookup = new JButton("Look-up");
		dbgetLookup.setOpaque(false);
		final JLabel searchUrl = new JLabel(getSearchUrl(EntryType.unspecified, "", true));
		searchUrl.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		searchUrl.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent arg0) {
				String searchText = searchInputField.getText();
				EntryType et = EntryType.getEntryType((String) entryTypeSelection.getSelectedItem());
				String url = getSearchUrl(et, searchText, false);
				AttributeHelper.showInBrowser(url);
			}
			
			public void mouseEntered(MouseEvent arg0) {
			}
			
			public void mouseExited(MouseEvent arg0) {
			}
			
			public void mousePressed(MouseEvent arg0) {
			}
			
			public void mouseReleased(MouseEvent arg0) {
			}
		});
		dbgetLookup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String searchText = searchInputField.getText();
				EntryType et = EntryType.getEntryType((String) entryTypeSelection.getSelectedItem());
				String url = getSearchUrl(et, searchText, false);
				// AttributeHelper.showInBrowser(url);
				ArrayList<String> lines = AttributeHelper.getWebPageContent(url);
				interpreteWebsite(lines, entryTypeSelection, idInputField,
									reference, labelInputField);
			}
		});
		
		JComponent searchPane =
							TableLayout.getSplitVertical(
												TableLayout.getSplit(
																	searchInputField, dbgetLookup,
																	TableLayout.FILL, TableLayout.PREFERRED),
												searchUrl,
												TableLayout.PREFERRED, TableLayout.PREFERRED);
		searchPane.setOpaque(true);
		searchInputField.setOpaque(true);
		searchInputField.setBackground(new Color(240, 240, 240));
		searchPane.setBackground(new Color(240, 240, 240));
		final ActionListener al1 = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						String searchText = searchInputField.getText();
						EntryType et = EntryType.getEntryType((String) entryTypeSelection.getSelectedItem());
						searchUrl.setText(getSearchUrl(et, searchText, true));
						FolderPanel.performDialogResize(entryTypeSelection);
					}
				});
			}
		};
		entryTypeSelection.addActionListener(al1);
		searchInputField.addActionListener(al1);
		searchInputField.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {
			}
			
			public void keyReleased(KeyEvent arg0) {
			}
			
			public void keyTyped(final KeyEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						al1.actionPerformed(null);
					}
				});
			}
		});
		
		// //////////
		
		for (EntryType et : EntryType.values()) {
			entryTypeSelection.addItem(et.getDescription());
		}
		if (initKeggType == null || initKeggType.length() <= 0)
			entryTypeSelection.setSelectedItem(EntryType.unspecified.getDescription());
		else
			entryTypeSelection.setSelectedItem(initKeggType);
		entryTypeSelection.setOpaque(false);
		
		// //////////
		int ph = reference.getPreferredSize().height;
		reference.setPreferredSize(new Dimension(150, ph));
		JButton visit = new JButton("Open URL");
		visit.setOpaque(false);
		JComponent urlPane = TableLayout.getSplit(
							reference, visit,
							TableLayout.FILL, TableLayout.PREFERRED);
		
		//
		dbgetLookup.addActionListener(
							getDBGETactionListener(
												searchInputField, dbgetLookup,
												idInputField, entryTypeSelection,
												reference, labelInputField
							));
		
		JLabel statusLabel = getErrorStatusLabel(errors, warnings);
		
		Object[] res = MyInputHelper.getInput(
							"Please fill in/modify the entry properties.<br>" +
												"You may use the search field to locate a entry based on a<br>" +
												"known ID or name, which is listed in the KEGG DBGET system.<br><br>" +
												"<small><font color='gray'>" +
												"To evaluate the search results manually, click onto the gray link-text beneath the<br>" +
												"search input field. Consider to limit the search scope with the Entry-Type drop-down list.<br>" +
												"Hint: You can look for more than one term. E.g. in order to search the TCA reference map,<br>" +
												"use this search term: &quot;TCA map&quot;.<br><br>",
							"Modify Entry",
							new Object[] {
												"<html><font color='#444444'>Search Item<br>" +
																	"<small><font color='gray'>(DBGET Web-Request)", searchPane,
												"Type", entryTypeSelection,
												"Label", initialEntry.getGraphics() != null ? labelInputField : null,
												"ID", idInputField,
												"Reference URL", urlPane,
												"BG Color", currentBgColor,
												"FG Color", currentFgColor,
												"Part of Group", initIsPartOfGroup,
												"Set Size", currentBgColor != null ? new Boolean(true) : null,
												"Information", statusLabel,
												(showDelCmd ? "Don't edit - delete?" : null), (showDelCmd ? jCheckBoxDeleteEntry : null)
					});
		if (res != null) {
			
			if (showDelCmd && jCheckBoxDeleteEntry.isSelected()) {
				p.getEntries().remove(initialEntry);
			} else {
				Graphics g = initialEntry.getGraphics();
				Color bgColor = (Color) res[5];
				Color fgColor = (Color) res[6];
				Boolean isPartOfGroup = (Boolean) res[7];
				Boolean autoResize = (Boolean) res[8];
				String label = labelInputField.getText();
				if (isPartOfGroup != null)
					initialEntry.setIsPartOfGroup(isPartOfGroup);
				if (g != null) {
					g.setBGcolor(bgColor);
					g.setFGcolor(fgColor);
					g.setName(label);
				}
				String id = idInputField.getText();
				initialEntry.getName().setId(id);
				
				EntryType et = EntryType.getEntryType((String) entryTypeSelection.getSelectedItem());
				initialEntry.setType(et);
				
				String url = reference.getText();
				initialEntry.setLink(url);
				
				if (et == EntryType.compound) {
					if (g != null) {
						g.setGraphicsType(kgmlGraphicsType.circle);
						if (autoResize) {
							g.setWidth(8);
							g.setHeight(8);
						}
					}
				} else {
					if (g != null) {
						if (et == EntryType.map)
							g.setGraphicsType(kgmlGraphicsType.roundrectangle);
						else
							g.setGraphicsType(kgmlGraphicsType.rectangle);
						JLabel dummy = new JLabel(label);
						dummy.setFont(new Font(dummy.getFont().getName(), dummy.getFont().getStyle(), 10));
						Dimension d = dummy.getPreferredSize();
						if (autoResize) {
							g.setWidth(d.width + 6);
							g.setHeight(d.height + 4);
						}
					}
				}
			}
			// entry updated
			// update graph
			Document d = p.getKgmlDocument();
			Pathway p2 = Pathway.getPathwayFromKGML(d.getRootElement());
			graph.clear();
			// graph.deleteAll(graph.getGraphElements());
			p2.getGraph(graph);
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					EditorSession es = MainFrame.getInstance().getActiveEditorSession();
					if (es == null || es.getGraph() != graph)
						return;
					Selection selection = es.getSelectionModel().getActiveSelection();
					selection.clear();
					MainFrame.getInstance().getActiveEditorSession().getSelectionModel().selectionChanged();
				}
			});
		}
	}
	
	public static JLabel getErrorStatusLabel(
						final Collection<Gml2PathwayErrorInformation> errors,
						final Collection<Gml2PathwayWarningInformation> warnings) {
		JLabel statusLabel = new JLabel("<html><font color='" + (errors.size() > 0 ? "red" : "gray") + "'><small><br>" +
							"Initial interpretation of graph network as KGML data model produced<br>" +
							errors.size() + " errors and " +
							warnings.size() + " warnings.<br><br>");
		statusLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		statusLabel.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				Pathway.showKgmlErrors(errors, warnings);
			}
			
			public void mouseEntered(MouseEvent e) {
			}
			
			public void mouseExited(MouseEvent e) {
			}
			
			public void mousePressed(MouseEvent e) {
			}
			
			public void mouseReleased(MouseEvent e) {
			}
		});
		return statusLabel;
	}
	
	protected static void interpreteWebsite(
						ArrayList<String> lines,
						JComboBox entryTypeSelection,
						JTextField idInputField,
						JTextField reference,
						JTextField labelInputField) {
		if (lines == null || lines.size() <= 0) {
			MainFrame.showMessageDialog(
								"<html>" +
													"DBGET request did not return data.<br>" +
													"Please check the error-log (Help menu) for more details.", "Error");
		} else {
			ArrayList<DBGETresult> result = new ArrayList<DBGETresult>();
			StringBuilder content = new StringBuilder();
			for (String line : lines) {
				content.append(line);
			}
			String line = content.toString();
			content = null;
			while (line.indexOf("<a href=\"/dbget-bin/www_bget?") >= 0) {
				String url = DBGETresult.getContentBetween("href=\"", "\">", line);
				String memUrl = "";
				String memId = "";
				String memNames = "";
				if (url != null && url.length() > 0) {
					url = "http://www.genome.jp" + url;
					memUrl = url;
				}
				line = line.substring(line.indexOf("<a href=\"/dbget-bin/www_bget?"));
				String id = DBGETresult.getContentBetween(">", "</a>", line);
				if (id != null && id.length() > 0)
					memId = id.trim();
				String names = DBGETresult.getContentBetween("</a><br><div style=\"margin-left:2em\">", "</div>", line);
				if (names != null && names.length() > 0)
					memNames = names.trim();
				result.add(new DBGETresult(memId, memNames, memUrl));
				line = line.substring(line.indexOf("</div>"));
			}
			DBGETresult selection = null;
			if (result.size() > 1) {
				if (result.size() >= 100) {
					MainFrame.showMessageDialog(
										"<html>" +
															"DBGET request did return the maximum specified amount of data.<br>" +
															"Only the first 100 result entries are shown.<br>" +
															"Please consider to limit the search scope by defining the correct<br>" +
															"Entry-Type. If the result-set is still to large, please use the<br>" +
															"DBGET search website directly, in order to locate the desired entity.",
										"Result-Set too large");
				}
				Object[] res = MyInputHelper.getInput(
									"<html>" +
														"Please select the desired entity.<br>" +
														"Don't forget to check the Entry-Type afterwards.<br>" +
														"If too many items are in the list, try to provide a more specific search term.<br>" +
														"Also consider to limit the scope of the DBGET search by selecting the desired<br>" +
														"Entry-Type before the DBGET search is performed.<br>" +
														"In case of doubt, consult the KEGG DBGET Webpage for detailed information about<br>" +
														"the available entities available in these databases.",
									"Select a Entity",
									new Object[] {
														"Entitity Selection", result
						});
				if (res != null)
					selection = (DBGETresult) res[0];
			} else
				if (result.size() <= 0)
					MainFrame.showMessageDialog("<html><h3>No result</h3>" +
										"No entries found by parsing the web-request result.<hr>" +
										"Please click onto the URL beneath the search input field<br>" +
										"to visit the KEGG DBGET websearch site to manually<br>" +
										"look for the desired information or to check<br>" +
										"search results.", "No result");
			if (result.size() == 1)
				selection = result.get(0);
			if (selection != null) {
				idInputField.setText(selection.getId());
				reference.setText(selection.getUrl());
				labelInputField.setText(selection.getFirstName());
				EntryType et = EntryType.getEntryType((String) entryTypeSelection.getSelectedItem());
				if (et == null || et == EntryType.unspecified || et == EntryType.undefined) {
					if (selection.getEntryTypeFromId() != null)
						entryTypeSelection.setSelectedItem(selection.getEntryTypeFromId().getDescription());
				}
			}
		}
	}
	
	private static ActionListener getDBGETactionListener(
						final JTextField searchInputField,
						final JButton dbgetLookup,
						final JTextField idInputField,
						final JComboBox entryTypeSelection,
						final JTextField reference,
						final JTextField labelInputField) {
		ActionListener result = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String searchText = searchInputField.getText();
				EntryType et = EntryType.getEntryType((String) entryTypeSelection.getSelectedItem());
				getSearchUrl(et, searchText, false);
			}
		};
		return result;
	}
	
	private static String getSearchUrl(EntryType et, String text, boolean htmlPrettyPrint) {
		String db = "";
		switch (et) {
			case enzyme:
				db = "ec";
				break;
			case compound:
				db = "cpd";
				break;
			case ortholog:
				db = "ko";
				break;
			case map:
				db = "path";
				break;
			default:
				db = "kegg";
				break;
		}
		return (htmlPrettyPrint ? "<html><font color='gray'><small>search URL: " : "") +
							"http://www.genome.jp/dbget-bin/www_bfind_sub?"
							+ (htmlPrettyPrint ? "<br>" : "") +
							"mode=bfind&max_hit=100&dbkey=" + db + "&keywords=" + text;
	}
	
	public static void checkGraphAttributes(Graph graph) {
		String name = KeggGmlHelper.getKeggId(graph);
		String org = KeggGmlHelper.getKeggOrg(graph);
		String number = KeggGmlHelper.getKeggMapNumber(graph);
		String title = KeggGmlHelper.getKeggTitle(graph);
		String image = KeggGmlHelper.getKeggImageUrl(graph);
		String link = KeggGmlHelper.getKeggLinkUrl(graph);
		
		if (name == null || name.length() <= 0)
			name = "unspecified";
		if (org == null || org.length() <= 0)
			org = "map";
		if (number == null || number.length() <= 0)
			number = "00000";
		if (title == null || title.length() <= 0)
			title = "unspecified";
		if (image == null)
			image = "";
		if (link == null)
			link = "";
		
		KeggGmlHelper.setKeggId(graph, name);
		KeggGmlHelper.setKeggOrg(graph, org);
		KeggGmlHelper.setKeggMapNumber(graph, number);
		if (title != null)
			KeggGmlHelper.setKeggTitle(graph, title);
		if (image != null)
			KeggGmlHelper.setKeggImageUrl(graph, image.toString());
		if (link != null)
			KeggGmlHelper.setKeggLinkUrl(graph, link.toString());
	}
	
}
