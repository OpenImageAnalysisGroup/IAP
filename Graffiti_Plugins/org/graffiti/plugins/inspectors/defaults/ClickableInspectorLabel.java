package org.graffiti.plugins.inspectors.defaults;

import info.clearthought.layout.TableLayout;

import java.awt.Cursor;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.graffiti.editor.GravistoService;

public class ClickableInspectorLabel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private final Runnable executeOnClick;
	private final JLabel textlabel, iconlabel;
	private static ImageIcon icon = new ImageIcon(GravistoService.getResource(ClickableInspectorLabel.class, "deleteCross.png"));
	
	public ClickableInspectorLabel(String text, final Runnable executeOnClick) {
		textlabel = new JLabel(text);
		textlabel.setIcon(null);
		textlabel.setHorizontalTextPosition(SwingConstants.LEADING);
		textlabel.setHorizontalAlignment(SwingConstants.LEADING);
		this.executeOnClick = executeOnClick;
		iconlabel = new JLabel();
		iconlabel.setToolTipText("Click to delete attribute(s)");
		addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				iconlabel.setIcon(null);
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				iconlabel.setIcon(icon);
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
			}
		});
		iconlabel.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) {
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				iconlabel.setIcon(null);
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				iconlabel.setIcon(icon);
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				executeOnClick.run();
			}
		});
		Cursor c = new Cursor(Cursor.HAND_CURSOR);
		iconlabel.setCursor(c);
		
		setLayout(TableLayout.getLayout(new double[] { TableLayout.PREFERRED, 5, TableLayout.PREFERRED }, TableLayout.PREFERRED));
		add(textlabel, "0,0");
		add(iconlabel, "2,0");
		setOpaque(false);
	}
	
	public ClickableInspectorLabel(String text, ClickableInspectorLabel copy) {
		this(text, copy.executeOnClick);
	}
	
	public JLabel getLabel() {
		return textlabel;
	}
	
}
