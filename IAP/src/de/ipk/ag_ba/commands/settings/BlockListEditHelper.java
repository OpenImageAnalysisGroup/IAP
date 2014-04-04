package de.ipk.ag_ba.commands.settings;

import iap.blocks.data_structures.ImageAnalysisBlock;
import info.clearthought.layout.TableLayout;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.font.TextAttribute;
import java.text.AttributedString;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import org.GuiRow;
import org.MarkComponent;
import org.ObjectRef;
import org.StringManipulationTools;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugins.editcomponents.ComponentBorder;
import org.graffiti.plugins.editcomponents.ComponentBorder.Edge;

import de.ipk.ag_ba.gui.images.IAPimages;

public class BlockListEditHelper {
	
	public static String getHelpText() {
		return "The input and output information boxes (IN/OUT) indicate whether a block (potentially) processes<br>" +
				"visible light images, fluorescence images, near-infrared and/or infrared images (in this order).<br>" +
				"If the box is filled, the block processes images from a certain type as input and/or output.<br>" +
				"If it is not filled, the corresponding image is not processed (for IN) or remains unchanged (OUT).<br>" +
				"All of this informtion is derived from static meta-data. Depending on availability of input<br>" +
				"images and block settings, images from certain camera types are not processed by the blocks.<br><br>";
	}
	
	public static void installEditButton(ArrayList<Object> entries, String blockName, String blockDesc, final JLabel leftLabel,
			String sl, final int startLine) {
		final ObjectRef inFocus = new ObjectRef();
		inFocus.setObject(false);
		
		final Font font = new JButton().getFont().deriveFont(Font.BOLD);
		final JTextField textField = new JTextField(sl + "") {
			String lastText = "-1";
			String blockName = null;
			boolean err = false;
			
			@Override
			public void paint(Graphics g) {
				String slinp = getText();
				if (!lastText.equals(slinp)) {
					lastText = slinp;
					String infRes = "";
					boolean first = true;
					if (slinp.startsWith("//") && !slinp.equals("//")) {
						infRes = "<html><small>[A new empty line will be<br>added before this block]</small><br>";
						blockName = blockName.split(" | ")[0] + " | [NEW LINE BEFORE]";
					}
					if (slinp.endsWith("//") && !slinp.equals("//")) {
						infRes = "<html><small>[A new empty line will be<br>added " +
								(slinp.startsWith("//") ? "before and " : "")
								+ "after this block]</small><br>";
						blockName = blockName.split(" | ")[0] + " | [NEW LINE " +
								(slinp.startsWith("//") ? "BEFORE AND " : "") + "BELOW]";
					}
					if (slinp.equals("//")) {
						infRes = "<html><small>[This block will be removed]</small>";
						blockName = StringManipulationTools.removeHTMLtags(infRes);
					}
					for (String sl : slinp.split("//")) {
						if (sl.isEmpty())
							continue;
						String inf = "Step " + (startLine);
						if (startLine + 1 <= 10)
							inf = "Step 0" + (startLine + 1 - 1);
						if (!first)
							inf = "Step --- (new)";
						first = false;
						try {
							ImageAnalysisBlock inst = (ImageAnalysisBlock) Class.forName(sl).newInstance();
							blockName = inst.getName();
							String blockDesc = inst.getDescription();
							setToolTipText("<html>" + StringManipulationTools.getWordWrap(blockDesc, 60));
							inf = "<html>" + BlockSelector.getBlockDescriptionAnnotation(inf, inst);
							err = false;
						} catch (NoClassDefFoundError e2) {
							blockName = sl;
							if (sl != null && sl.startsWith("#")) {
								try {
									String s = sl.substring(1);
									ImageAnalysisBlock inst = (ImageAnalysisBlock) Class.forName(s).newInstance();
									blockName = inst.getName();
									String blockDesc = inst.getDescription();
									setToolTipText("<html>" + StringManipulationTools.getWordWrap(blockDesc, 60));
									inf = "<html><font color='gray'>" + inf + "<br><small>disabled</small></font>";
									inf = "<html><font color='gray'>" + BlockSelector.getBlockDescriptionAnnotation(inf, inst);
									err = false;
								} catch (Error err) {
									inf = "<html>" + inf + "<br>[Unknown Disabled Block]";
								} catch (Exception err) {
									inf = "<html>" + inf + "<br>[Unknown Disabled Block]";
								}
							} else
								inf = "<html>" + inf + "<br>[Unknown Block]";
							
							err = true;
						} catch (Exception e) {
							blockName = sl;
							if (sl != null && sl.startsWith("#")) {
								try {
									String s = sl.substring(1);
									ImageAnalysisBlock inst = (ImageAnalysisBlock) Class.forName(s).newInstance();
									blockName = inst.getName();
									String blockDesc = inst.getDescription();
									setToolTipText("<html>" + StringManipulationTools.getWordWrap(blockDesc, 60));
									inf = "<html><font color='gray'>" + inf + "<br><small>disabled</small></font>";
									inf = "<html><font color='gray'>" + BlockSelector.getBlockDescriptionAnnotation(inf, inst);
									err = false;
								} catch (Error err) {
									inf = "<html>" + inf + "<br>[Unknown Disabled Block]";
								} catch (Exception err) {
									inf = "<html>" + inf + "<br>[Unknown Disabled Block]";
								}
							} else
								inf = "<html>" + inf + "<br>[Unknown Block]";
							err = true;
						}
						infRes += inf;
					}
					infRes = "<html>" + StringManipulationTools.stringReplace(infRes, "<html>", "");
					leftLabel.setText(infRes);
					leftLabel.repaint();
				}
				if (!(Boolean) inFocus.getObject() && blockName != null) {
					Graphics2D graphics2d = (Graphics2D) g;
					graphics2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
					g.setColor(new JDialog().getBackground());
					g.fillRect(0, 0, getWidth(), getHeight());
					g.setColor(Color.WHITE);// new JDialog().getBackground());
					g.fillRect(2, 0, getWidth() - 4, getHeight() - 4);
					g.setColor(Color.BLACK);
					g.drawRoundRect(2, 0, getWidth() - 4, getHeight() - 4, 0, 0);// 5, 5);
					g.setColor(Color.BLACK);
					if (blockName != null && !blockName.isEmpty()) {
						AttributedString str_attribut = new AttributedString(blockName);
						str_attribut.addAttribute(TextAttribute.FONT, font);
						if (err)
							str_attribut.addAttribute(TextAttribute.STRIKETHROUGH, TextAttribute.STRIKETHROUGH_ON, 0, blockName.length());
						g.drawString(str_attribut.getIterator(), 8, getHeight() / 2 + 2);
					}
				} else
					super.paint(g);
			}
			
		};
		
		final String MSG_CLOSE_WINDOW = "<html><b>After confirming this information window:</b><br><br>"
				+ "Click <b>OK</b> and reopen the editor to confirm this change<br><br>"
				+ "or click <b>Cancel</b> to revert this change.";
		
		Action ba = new AbstractAction("...") {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				final JPopupMenu popup = new JPopupMenu();
				
				if (!textField.getText().isEmpty() && !textField.getText().equals("//"))
					if (!textField.getText().startsWith("#"))
					{
						JMenuItem menuItem = new JMenuItem("Disable this block",
								(new ImageIcon(IAPimages.getImage("img/ext/gpl2/Glade-3-64_disabled.png").getScaledInstance(24, 24,
										Image.SCALE_SMOOTH))));
						menuItem.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								textField.setText("#" + textField.getText());
								MainFrame.showMessageDialog(MSG_CLOSE_WINDOW, "Further action needed");
							}
						});
						popup.add(menuItem);
					} else {
						JMenuItem menuItem = new JMenuItem("Enable this block",
								(new ImageIcon(IAPimages.getImage("img/ext/gpl2/Glade-3-64.png").getScaledInstance(24, 24,
										Image.SCALE_SMOOTH))));
						menuItem.addActionListener(new ActionListener() {
							@Override
							public void actionPerformed(ActionEvent e) {
								textField.setText(textField.getText().substring("#".length()));
								MainFrame.showMessageDialog(MSG_CLOSE_WINDOW, "Further action needed");
							}
						});
						popup.add(menuItem);
					}
				{
					JMenuItem menuItem = new JMenuItem("Select new block...",
							(new ImageIcon(IAPimages.getImage("img/ext/gpl2/Gnome-Applications-Science-64.png").getScaledInstance(24, 24,
									Image.SCALE_SMOOTH))));
					menuItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							final MarkComponent mc = (MarkComponent) textField.getClientProperty("markComponent");
							if (mc != null)
								mc.setMark(true);
							TextReceiver resultReceiver = new TextReceiver() {
								@Override
								public void setText(String result) {
									if (result != null) {
										textField.setText(result);
									}
								}
							};
							ImageAnalysisBlock currentSelection;
							try {
								currentSelection = (ImageAnalysisBlock) Class.forName(textField.getText()).newInstance();
							} catch (Error err) {
								currentSelection = null;
							} catch (Exception err) {
								currentSelection = null;
							}
							new BlockTypeSelector(
									"Select Analysis Block",
									"Select the block type:",
									resultReceiver, currentSelection).showDialog();
							mc.setMark(false);
						}
					});
					popup.add(menuItem);
				}
				if (!textField.getText().equals("//"))
				{
					JMenuItem menuItem = new JMenuItem("Remove this line",
							(new ImageIcon(IAPimages.getImage("img/ext/gpl2/Gnome-Edit-Clear-64.png").getScaledInstance(24, 24,
									Image.SCALE_SMOOTH))));
					menuItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							textField.setText("//");
							MainFrame.showMessageDialog(MSG_CLOSE_WINDOW, "Further action needed");
						}
					});
					popup.add(menuItem);
				}
				if (!textField.getText().isEmpty() && !textField.getText().equals("//"))
				{
					JMenuItem menuItem = new JMenuItem("Add new above",
							(new ImageIcon(IAPimages.getImage("img/ext/gpl2/Gnome-View-Sort-Ascending-64.png").getScaledInstance(24, 24,
									Image.SCALE_SMOOTH))));
					menuItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							textField.setText("//" + textField.getText());
							MainFrame.showMessageDialog(MSG_CLOSE_WINDOW, "Further action needed");
						}
					});
					popup.add(menuItem);
				}
				if (!textField.getText().isEmpty() && !textField.getText().equals("//"))
				{
					JMenuItem menuItem = new JMenuItem("Add new below",
							(new ImageIcon(IAPimages.getImage("img/ext/gpl2/Gnome-View-Sort-Descending-64.png").getScaledInstance(24, 24,
									Image.SCALE_SMOOTH))));
					menuItem.addActionListener(new ActionListener() {
						@Override
						public void actionPerformed(ActionEvent e) {
							textField.setText(textField.getText() + "//");
							MainFrame.showMessageDialog(MSG_CLOSE_WINDOW, "Further action needed");
						}
					});
					popup.add(menuItem);
				}
				JButton b = (JButton) e.getSource();
				popup.show(b, 0, 0);
				
			}
		};
		final JButton selButton = new JButton(ba);
		selButton.setVisible(false);
		if (blockDesc != null)
			textField.setToolTipText("<html>" +
					StringManipulationTools.getWordWrap(blockDesc, 60));
		
		textField.addFocusListener(new FocusListener() {
			@Override
			public void focusLost(FocusEvent e) {
				inFocus.setObject(false);
				selButton.setVisible(false);
				textField.repaint();
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				inFocus.setObject(true);
				selButton.setVisible(true);
				textField.repaint();
			}
		});
		
		selButton.setToolTipText("Modify Item");
		ComponentBorder cb = new ComponentBorder(selButton, Edge.RIGHT) {
			
			@Override
			public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
				if ((Boolean) inFocus.getObject())
					super.paintBorder(c, g, x, y, width, height);
			}
		};
		cb.install(textField);
		MarkComponent mc = new MarkComponent(textField, false, TableLayout.FILL, false, 3);
		textField.putClientProperty("markComponent", mc);
		GuiRow gr = new GuiRow(leftLabel, mc);
		gr.setSecondComponentVerticalAlignedMiddle(true);
		entries.add(gr.getRowGui());// + "");
	}
}
