/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/

package org;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class MarkComponent extends JComponent {
	private static final long serialVersionUID = 1L;
	
	JLabel b1 = new JLabel();
	JLabel b2 = new JLabel();
	JLabel bb1 = new JLabel();
	JLabel bb2 = new JLabel();
	Color selCol = new Color(180, 180, 255);
	Color selColBB = null;
	JComponent comp;
	private boolean marked;
	
	private boolean requestFocus;
	
	private double intensity = 1d;
	
	public MarkComponent(JComponent comp, boolean marked, double width, boolean requestFocus, double layoutHeight) {
		
		this.comp = comp;
		this.requestFocus = requestFocus;
		
		setLayout(TableLayout.getLayout(new double[] { 5, 1, width, 1, 5 }, layoutHeight));
		add(b1, "0,0");
		add(bb1, "1,0");
		add(comp, "2,0");
		add(bb2, "3,0");
		add(b2, "4,0");
		this.marked = marked;
		updateMarked();
	}
	
	public MarkComponent(JComponent comp, boolean marked, double width, boolean requestFocus) {
		
		this.comp = comp;
		this.requestFocus = requestFocus;
		
		setLayout(TableLayout.getLayout(new double[] { 5, 1, width, 1, 5 }, TableLayoutConstants.PREFERRED));
		add(b1, "0,0");
		add(bb1, "1,0");
		add(comp, "2,0");
		add(bb2, "3,0");
		add(b2, "4,0");
		this.marked = marked;
		updateMarked();
	}
	
	public void setRequestFocus(boolean requestFocus) {
		this.requestFocus = requestFocus;
	}
	
	public void playAnimation(int animationtime) {
		setMark(true);
		final MarkComponent thisM = this;
		final Timer t = new Timer(50, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!thisM.isVisible()) {
					thisM.intensity = 1;
					// ((Timer)e.getSource()).stop();
				} else {
					if (marked) {
						intensity += 0.02;
						if (intensity > 1)
							intensity = -1;
					} else
						intensity = 1;
				}
				updateMarked();
			}
		});
		t.start();
		Timer t2 = new Timer(animationtime, new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				t.stop();
				intensity = 1;
				setMark(false);
			}
		});
		t2.setRepeats(false);
		t2.start();
	}
	
	public boolean isMarked() {
		return marked;
	}
	
	private void updateMarked() {
		b1.setOpaque(marked);
		b2.setOpaque(marked);
		bb1.setOpaque(marked);
		bb2.setOpaque(marked);
		if (marked) {
			Color c1 = selCol;
			Color c2 = selColBB;
			float i = intensity > 0 ? (float) intensity : (float) -intensity;
			if (i < 1d) {
				// System.out.println(i);
				if (c1 != null) {
					Color c11 = Colors.getOppositeColor(c1);
					// Color c11 = c1.darker(); // brighter();
					c1 = Colors.getColor(i, 1d, c11, c1);
				}
				if (c2 != null) {
					// Color c22 = Colors.getOppositeColor(c2);
					Color c22 = c2.brighter();
					c1 = Colors.getColor(i, 1d, c22, c2);
				}
			}
			b1.setBackground(c1);
			b2.setBackground(c1);
			bb1.setBackground(c2);
			bb2.setBackground(c2);
		} else {
			b1.setBackground(null);
			b2.setBackground(null);
			bb1.setBackground(null);
			bb2.setBackground(null);
		}
		if (isVisible()) {
			if (SwingUtilities.isEventDispatchThread()) {
				b1.repaint();
				b2.repaint();
				bb1.repaint();
				bb2.repaint();
			} else {
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						b1.repaint();
						b2.repaint();
						bb1.repaint();
						bb2.repaint();
					}
				});
			}
		}
	}
	
	public void setMarkColor(Color c, Color gapColor) {
		this.selCol = c;
		this.selColBB = gapColor;
		updateMarked();
	}
	
	public void setMark(final boolean markedReq) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				if (marked == markedReq)
					return;
				marked = markedReq;
				updateMarked();
				if (marked && requestFocus)
					comp.requestFocusInWindow();
			}
		});
	}
	
	public static void initLinearMarkSequence(final MarkComponent markGotoFromEnd, final MarkComponent... mark) {
		final ArrayList<MarkComponent> mcs = new ArrayList<MarkComponent>();
		for (MarkComponent mc : mark) {
			mcs.add(mc);
		}
		for (int i = 0; i < mcs.size(); i++) {
			MarkComponent mc = mcs.get(i);
			Component c = mc.comp;
			if (c instanceof JComboBox) {
				JComboBox jc = (JComboBox) c;
				final int ti = i;
				jc.addActionListener(getUpdateCommand(mcs, ti, markGotoFromEnd, mark));
				
			}
			if (c instanceof JCheckBox) {
				JCheckBox jc = (JCheckBox) c;
				final int ti = i;
				jc.addActionListener(getUpdateCommand(mcs, ti, markGotoFromEnd, mark));
				
			}
			if (c instanceof JButton) {
				JButton jb = (JButton) c;
				final int ti = i;
				jb.addActionListener(getUpdateCommand(mcs, ti, markGotoFromEnd, mark));
			}
		}
	}
	
	private static boolean invokePending = false;
	
	private static ActionListener getUpdateCommand(final ArrayList<MarkComponent> mcs, final int ti, final MarkComponent jump, final MarkComponent... mark) {
		return new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				invokePending = true;
				SwingUtilities.invokeLater(new Runnable() {
					public void run() {
						if (!invokePending)
							return;
						try {
							int idx = ti + 1;
							if (idx >= mcs.size())
								idx = mcs.indexOf(jump);
							if (idx < 0)
								idx = 0;
							MarkComponent.markComponent(mcs.get(idx), mark);
						} finally {
							invokePending = false;
						}
					}
				});
			}
		};
	}
	
	protected static void markComponent(
						MarkComponent markThis,
						MarkComponent... allMarks) {
		for (MarkComponent mc : allMarks) {
			if (mc != markThis)
				mc.setMark(false);
			else
				mc.setMark(true);
		}
	}
}
