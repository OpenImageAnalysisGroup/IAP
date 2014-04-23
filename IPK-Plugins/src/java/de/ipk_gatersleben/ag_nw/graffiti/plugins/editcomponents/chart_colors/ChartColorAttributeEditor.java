/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 04.11.2004 by Christian Klukas
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_colors;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.AttributeHelper;
import org.Colors;
import org.ErrorMsg;
import org.graffiti.editor.MainFrame;
import org.graffiti.plugin.Displayable;
import org.graffiti.plugin.editcomponent.AbstractValueEditComponent;

import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.dbe.algorithms.RecolorEdgesAlgorithm;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public class ChartColorAttributeEditor extends AbstractValueEditComponent {
	private JPanel jpanel;
	
	private JButton qsButton;
	
	private int barCount = -1;
	
	public ChartColorAttributeEditor(Displayable disp) {
		super(disp);
		qsButton = getQuickSetButton();
		setGUI((ChartColorAttribute) disp, false);
	}
	
	private JButton getQuickSetButton() {
		JButton res = new JButton("P");
		res.setToolTipText("Select Preset (Quick-Set Colors)");
		res.setOpaque(false);
		res.setMinimumSize(new Dimension(40, 10));
		res.setPreferredSize(new Dimension(40, 10));
		res.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				ChartColorAttribute cca = (ChartColorAttribute) getDisplayable();
				int numberOfColors = cca.getDefinedBarCount();
				if (numberOfColors <= 0) {
					MainFrame.showMessageDialog("Number of lines is too low (0). Editing colors not possible.", "Information");
					return;
				}
				ArrayList<String> knownPresets = new ArrayList<String>();
				String presetDiff = "Different Colors (intensity 1)";
				String presetDiffLess1 = "Different Colors (intensity 0.7)";
				String presetDiffLess2 = "Different Colors (intensity 0.5)";
				String presetDiffLess3 = "Different Colors (intensity 0.3)";
				String presetFromTo = "Color Stripe";
				knownPresets.add(presetFromTo);
				knownPresets.add(presetDiff);
				knownPresets.add(presetDiffLess1);
				knownPresets.add(presetDiffLess2);
				knownPresets.add(presetDiffLess3);
				Object[] inp = MyInputHelper.getInput("Select a preset:", "Color Presets (" + numberOfColors + " lines)",
									new Object[] {
														"Presets", knownPresets,
														"Stripe/Start", Color.BLACK,
														"Stripe/End", Color.LIGHT_GRAY,
														"Set Line/Fill", true,
														"Set Outline", false
				});
				if (inp != null) {
					String cmd = (String) inp[0];
					ArrayList<Color> colors = null;
					if (cmd.equals(presetDiff))
						colors = Colors.get(numberOfColors, 1);
					if (cmd.equals(presetDiffLess1))
						colors = Colors.get(numberOfColors, 0.7);
					if (cmd.equals(presetDiffLess2))
						colors = Colors.get(numberOfColors, 0.5);
					if (cmd.equals(presetDiffLess3))
						colors = Colors.get(numberOfColors, 0.3);
					if (cmd.equals(presetFromTo)) {
						colors = new ArrayList<Color>();
						Color c1 = (Color) (inp[1]);
						Color c2 = (Color) (inp[2]);
						for (int i = 0; i < numberOfColors; i++) {
							float f;
							if (numberOfColors > 1)
								f = ((float) i) / (numberOfColors - 1);
							else
								f = 0.5f;
							Color cc = RecolorEdgesAlgorithm.getColor(f, 1, c1, c2);
							colors.add(cc);
						}
					}
					boolean setForeground = (Boolean) inp[3];
					boolean setBackground = (Boolean) inp[4];
					if (colors != null && colors.size() > 0) {
						for (int i = 0; i < jpanel.getComponentCount(); i++) {
							Object o = jpanel.getComponent(i);
							if (o instanceof JLabel) {
								JLabel jb = (JLabel) o;
								Boolean bar = (Boolean) jb.getClientProperty("isBar");
								Integer barIndex = (Integer) jb.getClientProperty("barIndex");
								if (bar != null && bar.booleanValue()) {
									Color c1 = colors.get(barIndex.intValue());
									if (setForeground) {
										jb.setBackground(c1);
										jb.setForeground(c1);
										jb.setText("#");
									}
								} else
									if (barIndex != null) {
										Color c1 = colors.get(barIndex.intValue());
										if (setBackground) {
											jb.setBackground(c1);
											jb.setForeground(c1);
											jb.setText("#");
										}
									}
							}
						}
					}
				}
			}
		});
		return res;
	}
	
	private void setGUI(ChartColorAttribute cca, boolean showEmpty) {
		jpanel = new JPanel();
		
		double[][] size = new double[2][];
		barCount = cca.getDefinedBarCount();
		if (barCount <= 0) {
			jpanel.setLayout(new GridLayout(1, 1));
			jpanel.add(new JLabel("No dataset visible"));
		} else {
			size[0] = new double[barCount];
			size[1] = new double[2];
			for (int x = 0; x < barCount; x++)
				size[0][x] = TableLayoutConstants.FILL;
			size[1][0] = TableLayoutConstants.PREFERRED; // TableLayoutConstants.FILL;
			size[1][1] = TableLayoutConstants.PREFERRED; // TableLayoutConstants.FILL;
			
			jpanel.setLayout(new TableLayout(size));
			ArrayList<Color> barCols = null;
			ArrayList<Color> barOutlineCols = null;
			try {
				barCols = cca.getSeriesColors(cca.getIdList(barCount));
				barOutlineCols = cca.getSeriesOutlineColors(cca.getIdList(barCount));
			} catch (Exception e) {
				ErrorMsg.addErrorMessage(e);
			}
			for (int i = 0; i < barCount; i++) {
				JLabel colButton = new JLabel();
				JLabel colButtonOutline = new JLabel();
				try {
					if (barCols != null && barCols.get(i) != null && !showEmpty) {
						colButton.setBackground(barCols.get(i));
						colButton.setForeground(barCols.get(i));
						colButton.setText("#");
					} else
						colButton.setText(EMPTY_STRING);
					addDefaultColorActionListenerAndAddBarInfo(colButton, true, cca.getIdList(barCount).get(i));
					colButton.putClientProperty("isBar", new Boolean(true));
					colButton.putClientProperty("barIndex", new Integer(i));
					colButton.setToolTipText("Line/Bar-Color: " + cca.getIdList(barCount).get(i) + ": " + AttributeHelper.getColorName(barCols.get(i)));
					
					if (barOutlineCols != null && barOutlineCols.get(i) != null && !showEmpty) {
						colButtonOutline.setBackground(barOutlineCols.get(i));
						colButtonOutline.setForeground(barOutlineCols.get(i));
						colButtonOutline.setText("#");
					} else
						colButtonOutline.setText(EMPTY_STRING);
					
					colButtonOutline.setToolTipText("Outline-Color: " + cca.getIdList(barCount).get(i) + ": " + AttributeHelper.getColorName(barOutlineCols.get(i)));
					
					addDefaultColorActionListenerAndAddBarInfo(colButtonOutline, false, cca.getIdList(barCount).get(i));
					colButtonOutline.putClientProperty("isBar", new Boolean(false));
					colButtonOutline.putClientProperty("barIndex", new Integer(i));
				} catch (Exception e) {
					ErrorMsg.addErrorMessage(e);
				}
				jpanel.add(colButton, i + ",0");
				
				jpanel.add(colButtonOutline, i + ",1");
			}
		}
		// jpanel.setMaximumSize(new Dimension(2000, 40));
		jpanel.validate();
	}
	
	private void addDefaultColorActionListenerAndAddBarInfo(final JLabel colorButton, final boolean barColor, final String bar) {
		colorButton.setOpaque(true);
		colorButton.setBorder(BorderFactory.createRaisedBevelBorder());
		colorButton.addMouseListener(new MouseListener() {
			public void mouseClicked(MouseEvent e) {
				JLabel src = (JLabel) e.getSource();
				Color c;
				if (barColor)
					c = JColorChooser.showDialog(MainFrame.getInstance(), "Select the (bar/line) color of dataset " + bar, src.getBackground());
				else
					c = JColorChooser.showDialog(MainFrame.getInstance(), "Select the outline-color (bar charts) of dataset " + bar, src.getBackground());
				if (c != null) {
					src.setBackground(c);
					src.setForeground(c);
					src.setText("#");
					if (barColor)
						src.setToolTipText("Line/Bar-Color: " + bar + ": " + AttributeHelper.getColorName(c));
					else
						src.setToolTipText("Outline-Color: " + bar + ": " + AttributeHelper.getColorName(c));
				}
			}
			
			public void mousePressed(MouseEvent arg0) {
			}
			
			public void mouseReleased(MouseEvent arg0) {
			}
			
			public void mouseEntered(MouseEvent arg0) {
				colorButton.setBorder(BorderFactory.createLineBorder(Color.BLUE, 1));
			}
			
			public void mouseExited(MouseEvent arg0) {
				colorButton.setBorder(BorderFactory.createRaisedBevelBorder());
			}
		});
	}
	
	public JComponent getComponent() {
		return TableLayout.getSplit(qsButton, jpanel, TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL);
	}
	
	public void setEditFieldValue() {
		ChartColorAttribute cca = (ChartColorAttribute) getDisplayable();
		for (int i = 0; i < jpanel.getComponentCount(); i++) {
			Object o = jpanel.getComponent(i);
			if (o instanceof JLabel) {
				JLabel jb = (JLabel) o;
				if (jb.getText().equals(EMPTY_STRING))
					continue;
				Boolean bar = (Boolean) jb.getClientProperty("isBar");
				Integer barIndex = (Integer) jb.getClientProperty("barIndex");
				if (bar != null && bar.booleanValue())
					cca.setSeriesColor(barIndex.intValue(), jb.getBackground());
				else
					if (barIndex != null)
						cca.setSeriesOutlineColor(barIndex.intValue(), jb.getBackground());
			}
		}
	}
	
	public void setValue() {
		ChartColorAttribute cca = (ChartColorAttribute) getDisplayable();
		for (int i = 0; i < jpanel.getComponentCount(); i++) {
			Object o = jpanel.getComponent(i);
			if (o instanceof JLabel) {
				JLabel jb = (JLabel) o;
				if (jb.getText().equals(EMPTY_STRING))
					continue;
				Boolean bar = (Boolean) jb.getClientProperty("isBar");
				Integer barIndex = (Integer) jb.getClientProperty("barIndex");
				if (bar == null || barIndex == null)
					continue;
				if (bar.booleanValue())
					cca.setSeriesColor(barIndex.intValue(), jb.getBackground());
				else
					cca.setSeriesOutlineColor(barIndex.intValue(), jb.getBackground());
			}
		}
	}
}
