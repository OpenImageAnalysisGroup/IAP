package placement;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;

import javax.swing.JPanel;

public class DebugPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Constraints constraints;
	
	private Blocks blocks;
	
	public DebugPanel(Blocks blocks, Constraints constraints) {
		super();
		this.blocks = blocks;
		this.constraints = constraints;
	}
	
	@Override
	synchronized public void paintComponent(Graphics gOld) {
		Graphics2D g = (Graphics2D) gOld;
		Color original = g.getColor();
		g.setColor(getBackground());
		Dimension size = getSize();
		g.fillRect(0, 0, size.width, size.height);
		g.setColor(original);
		super.paintChildren(g);
		yLookup.clear();
		Variables allVars = blocks.getAllVariables();
		int y = 0, ystep = getSize().height / (allVars.size());
		float h = 0, hstep = 1f / 7f;
		double xMin = allVars.getMinPos(), xMax = allVars.getMaxPos();
		double xRange = xMax - xMin;
		double xScale = (getSize().width - 20) / xRange;
		Block b = blocks.head;
		int vcounter = 0;
		while (b != null) {
			Color c = Color.WHITE;
			if (b.variables.size() > 1) {
				c = Color.getHSBColor(h, 0.5f, 1f);
				h += hstep;
			}
			for (Variable v : b.variables) {
				int x = (int) ((v.getPosition() - xMin) * xScale);
				int w = 20;
				try {
					w = (int) (((Rectangle) v.data.get(Rectangle2D.class)).getWidth() * xScale);
				} catch (NullPointerException e) {
					// variable is not associated with a Rectangle
				}
				Rectangle r = new Rectangle(x, y, w, ystep);
				yLookup.put(v, y);
				v.colour = c;
				g.setPaint(c);
				g.fill(r);
				g.setPaint(Color.BLACK);
				g.draw(r);
				g.drawString(v.name, x, y + 10);
				y += ystep;
				vcounter++;
			}
			b = b.nextRight;
		}
		for (Constraint c : constraints) {
			Variable l = c.left, r = c.right;
			int xl = (int) ((l.getPosition() - xMin) * xScale);
			int xr = (int) ((r.getPosition() - xMin) * xScale);
			c.colour = Color.BLUE;
			if (c.isViolated()) {
				c.colour = Color.RED;
			} else
				if (c.isTight()) {
					c.colour = Color.GREEN;
				}
			g.setPaint(c.colour);
			g.drawLine(xl, yLookup.get(l), xr, yLookup.get(r));
			g.drawOval(xr - 5, yLookup.get(r) - 5, 10, 10);
		}
	}
	
	public void updateDrawing() {
		paintComponent(getGraphics());
	}
	
	Hashtable<Variable, Integer> yLookup = new Hashtable<Variable, Integer>();
	
}
