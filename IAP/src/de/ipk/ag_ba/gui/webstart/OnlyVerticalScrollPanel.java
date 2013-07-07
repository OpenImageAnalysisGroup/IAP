package de.ipk.ag_ba.gui.webstart;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Rectangle;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;

/**
 * A panel that, when placed in a {@link JScrollPane}, only scrolls vertically and resizes horizontally as needed.
 */
public class OnlyVerticalScrollPanel extends JPanel implements Scrollable {
	private static final long serialVersionUID = 1L;
	private Component comp;
	
	public OnlyVerticalScrollPanel() {
		this(new GridLayout(0, 1));
	}
	
	public OnlyVerticalScrollPanel(LayoutManager lm) {
		super(lm);
	}
	
	public OnlyVerticalScrollPanel(Component comp) {
		this();
		this.comp = comp;
		add(comp);
	}
	
	@Override
	public Dimension getPreferredScrollableViewportSize() {
		return (getPreferredSize());
	}
	
	@Override
	public int getScrollableUnitIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return (10);
	}
	
	@Override
	public int getScrollableBlockIncrement(Rectangle visibleRect,
			int orientation, int direction) {
		return (100);
	}
	
	@Override
	public boolean getScrollableTracksViewportWidth() {
		return true;
	}
	
	@Override
	public boolean getScrollableTracksViewportHeight() {
		return false;
	}
}