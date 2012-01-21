package de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.label_alignment;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.JLabel;

import org.AlignmentSetting;
import org.graffiti.graphics.GraphicAttributeConstants;

public class PositionButton extends JLabel {
	
	private static final long serialVersionUID = -179271224448469146L;
	
	private AlignmentSetting alignment;
	
	private boolean active = false;
	private boolean mark = false;
	private boolean isNodeBorder = false;
	
	public PositionButton(final LabelAlignmentAttributeEditor ae, AlignmentSetting align, String desc, final boolean border) {
		this.alignment = align;
		this.isNodeBorder = border;
		setToolTipText(desc);
		setOpaque(true);
		setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));
		setPreferredSize(new Dimension(ae.www, ae.hhh));
		setMinimumSize(new Dimension(ae.www, ae.hhh));
		setSize(new Dimension(ae.www, ae.hhh));
		addMouseListener(new MouseListener() {
			
			public void mouseClicked(MouseEvent e) {
				ae.setShowEmpty(false);
				if (ae.currentSelection.equals(alignment.toGMLstring()))
					ae.currentSelection = GraphicAttributeConstants.AUTO_OUTSIDE;
				else
					ae.currentSelection = alignment.toGMLstring();
				ae.updateButtonState();
			}
			
			public void mouseEntered(MouseEvent e) {
				mark = true;
				checkColor();
			}
			
			public void mouseExited(MouseEvent e) {
				mark = false;
				checkColor();
			}
			
			public void mousePressed(MouseEvent e) {
			}
			
			public void mouseReleased(MouseEvent e) {
			}
		});
		checkColor();
	}
	
	public String getAlignmentSetting() {
		return alignment.toGMLstring();
	}
	
	public void setSelected(boolean active) {
		this.active = active;
		checkColor();
	}
	
	public void checkColor() {
		if (mark && !active) {
			setBackground(new Color(200, 200, 255));
		} else
			if (active) {
				if (alignment == AlignmentSetting.HIDDEN) {
					setBackground(new Color(230, 230, 230));
				} else {
					setBackground(new Color(100, 100, 255));
				}
			} else {
				if (isNodeBorder) {
					setBackground(new Color(200, 200, 200));
				} else {
					if (alignment == AlignmentSetting.HIDDEN)
						setBackground(new Color(255, 255, 255));
					else
						setBackground(new Color(230, 230, 230));
				}
			}
		repaint();
	}
}
