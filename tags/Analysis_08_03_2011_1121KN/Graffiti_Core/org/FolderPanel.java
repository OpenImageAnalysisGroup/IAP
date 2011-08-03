/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

/*
 * Created on 13.12.2004 by Christian Klukas
 */
package org;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class FolderPanel extends JComponent {
	
	private static final long serialVersionUID = 1L;
	private String title;
	private boolean condensedState;
	private Color frameColor = new Color(195, 212, 232); // new
	// Color(120,120,160);//
	// new Color(140,
	// 140, 140);
	private Color headingColor = Color.BLACK; // Color.WHITE;
	private Color backgroundColor = Color.WHITE;
	int frameWidth = 2;
	int emptyBorderWidth = 10;
	
	int colSpacing = 0;
	int rowSpacing = 0;
	
	private double columnStyle1 = TableLayoutConstants.PREFERRED;
	private double columnStyle2 = TableLayoutConstants.FILL;
	
	private ImageIcon condensedIcon;
	private ImageIcon uncondensedIcon;
	
	private List<ActionListener> collapse_listeners = new ArrayList<ActionListener>();
	
	private JPanel rowPanel;
	private static HashMap<String, Boolean> showCondensed = new HashMap<String, Boolean>();
	private boolean showCondenseButton;
	private boolean showHelpButton;
	private JLabel titleLabel;
	private boolean searchEnabled = false;
	
	private boolean sortedRows;
	
	private ArrayList<GuiRow> guiComponentRows = new ArrayList<GuiRow>();
	private ArrayList<GuiRow> guiComponentInvisibleRows = new ArrayList<GuiRow>();
	private ActionListener helpActionListener;
	private CondenseButtonLayout condenseStyle = CondenseButtonLayout.LEFT;
	private int maxRowCount = -1;
	private int currentPage = 0;
	private Color rowBackground0; // if <> null, it is used to set the
	// background color
	private Color rowBackground1;
	private String lastSearchText = "";
	private SearchFilter searchFilter;
	protected JTextField currentSearchInputField;
	private int activeSearchResult = -1;
	private boolean lockRowCount;
	private Iconsize bigIcons = Iconsize.SMALL;
	private boolean hideSearch;
	
	public void setIconSize(Iconsize bigIcons) {
		this.bigIcons = bigIcons;
	}
	
	public FolderPanel(String title, boolean openCondensed,
						boolean showCondenseButton, boolean sortRows,
						ActionListener helpActionListener) {
		initComponent(title, openCondensed, showCondenseButton, sortRows,
							helpActionListener);
	}
	
	public void enableSearch(boolean enable) {
		this.searchEnabled = enable;
	}
	
	public void setColumnStyle(double left, double right) {
		columnStyle1 = left;
		columnStyle2 = right;
	}
	
	private void initComponent(String title, boolean openCondensed,
						boolean showCondenseButton, boolean sortRows,
						ActionListener helpActionListener) {
		this.title = title;
		this.showCondenseButton = showCondenseButton;
		this.condensedState = openCondensed;
		this.sortedRows = sortRows;
		rowPanel = new JPanel();
		setBorder(BorderFactory.createLineBorder(frameColor, frameWidth));
		titleLabel = new JLabel(title);
		titleLabel.setOpaque(true);
		titleLabel.setBackground(frameColor);
		titleLabel.setForeground(headingColor);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 0));
		this.helpActionListener = helpActionListener;
		this.showHelpButton = helpActionListener != null;
	}
	
	public FolderPanel(String title, boolean sortRows,
						ActionListener helpActionListener, String helpTopic) {
		if (showCondensed.containsKey(title)) {
			Boolean showFolded = showCondensed.get(title);
			initComponent(title, showFolded.booleanValue(), true, sortRows,
								helpActionListener);
		} else
			initComponent(title, false, true, sortRows, helpActionListener);
	}
	
	public FolderPanel(String title) {
		this(title, false, false, false, null);
	}
	
	public void setEmptyBorderWidth(int emptyBorderWidth) {
		setFrameColor(frameColor, headingColor, frameWidth, emptyBorderWidth);
	}
	
	public void setRowColSpacing(int rowSpacing, int colSpacing) {
		this.rowSpacing = rowSpacing;
		this.colSpacing = colSpacing;
	}
	
	public Color getFrameColor() {
		return frameColor;
	}
	
	public void setFrameColor(Color frameColor, Color headingColor,
						int frameWidth, int emptyBorderWidth) {
		this.frameColor = frameColor;
		this.headingColor = headingColor;
		this.frameWidth = frameWidth;
		this.emptyBorderWidth = emptyBorderWidth;
		titleLabel.setBackground(frameColor);
		setBorder(BorderFactory.createLineBorder(frameColor, frameWidth));
		layoutRows();
	}
	
	public void setFrameColor(Color frameColor, Color headingColor) {
		this.frameColor = frameColor;
		this.headingColor = headingColor;
		titleLabel.setBackground(frameColor);
		setBorder(BorderFactory.createLineBorder(frameColor, frameWidth));
		layoutRows();
	}
	
	@Override
	public void setBackground(Color backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	
	public void addFirstGuiComponentRow(JComponent leftComponent,
						JComponent rightComponent, boolean updateLayout, int spaceAroundComponents) {
		int sp = spaceAroundComponents;
		if (spaceAroundComponents == 0)
			guiComponentRows.add(0, new GuiRow(leftComponent, rightComponent));
		else
			guiComponentRows.add(0, new GuiRow(
								getBorderedComponent(leftComponent, sp, sp, sp, sp),
								getBorderedComponent(rightComponent, sp, sp, sp, sp)));
		if (updateLayout)
			layoutRows();
	}
	
	public GuiRow addGuiComponentRow(JComponent leftComponent,
						JComponent rightComponent, boolean updateLayout) {
		synchronized (guiComponentRows) {
			GuiRow gr = new GuiRow(leftComponent, rightComponent);
			guiComponentRows.add(gr);
			if (updateLayout)
				layoutRows();
			return gr;
		}
	}
	
	public void removeGuiComponentRow(GuiRow guiRow, boolean updateLayout) {
		synchronized (guiComponentRows) {
			guiComponentRows.remove(guiRow);
		}
		synchronized (guiComponentInvisibleRows) {
			guiComponentInvisibleRows.remove(guiRow);
		}
		if (updateLayout)
			layoutRows();
	}
	
	public void exchangeGuiComponentRow(GuiRow guiRow, GuiRow newRow, boolean updateLayout) {
		synchronized (guiComponentRows) {
			int idx = guiComponentRows.indexOf(guiRow);
			guiComponentRows.remove(guiRow);
			guiComponentRows.add(idx, newRow);
			if (updateLayout)
				layoutRows();
		}
	}
	
	/**
	 * Removes all current known GUI component rows from the internal list. Use <code>addGuiComponentRow</code> to refill this list and <code>layoutRows</code>
	 * to fill the actual GUI of this component.
	 */
	public void clearGuiComponentList() {
		guiComponentRows.clear();
		guiComponentInvisibleRows.clear();
		currentPage = 0;
		activeSearchResult = -1;
		if (currentSearchInputField != null)
			currentSearchInputField.setText("");
	}
	
	public void addGuiComponentRow(GuiRow row, boolean updateLayout) {
		guiComponentRows.add(row);
		if (updateLayout)
			layoutRows();
	}
	
	public static TableLayout getVSplitLayout(JComponent topComp,
						JComponent bottomComp, double topSize, double bottomSize) {
		double border = 0;
		double[][] size = { { border, TableLayoutConstants.FILL, border }, // Columns
				{ border, topSize, bottomSize, border } }; // Rows
		return new TableLayout(size);
	}
	
	public static TableLayout getHSplitLayout(JComponent leftComponent,
						JComponent rightComponent, double leftSize, double rightSize) {
		double border = 0;
		double[][] size = { { border, leftSize, rightSize, border }, // Columns
				{ border, TableLayoutConstants.PREFERRED, border } // ROWS
		}; // Rows
		return new TableLayout(size);
	}
	
	public void layoutRows() {
		if (!SwingUtilities.isEventDispatchThread()) {
			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					layoutRows();
					return;
				}
			});
		};
		synchronized (guiComponentRows) {
			checkCondensedState();
			removeAll();
			
			rowPanel.removeAll();
			ArrayList<GuiRow> guiComponentRowsForCurrentPage = new ArrayList<GuiRow>();
			guiComponentRowsForCurrentPage
								.addAll(getFilteredList(guiComponentRows));
			if (guiComponentRowsForCurrentPage.size() > maxRowCount
								&& maxRowCount > 0) {
				ArrayList<GuiRow> remove = new ArrayList<GuiRow>();
				for (int i = 0; i < guiComponentRowsForCurrentPage.size(); i++) {
					if (!(i >= currentPage * maxRowCount && i < (currentPage + 1)
										* maxRowCount))
						remove.add(guiComponentRowsForCurrentPage.get(i));
				}
				if (remove.size() > 0) {
					for (GuiRow gr : remove)
						guiComponentRowsForCurrentPage.remove(gr);
				}
			}
			int maxX = 2;
			int maxY = guiComponentRowsForCurrentPage.size();
			double[][] size = new double[2][];
			size[0] = new double[maxX];
			size[1] = new double[maxY];
			size[0][0] = columnStyle1; // first column //
			// TableLayoutConstants.PREFERRED
			size[0][1] = columnStyle2; // TableLayoutConstants.FILL;
			
			rowPanel.setBorder(BorderFactory.createEmptyBorder(
								maxY > 0 ? emptyBorderWidth : 0, maxY > 0 ? emptyBorderWidth
													: 0, maxY > 0 ? emptyBorderWidth : 0,
									maxY > 0 ? emptyBorderWidth : 0));
			titleLabel
								.setForeground(maxY > 0 ? headingColor
													: (headingColor != null ? headingColor.darker()
																		: headingColor));
			boolean hasData = guiComponentRowsForCurrentPage.size() > 0
								|| guiComponentInvisibleRows.size() > 0;
			titleLabel.setText(hasData ? title : title); // + " (no data)");
			titleLabel.validate();
			
			boolean firstColumn = true;
			int row = 0;
			ArrayList<GuiRow> workSet;
			if (sortedRows)
				workSet = getSortedRows(guiComponentRowsForCurrentPage);
			else
				workSet = guiComponentRowsForCurrentPage;
			
			for (int y = 0; y < maxY; y++) {
				if (workSet.get(y) != null && workSet.get(y).right != null &&
									(workSet.get(y).right instanceof JScrollPane)) {
					size[1][y] = TableLayoutConstants.FILL;
					// workSet.get(y).right.setBorder(BorderFactory.createLineBorder(Color.red));
				} else
					size[1][y] = TableLayoutConstants.PREFERRED;
			}
			
			rowPanel.setLayout(new TableLayout(size));
			
			for (GuiRow gr : workSet) {
				if (rowSpacing > 0 || colSpacing > 0) {
					if (gr.span) {
						rowPanel.add(getBorderedComponent(gr.left, 0, 0, gr != workSet.get(workSet.size() - 1) ? rowSpacing : 0,
											colSpacing), "0," + row + ", 1, " + row); // left orientation
					} else {
						rowPanel.add(getBorderedComponent(gr.left, 0, 0, gr != workSet.get(workSet.size() - 1) ? rowSpacing : 0,
											colSpacing), "0," + row + ", l"); // left orientation
						rowPanel.add(
											getBorderedComponent(gr.right, 0, 0, gr != workSet.get(workSet.size() - 1) ? rowSpacing : 0, 0),
											"1," + row);
					}
				} else {
					if (gr.span) {
						rowPanel.add(gr.left, "0," + row + ", 1, " + row);
					} else {
						rowPanel.add(gr.left, "0," + row + ", l"); // left orientation
						rowPanel.add(gr.right, "1," + row);
					}
				}
				row++;
				colorRow(firstColumn, gr.left, gr.right);
				firstColumn = !firstColumn;
			}
			if (backgroundColor != null)
				if (backgroundColor.getRGB() == Color.BLACK.getRGB())
					rowPanel.setBackground(null);
				else
					rowPanel.setBackground(backgroundColor);
			else {
				rowPanel.setOpaque(false);
			}
			rowPanel.validate();
			
			if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.GravistoJavaHelp))
				showHelpButton = false;
			
			if (showCondenseButton || showHelpButton || maxRowCount > 0
								|| searchEnabled) {
				JComponent button1 = null, button2 = null;
				JComponent titleComp = titleLabel;
				if (searchEnabled) {
					JComponent sfield = getSearchField();
					titleComp = TableLayout.getSplit(titleLabel, sfield,
										TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED);
				}
				if (maxRowCount > 0) {
					JComponent lrb = getLeftRightButton();
					titleComp = TableLayout.getSplit(titleComp, lrb,
										TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED);
				}
				if (showCondenseButton) {
					final JComponent condenseCmdPanel = getCondenseButton();
					condenseCmdPanel.setEnabled(hasData);
					button1 = condenseCmdPanel;
					addTitleMouseClickHandler(condenseCmdPanel);
				}
				if (showHelpButton) {
					JComponent helpButton = getHelpButton();
					button2 = helpButton;
				}
				JComponent labelPanel = titleComp;
				if (button1 != null && button2 == null) {
					if (condenseStyle == CondenseButtonLayout.RIGHT)
						labelPanel = TableLayout.getSplit(titleComp, button1,
											TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED);
					else
						labelPanel = TableLayout.getSplit(button1, titleComp,
											TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL);
				}
				if (button2 != null && button1 == null)
					labelPanel = TableLayout.getSplit(titleComp, button2,
										TableLayoutConstants.FILL, TableLayoutConstants.PREFERRED);
				if (button1 != null && button2 != null) {
					if (condenseStyle == CondenseButtonLayout.RIGHT)
						labelPanel = TableLayout.getSplit(titleComp, TableLayout
											.getSplit(button2, button1, TableLayoutConstants.PREFERRED,
																TableLayoutConstants.PREFERRED), TableLayoutConstants.FILL,
											TableLayoutConstants.PREFERRED);
					else
						labelPanel = TableLayout.getSplit(button1, TableLayout
											.getSplit(titleComp, button2, TableLayoutConstants.FILL,
																TableLayout.PREFERRED),
											TableLayout.PREFERRED, TableLayout.FILL);
				}
				setLayout(getVSplitLayout(labelPanel, rowPanel,
									TableLayout.PREFERRED, TableLayout.FILL));
				add(labelPanel, "1,1");
			} else {
				setLayout(getVSplitLayout(titleLabel, rowPanel,
									TableLayout.PREFERRED, TableLayout.FILL));
				add(titleLabel, "1,1");
			}
			add(rowPanel, "1,2");
			
			validate();
			repaint();
			
			if (!hideSearch && lastSearchText.length() > 0 && currentSearchInputField != null) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						currentSearchInputField.requestFocusInWindow();
					}
				});
			}
		}
	}
	
	private ArrayList<GuiRow> getFilteredList(ArrayList<GuiRow> l1) {
		activeSearchResult = -1;
		if (searchFilter == null || !searchEnabled)
			return l1;
		ArrayList<GuiRow> result = new ArrayList<GuiRow>();
		for (GuiRow gr : l1)
			if (searchFilter.accept(gr, lastSearchText))
				result.add(gr);
		activeSearchResult = result.size();
		return result;
	}
	
	private void colorRow(boolean firstColumn, JComponent left, JComponent right) {
		if (rowBackground0 == null || rowBackground1 == null)
			return;
		if (firstColumn) {
			// if (left!=null)
			// left.setBorder(BorderFactory.createMatteBorder(0,2,0,0,rowBackground0));
		} else {
			// if (left!=null)
			// left.setBorder(BorderFactory.createMatteBorder(0,2,0,0,rowBackground1));
		}
	}
	
	private void addTitleMouseClickHandler(final JComponent condenseCmdPanel) {
		MouseListener ml[] = titleLabel.getMouseListeners();
		titleLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		for (MouseListener m : ml)
			titleLabel.removeMouseListener(m);
		titleLabel.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				for (int i = 0; i < condenseCmdPanel.getComponentCount(); i++) {
					Component c = condenseCmdPanel.getComponent(i);
					if (c instanceof JButton) {
						JButton cmdButton = (JButton) c;
						cmdButton.doClick();
						break;
					}
				}
			}
			
			public void mousePressed(MouseEvent e) {
				//
				
			}
			
			public void mouseReleased(MouseEvent e) {
				//
				
			}
			
			public void mouseEntered(MouseEvent e) {
				// titleLabel.setBackground(frameColor.brighter());
			}
			
			public void mouseExited(MouseEvent e) {
				// titleLabel.setBackground(frameColor);
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	private ArrayList<GuiRow> getSortedRows(ArrayList<GuiRow> rows) {
		GuiRow sorted[] = rows.toArray(new GuiRow[] {});
		Arrays.sort(sorted, new Comparator() {
			public int compare(Object o1, Object o2) {
				GuiRow r1 = (GuiRow) o1;
				GuiRow r2 = (GuiRow) o2;
				JComponent jc1 = r1.left;
				JComponent jc2 = r2.left;
				String label1 = "";
				String label2 = "";
				if (jc1 instanceof JLabel) {
					label1 = ((JLabel) jc1).getText();
					label1 = StringManipulationTools.stringReplace(label1, " ", "");
					label1 = StringManipulationTools.stringReplace(label1, "<html>", "");
					label1 = StringManipulationTools.stringReplace(label1, "<small>", "");
					label1 = StringManipulationTools.stringReplace(label1, "<br>", "");
					label1 = StringManipulationTools.stringReplace(label1, "&nbsp;", "");
				}
				if (jc2 instanceof JLabel) {
					label2 = ((JLabel) jc2).getText();
					label2 = StringManipulationTools.stringReplace(label2, " ", "");
					label2 = StringManipulationTools.stringReplace(label2, "<html>", "");
					label2 = StringManipulationTools.stringReplace(label2, "<small>", "");
					label2 = StringManipulationTools.stringReplace(label2, "<br>", "");
					label2 = StringManipulationTools.stringReplace(label2, "&nbsp;", "");
				}
				
				return label1.compareTo(label2);
			}
		});
		ArrayList<GuiRow> result = new ArrayList<GuiRow>();
		for (GuiRow gr : sorted)
			result.add(gr);
		return result;
	}
	
	public void setCondensedState(boolean condensed) {
		this.condensedState = condensed;
	}
	
	private void checkCondensedState() {
		if (condensedState) {
			guiComponentInvisibleRows.addAll(guiComponentRows);
			guiComponentRows.clear();
		} else {
			guiComponentRows.addAll(guiComponentInvisibleRows);
			guiComponentInvisibleRows.clear();
		}
	}
	
	private JComponent getCondenseButton() {
		JToolBar tb = new JToolBar();
		tb.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		tb.setBorderPainted(false);
		tb.setFloatable(false);
		tb.setOpaque(true);
		tb.setBackground(frameColor);
		tb.setLayout(new TableLayout(new double[][] { { TableLayout.FILL },
							{ TableLayout.FILL } }));
		
		final JButton cmdButton = new JButton();
		// result.setContentAreaFilled(false);
		cmdButton.setBackground(frameColor);
		cmdButton.setBorderPainted(false);
		cmdButton.setRolloverEnabled(true);
		cmdButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		// result.setOpaque(false);
		ClassLoader cl = FolderPanel.class.getClassLoader();
		String path = FolderPanel.class.getPackage().getName().replace('.', '/') + "/images";
		if (condenseStyle == CondenseButtonLayout.RIGHT) {
			condensedIcon = new ImageIcon(cl.getResource(path + "/unfold.png"));
			uncondensedIcon = new ImageIcon(cl.getResource(path + "/fold.png"));
		} else {
			condensedIcon = new ImageIcon(cl.getResource(path + "/unfoldL.png"));
			uncondensedIcon = new ImageIcon(cl.getResource(path + "/foldL.png"));
		}
		if (condensedState)
			cmdButton.setIcon(condensedIcon);
		else
			cmdButton.setIcon(uncondensedIcon);
		cmdButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				condensedState = !condensedState;
				if (condensedState)
					cmdButton.setIcon(condensedIcon);
				else
					cmdButton.setIcon(uncondensedIcon);
				showCondensed.put(title, new Boolean(condensedState));
				layoutRows();
				for (ActionListener al : collapse_listeners) {
					al.actionPerformed(new ActionEvent(this, condensedState ? 1 : 0, "collapseevent"));
				}
			}
		});
		int s = 0;
		cmdButton.setMargin(new Insets(s, s, s, s));
		tb.add(cmdButton, "0,0");
		tb.validate();
		return tb;
	}
	
	private JComponent getSearchField() {
		JToolBar tb = new JToolBar();
		tb.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		tb.setBorderPainted(false);
		tb.setFloatable(false);
		tb.setOpaque(false);
		tb.setBackground(frameColor);
		tb.setLayout(new TableLayout(new double[][] {
							{ TableLayout.PREFERRED, TableLayout.PREFERRED, 40 },
							{ TableLayout.FILL } }));
		
		final JButton cmdButtonS = new JButton();
		cmdButtonS.setToolTipText(getSearchHintText());
		final JTextField input = new JTextField();
		if (hideSearch)
			input.setVisible(false);
		currentSearchInputField = input;
		input.setToolTipText(getSearchHintText());
		input.setBorder(BorderFactory.createEtchedBorder(Color.WHITE,
							Color.LIGHT_GRAY));
		input.setBackground(frameColor);
		input.setOpaque(true);
		input.setText(lastSearchText);
		input.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent arg0) {
			}
			
			public void keyReleased(KeyEvent arg0) {
			}
			
			public void keyTyped(KeyEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						lastSearchText = input.getText();
						currentPage = 0;
						layoutRows();
						for (ActionListener al : collapse_listeners) {
							al.actionPerformed(new ActionEvent(this,
												condensedState ? 0 : 1, "collapseevent"));
						}
					}
				});
			}
		});
		input.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent e) {
				input.setOpaque(true);
				input.setBackground(Color.white);
				input.repaint();
			}
			
			public void focusLost(FocusEvent e) {
				input.setOpaque(true);
				input.setBackground(frameColor);
				lastSearchText = input.getText();
				input.repaint();
			}
		});
		
		// layoutRows();
		// for (ActionListener al : collapse_listeners) {
		// al.actionPerformed(new ActionEvent(this, condensedState ? 0 : 1,
		// "collapseevent"));
		// }
		
		cmdButtonS.setBackground(frameColor);
		cmdButtonS.setOpaque(true);
		cmdButtonS.setBorderPainted(false);
		cmdButtonS.setRolloverEnabled(true);
		
		JLabel searchResLabel = new JLabel();
		searchResLabel.setOpaque(true);
		searchResLabel.setBackground(frameColor);
		if (activeSearchResult > 0) {
			if (activeSearchResult < guiComponentRows.size()
								+ guiComponentInvisibleRows.size())
				searchResLabel.setText("<html><font color='gray'><small>"
									+ activeSearchResult + "&nbsp;");
		}
		
		// result.setOpaque(false);
		ImageIcon searchIcon = getSearchIcon();
		cmdButtonS.setIcon(searchIcon);
		int s = 0;
		cmdButtonS.setMargin(new Insets(s, s, s, s));
		tb.add(cmdButtonS, "0,0");
		tb.add(searchResLabel, "1,0");
		JComponent jc = FolderPanel.getBorderedComponent(input, 0, 0, 2, 2);
		jc.setOpaque(true);
		jc.setBackground(frameColor);
		tb.add(jc, "2,0");
		tb.validate();
		return tb;
	}
	
	private String getSearchHintText() {
		return "Enter text into the search field to filter the list content";
	}
	
	public static ImageIcon getSearchIcon() {
		ClassLoader cl = FolderPanel.class.getClassLoader();
		String path = FolderPanel.class.getPackage().getName()
							.replace('.', '/') + "/images";
		ImageIcon searchIcon = new ImageIcon(cl.getResource(path + "/lupe.png"));
		return searchIcon;
	}
	
	private JComponent getLeftRightButton() {
		JToolBar tb = new JToolBar();
		tb.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		tb.setBorderPainted(false);
		tb.setFloatable(false);
		tb.setOpaque(true);
		tb.setBackground(frameColor);
		tb.setLayout(new TableLayout(new double[][] {
							{ TableLayout.PREFERRED, TableLayout.PREFERRED,
												TableLayout.PREFERRED, TableLayout.PREFERRED,
												TableLayout.PREFERRED }, { TableLayout.FILL } }));
		
		final JButton cmdButtonReduceMaximumRowCount = new JButton(); // less rows
		final JButton cmdButtonIncreaseMaximumRowCount = new JButton(); // more rows
		
		final JButton cmdButton1 = new JButton();
		final JButton cmdButton2 = new JButton();
		
		int gcs = guiComponentRows.size();
		if (condensedState)
			gcs = guiComponentInvisibleRows.size();
		if (searchEnabled && activeSearchResult >= 0)
			gcs = activeSearchResult;
		
		int pages = gcs / maxRowCount;
		if (gcs % maxRowCount > 0)
			pages++;
		
		cmdButtonReduceMaximumRowCount.setEnabled(maxRowCount > 1 && gcs > 0);
		cmdButtonIncreaseMaximumRowCount.setEnabled(gcs > 0);
		
		cmdButton1.setEnabled(currentPage > 0);
		cmdButton2.setEnabled(currentPage + 1 < pages);
		
		cmdButtonReduceMaximumRowCount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				maxRowCount--;
				currentPage = 0;
				layoutRows();
				for (ActionListener al : collapse_listeners) {
					al.actionPerformed(new ActionEvent(this, condensedState ? 0
										: 1, "collapseevent"));
				}
			}
		});
		cmdButtonIncreaseMaximumRowCount.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				maxRowCount++;
				currentPage = 0;
				layoutRows();
				for (ActionListener al : collapse_listeners) {
					al.actionPerformed(new ActionEvent(this, condensedState ? 0
										: 1, "collapseevent"));
				}
			}
		});
		
		cmdButton1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentPage--;
				layoutRows();
				for (ActionListener al : collapse_listeners) {
					al.actionPerformed(new ActionEvent(this, condensedState ? 0
										: 1, "collapseevent"));
				}
			}
		});
		cmdButton2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				currentPage++;
				layoutRows();
				for (ActionListener al : collapse_listeners) {
					al.actionPerformed(new ActionEvent(this, condensedState ? 0
										: 1, "collapseevent"));
				}
			}
		});
		
		cmdButtonReduceMaximumRowCount.setBackground(frameColor);
		cmdButtonReduceMaximumRowCount.setBorderPainted(false);
		cmdButtonReduceMaximumRowCount.setRolloverEnabled(true);
		cmdButtonReduceMaximumRowCount.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		cmdButtonIncreaseMaximumRowCount.setBackground(frameColor);
		cmdButtonIncreaseMaximumRowCount.setBorderPainted(false);
		cmdButtonIncreaseMaximumRowCount.setRolloverEnabled(true);
		cmdButtonIncreaseMaximumRowCount.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		cmdButton1.setBackground(frameColor);
		cmdButton1.setBorderPainted(false);
		cmdButton1.setRolloverEnabled(true);
		cmdButton1.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		cmdButton2.setBackground(frameColor);
		cmdButton2.setBorderPainted(false);
		cmdButton2.setRolloverEnabled(true);
		cmdButton2.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		
		cmdButtonReduceMaximumRowCount.setToolTipText("reduce row count");
		cmdButtonIncreaseMaximumRowCount.setToolTipText("increase row count");
		cmdButton1.setToolTipText("turn page");
		cmdButton2.setToolTipText("turn page");
		
		// result.setOpaque(false);
		ClassLoader cl = FolderPanel.class.getClassLoader();
		String path = FolderPanel.class.getPackage().getName().replace('.', '/') + "/images";
		ImageIcon leftIcon = null;
		ImageIcon rightIcon = null;
		ImageIcon lessIcon = new ImageIcon(cl
							.getResource(path + "/bw_fold.png"));
		ImageIcon moreIcon = new ImageIcon(cl.getResource(path
							+ "/bw_unfold.png"));
		
		if (bigIcons == Iconsize.LARGE) {
			leftIcon = new ImageIcon(cl
								.getResource(path + "/large_left.png"));
			rightIcon = new ImageIcon(cl.getResource(path
								+ "/large_right.png"));
		} else
			if (bigIcons == Iconsize.MIDDLE) {
				leftIcon = new ImageIcon(cl
									.getResource(path + "/middle_left.png"));
				rightIcon = new ImageIcon(cl.getResource(path
									+ "/middle_right.png"));
			} else
				if (bigIcons == Iconsize.SMALL) {
					leftIcon = new ImageIcon(cl
										.getResource(path + "/bw_left.png"));
					rightIcon = new ImageIcon(cl.getResource(path
										+ "/bw_right.png"));
				}
		
		cmdButton1.setIcon(leftIcon);
		cmdButton2.setIcon(rightIcon);
		cmdButtonReduceMaximumRowCount.setIcon(lessIcon);
		cmdButtonIncreaseMaximumRowCount.setIcon(moreIcon);
		int s = 0;
		cmdButton1.setMargin(new Insets(s, s, s, s));
		cmdButton2.setMargin(new Insets(s, s, s, s));
		cmdButtonReduceMaximumRowCount.setMargin(new Insets(s, s, s, s));
		cmdButtonIncreaseMaximumRowCount.setMargin(new Insets(s, s, s, s));
		String pt = "<html><font color='gray'><small>" + (currentPage + 1)
							+ "/" + (pages);
		if (((currentPage + 1) + "/" + (pages)).equals("1/0"))
			pt = "";
		JLabel pageLabel = new JLabel(pt);
		pageLabel.setBackground(frameColor);
		pageLabel.setOpaque(true);
		if (!lockRowCount) {
			tb.add(cmdButtonReduceMaximumRowCount, "0,0");
			tb.add(cmdButtonIncreaseMaximumRowCount, "1,0");
		}
		tb.add(cmdButton1, "2,0");
		tb.add(pageLabel, "3,0");
		tb.add(cmdButton2, "4,0");
		tb.validate();
		return tb;
	}
	
	public static ImageIcon getLeftRightIcon(Iconsize bigIcons, boolean left) {
		ClassLoader cl = FolderPanel.class.getClassLoader();
		String path = FolderPanel.class.getPackage().getName().replace('.', '/') + "/images";
		ImageIcon leftIcon = null;
		ImageIcon rightIcon = null;
		
		if (bigIcons == Iconsize.LARGE) {
			leftIcon = new ImageIcon(cl
								.getResource(path + "/large_left.png"));
			rightIcon = new ImageIcon(cl.getResource(path
								+ "/large_right.png"));
		} else
			if (bigIcons == Iconsize.MIDDLE) {
				leftIcon = new ImageIcon(cl
									.getResource(path + "/middle_left.png"));
				rightIcon = new ImageIcon(cl.getResource(path
									+ "/middle_right.png"));
			} else
				if (bigIcons == Iconsize.SMALL) {
					leftIcon = new ImageIcon(cl
										.getResource(path + "/bw_left.png"));
					rightIcon = new ImageIcon(cl.getResource(path
										+ "/bw_right.png"));
				}
		if (left)
			return leftIcon;
		else
			return rightIcon;
	}
	
	public static JComponent getHelpButton(ActionListener helpActionListener,
						Color frameColor) {
		JToolBar tb = new JToolBar();
		tb.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		tb.setFloatable(false);
		tb.setOpaque(false);
		tb.setBackground(frameColor);
		tb.setLayout(new TableLayout(new double[][] { { TableLayoutConstants.FILL },
							{ TableLayoutConstants.FILL } }));
		// tb.setBackground(frameColor);
		final JButton result = new JButton();
		result.setBackground(frameColor);
		result.setBorderPainted(false);
		result.setRolloverEnabled(true);
		result.setOpaque(frameColor != null);
		ClassLoader cl = FolderPanel.class.getClassLoader();
		String path = FolderPanel.class.getPackage().getName()
							.replace('.', '/') + "/images";
		
		result.setIcon(new ImageIcon(cl.getResource(path + "/help2.png")));
		result.addActionListener(helpActionListener);
		/*
		 * result.addActionListener(new ActionListener() { public void
		 * actionPerformed(ActionEvent e) { System.out.println("Showing Help:
		 * "+helpTopic); }});
		 */
		int s = 1;
		if (!ReleaseInfo.getIsAllowedFeature(FeatureSet.GravistoJavaHelp))
			s = 0;
		result.setMargin(new Insets(s, s, s, s));
		if (ReleaseInfo.getIsAllowedFeature(FeatureSet.GravistoJavaHelp))
			tb.add(result, "0,0");
		tb.validate();
		tb.repaint();
		return tb;
	}
	
	private JComponent getHelpButton() {
		return getHelpButton(helpActionListener, frameColor);
	}
	
	public int getRowCount() {
		return guiComponentRows.size() + guiComponentInvisibleRows.size();
	}
	
	public ArrayList<GuiRow> getVisibleGuiRows() {
		return guiComponentRows;
	}
	
	public ArrayList<GuiRow> getAllGuiRows() {
		ArrayList<GuiRow> result = new ArrayList<GuiRow>();
		result.addAll(guiComponentRows);
		result.addAll(guiComponentInvisibleRows);
		return result;
	}
	
	public JComponent getBorderedComponent(int top, int left, int bottom,
						int right) {
		JComponent result = TableLayout.getSplitVertical(this, null,
							TableLayout.PREFERRED, 0);
		result.setBorder(BorderFactory.createEmptyBorder(top, left, bottom,
							right));
		return result;
	}
	
	public static JComponent getBorderedComponent(JComponent comp, int top,
						int left, int bottom, int right) {
		if (comp == null)
			return null;
		JComponent result = TableLayout.getSplitVertical(comp, null,
							TableLayoutConstants.PREFERRED, 0);
		result.setBorder(BorderFactory.createEmptyBorder(top, left, bottom,
							right));
		result.setBackground(null);
		return result;
	}
	
	public void addCollapseListener(ActionListener listener) {
		collapse_listeners.add(listener);
	}
	
	public GuiRow addGuiComponentRow(JComponent left, JComponent right,
						boolean updateLayout, int spaceAroundElements) {
		int sp = spaceAroundElements;
		GuiRow gr = null;
		if (spaceAroundElements == 0)
			gr = addGuiComponentRow(left, right, updateLayout);
		else
			gr = addGuiComponentRow(getBorderedComponent(left, sp, sp, sp, sp),
								getBorderedComponent(right, sp, sp, sp, sp), updateLayout);
		if (right != null && right instanceof JButton)
			((JButton) right).setOpaque(false);
		return gr;
	}
	
	public void addComp(JComponent component, int border) {
		if (border == 0)
			addComp(component);
		else
			addComp(getBorderedComponent(component, border, border, border,
								border));
	}
	
	public void addComp(JComponent comp) {
		addGuiComponentRow(null, comp, false);
	}
	
	public void setTitle(String newTitle) {
		titleLabel.setText(newTitle);
		titleLabel.repaint();
		this.title = newTitle;
	}
	
	public void addCollapseListenerDialogSizeUpdate() {
		final FolderPanel fp = this;
		ActionListener resizeListener = new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						Component pc = fp.getParent();
						performDialogResize(pc);
					}
				});
			}
		};
		addCollapseListener(resizeListener);
	}
	
	public void dialogSizeUpdate() {
		final FolderPanel fp = this;
		try {
			final Component pc = fp.getParent();
			if (!SwingUtilities.isEventDispatchThread())
				SwingUtilities.invokeAndWait(new Runnable() {
					public void run() {
						performDialogResize(pc);
					}
				});
			else
				performDialogResize(pc);
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
	}
	
	public static void performDialogResize(Component startComponent) {
		Component pc = startComponent;
		while (!((pc instanceof JDialog) || (pc instanceof JFrame))
							&& pc != null) {
			// System.out.println(pc.getClass().getCanonicalName());
			pc = pc.getParent();
		}
		if (pc != null && pc instanceof JDialog) {
			final JDialog jf = (JDialog) pc;
			jf.pack();
			jf.pack();
			jf.repaint();
		}
		if (pc != null && pc instanceof JFrame) {
			JFrame jf = (JFrame) pc;
			jf.pack();
			jf.repaint();
		}
	}
	
	public static void closeParentDialog(Component startComponent) {
		Component pc = startComponent;
		while (!((pc instanceof JDialog) || (pc instanceof JFrame))
							&& pc != null) {
			pc = pc.getParent();
		}
		if (pc != null && pc instanceof JDialog) {
			JDialog jf = (JDialog) pc;
			jf.setVisible(false);
			jf.dispose();
		}
		if (pc != null && pc instanceof JFrame) {
			JFrame jf = (JFrame) pc;
			jf.setVisible(false);
			jf.dispose();
		}
	}
	
	public void setMaximumRowCount(int maxRowCount) {
		setMaximumRowCount(maxRowCount, false);
	}
	
	public void setMaximumRowCount(int maxRowCount, boolean locked) {
		this.maxRowCount = maxRowCount;
		this.lockRowCount = locked;
	}
	
	public void setRowBackground0(Color col0) {
		rowBackground0 = col0;
	}
	
	public void setRowBackground1(Color col1) {
		rowBackground1 = col1;
	}
	
	public void addSearchFilter(SearchFilter filter) {
		this.searchFilter = filter;
	}
	
	public void addDefaultTextSearchFilter() {
		addDefaultTextSearchFilterFixed(null);
	}
	
	public void addDefaultTextSearchFilterFixed(final String optFixedSearch) {
		if (optFixedSearch != null) {
			lastSearchText = optFixedSearch;
			hideSearch = true;
		}
		addSearchFilter(getDefaultSearchFilter(optFixedSearch));
	}
	
	public String getTitle() {
		return title;
	}
	
	public void mergeRowsWithSameLeftLabel() {
		HashMap<String, ArrayList<GuiRow>> descAndGuiRows = new HashMap<String, ArrayList<GuiRow>>();
		for (GuiRow gr : guiComponentRows) {
			if (gr.left != null && gr.left instanceof JLabel) {
				String lbl = ((JLabel) gr.left).getText();
				if (lbl != null && lbl.length() > 0) {
					if (!descAndGuiRows.containsKey(lbl))
						descAndGuiRows.put(lbl, new ArrayList<GuiRow>());
					descAndGuiRows.get(lbl).add(gr);
				}
			}
		}
		ArrayList<GuiRow> toBeDeleted = new ArrayList<GuiRow>();
		ArrayList<GuiRow> toBeAdded = new ArrayList<GuiRow>();
		for (ArrayList<GuiRow> grl : descAndGuiRows.values()) {
			if (grl.size() > 1) {
				toBeDeleted.addAll(grl);
				ArrayList<JComponent> grlRight = new ArrayList<JComponent>();
				for (GuiRow gr : grl)
					grlRight.add(gr.right);
				// Collections.sort(grlRight, new Comparator<JComponent>() {
				// @Override
				// public int compare(JComponent o1, JComponent o2) {
				// String a = o1.getToolTipText();
				// String b = o2.getToolTipText();
				// if (a==null) a = "";
				// if (b==null) b = "";
				// if (a.contains("Width") && b.contains("Height"))
				// return -1;
				// if (a.contains("Height") && b.contains("Width"))
				// return 1;
				// String numA = ErrorMsg.getNummericChars(a);
				// String numB = ErrorMsg.getNummericChars(a);
				//
				// return a.compareTo(b);
				// }});
				toBeAdded.add(new GuiRow(grl.iterator().next().left, TableLayout.getMultiSplit(grlRight)));
			}
		}
		guiComponentRows.removeAll(toBeDeleted);
		guiComponentInvisibleRows.removeAll(toBeDeleted);
		for (GuiRow gr : toBeAdded)
			addGuiComponentRow(gr, false);
	}
	
	public enum Iconsize {
		SMALL, MIDDLE, LARGE
	}
	
	public void setShowCondenseButton(boolean b) {
		showCondenseButton = b;
	}
	
	public int getFixedSearchFilterMatchCount() {
		return getFilteredList(guiComponentRows).size();
	}
	
	public static SearchFilter getDefaultSearchFilter(final String optFixedSearch) {
		return new SearchFilter() {
			public boolean accept(GuiRow gr, String searchText) {
				if (gr.left == null || gr.right == null || searchText == null)
					return true;
				if (optFixedSearch != null)
					searchText = optFixedSearch;
				searchText = searchText.toUpperCase();
				String c1 = "";
				String c2 = "";
				JComponent left = findMyComponent(gr.left);
				JComponent right = findMyComponent(gr.right);
				if (left != null && left instanceof AbstractButton) {
					AbstractButton jb = (AbstractButton) left;
					c1 = jb.getText().toUpperCase();
				} else
					if (left != null && left instanceof JLabel) {
						JLabel jb = (JLabel) left;
						c1 = jb.getText().toUpperCase();
					} else
						if (left != null && left instanceof JComponent) {
							StringBuilder sb = new StringBuilder();
							getSubText(left, sb);
							c1 = sb.toString().toUpperCase();
						}
				if (right != null && right instanceof AbstractButton) {
					AbstractButton jb = (AbstractButton) right;
					c2 = jb.getText().toUpperCase();
				} else
					if (right != null && right instanceof JLabel) {
						JLabel jb = (JLabel) right;
						c2 = jb.getText().toUpperCase();
					} else
						if (right != null && right instanceof JComponent) {
							StringBuilder sb = new StringBuilder();
							getSubText(right, sb);
							c2 = sb.toString().toUpperCase();
						}
				if (c1.length() <= 0 && c2.length() <= 0)
					return true;
				return c2.contains(searchText) || c1.contains(searchText);
			}
			
			private void getSubText(JComponent c, StringBuilder sb) {
				for (Component jc : c.getComponents()) {
					if (jc instanceof JLabel)
						sb.append("/" + ((JLabel) jc).getText());
					else
						if (jc instanceof JButton)
							sb.append("/" + ((JButton) jc).getText());
						else
							if (jc instanceof JComponent)
								getSubText((JComponent) jc, sb);
					
				}
			}
			
			private JComponent findMyComponent(JComponent jc) {
				if (jc instanceof JPanel) {
					JPanel jp = (JPanel) jc;
					return (JComponent) jp.getComponent(0);
				} else
					return jc;
			}
		};
	}
	
	public static ImageIcon getLeftOrRightIcon(boolean left) {
		ClassLoader cl = FolderPanel.class.getClassLoader();
		String path = FolderPanel.class.getPackage().getName().replace('.', '/') + "/images";
		if (left)
			return new ImageIcon(cl.getResource(path + "/large_left.png"));
		else
			return new ImageIcon(cl.getResource(path + "/large_right.png"));
	}
	
	public int getMaxRowCount() {
		return this.maxRowCount;
	}
	
	public int getCurrentPage() {
		return this.currentPage;
	}
	
	public void setCurrentPage(int page) {
		// page valid?
		int maxPage = this.guiComponentRows.size() / this.maxRowCount - ((this.guiComponentRows.size() % this.maxRowCount == 0) ? 1 : 0);
		if (maxPage < page)
			// page too big
			page = maxPage;
		
		if (page < 0 || this.currentPage == page)
			// invalid or redundant
			return;
		
		// set it!
		this.currentPage = page;
		this.layoutRows();
		
		for (ActionListener al : collapse_listeners)
			al.actionPerformed(new ActionEvent(this, condensedState ? 0 : 1, "collapseevent"));
		
		// all OK!
	}
}
