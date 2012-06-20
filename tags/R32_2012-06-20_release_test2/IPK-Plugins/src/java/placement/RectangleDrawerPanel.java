package placement;

/*
 * A simple program where the user can sketch curves in a variety of
 * colors. A color palette is shown on the right of the applet.
 * The user can select a drawing color by clicking on a color in the
 * palette. Under the colors is a "Clear button" that the user
 * can press to clear the sketch. The user draws by clicking and
 * dragging in a large white area that occupies most of the applet.
 * The user's drawing is not persistant. It is cleared if the
 * applet is resized. If it is covered, in whole or part, and
 * then uncovered, the part was covered is gone.
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.event.MouseInputListener;
import javax.swing.filechooser.FileFilter;

class BlocksFileFilter extends FileFilter {
	public File lastSelectedFile = null;
	
	@Override
	public boolean accept(File file) {
		return file.isDirectory() || file.getName().endsWith(".blocks")
							|| file.getName().endsWith(".dot");
	}
	
	@Override
	public String getDescription() {
		return "Blocks and Dot graph files";
	}
}

public class RectangleDrawerPanel extends JPanel implements Printable, MouseInputListener,
					Observer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int prevX, prevY;
	
	private boolean dragging;
	
	private Graphics2D g;
	
	private Rectangle2D rect;
	
	private ArrayList<Rectangle2D> rectangles = new ArrayList<Rectangle2D>();
	
	private Graph graph = null;
	
	private ArrayList<Rectangle2D> undoRectangles = new ArrayList<Rectangle2D>();;
	
	private Constraints constraints;
	
	BlocksFileFilter fileFilter = new BlocksFileFilter();
	
	protected Hashtable<Rectangle2D, Color> rectangleColourMap = new Hashtable<Rectangle2D, Color>();
	
	protected void generateRandom() {
		clear();
		Dimension dim = getSize();
		Random rand = new Random();
		int w = dim.width / 3;
		int h = dim.height / 3;
		for (int i = 0; i < 100; i++) {
			Rectangle2D r = new Rectangle(w + rand.nextInt(w / 2), h
								+ rand.nextInt(h / 2), rand.nextInt(w / 10), rand
								.nextInt(h / 10));
			rectangles.add(r);
		}
		int overlapCount = 0;
		for (int i = 0; i < rectangles.size(); i++) {
			Rectangle2D u = rectangles.get(i);
			for (int j = i + 1; j < rectangles.size(); j++) {
				Rectangle2D v = rectangles.get(j);
				if (u.intersects(v))
					overlapCount++;
			}
		}
		System.out.println("Random graph has " + overlapCount + " overlaps.");
		fitToScreen();
		repaint();
	}
	
	void fitToScreen() {
		ArrayList<Rectangle2D> rectangles = getRectangles();
		int xmax = 0;
		int xmin = Integer.MAX_VALUE;
		int ymax = 0;
		int ymin = Integer.MAX_VALUE;
		for (Rectangle2D r : rectangles) {
			xmin = Math.min(xmin, (int) r.getMinX());
			xmax = Math.max(xmax, (int) (r.getMinX() + r.getWidth()));
			ymin = Math.min(ymin, (int) r.getMinY());
			ymax = Math.max(ymax, (int) (r.getMinY() + r.getHeight()));
		}
		Dimension currentDim = new Dimension(xmax - xmin, ymax - ymin);
		Dimension targetDim = getSize();
		float xscale = (float) targetDim.width / (float) currentDim.width;
		float yscale = (float) targetDim.height / (float) currentDim.height;
		float scale = 1;
		if (xscale < yscale && xscale * currentDim.height <= targetDim.height) {
			scale = xscale;
		} else {
			scale = yscale;
		}
		for (Rectangle2D r : rectangles) {
			double x = r.getMinX();
			double y = r.getMinY();
			double w = r.getWidth();
			double h = r.getHeight();
			x -= xmin < 0 ? xmin : 0;
			y -= ymin < 0 ? ymin : 0;
			if (scale < 1) {
				x *= scale;
				w *= scale;
				y *= yscale;
				h *= yscale;
			}
			r.setRect(Math.floor(x), Math.floor(y), Math.floor(w), Math.floor(h));
		}
	}
	
	@SuppressWarnings("unchecked")
	protected void load(File f) {
		clear();
		ObjectInput input = null;
		if (f.getPath().endsWith(".dot")) {
			GraphParser g = new GraphParser(f.getPath());
			graph = g.getGraph();
			fileFilter.lastSelectedFile = f;
		} else {
			try {
				// use buffering
				InputStream file = new FileInputStream(f);
				InputStream buffer = new BufferedInputStream(file);
				input = new ObjectInputStream(buffer);
				// deserialize the List
				rectangles = (ArrayList<Rectangle2D>) input.readObject();
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				try {
					if (input != null) {
						// close "input" and its underlying streams
						input.close();
						fileFilter.lastSelectedFile = f;
					}
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		fitToScreen();
		repaint();
	}
	
	/**
	 * 
	 */
	protected void backup() {
		undoRectangles = new ArrayList<Rectangle2D>();
		for (Rectangle2D r : getRectangles()) {
			Rectangle nr = new Rectangle();
			nr.setRect(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight());
			undoRectangles.add(nr);
		}
	}
	
	protected void undo() {
		rectangles = new ArrayList<Rectangle2D>(undoRectangles);
	}
	
	/**
	 * @return
	 */
	protected BlocksFileFilter getFileFilter() {
		return fileFilter;
	}
	
	/**
	 * 
	 */
	public RectangleDrawerPanel() {
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	public void clear() {
		rectangles = new ArrayList<Rectangle2D>();
		constraints = null;
		graph = null;
		repaint();
	}
	
	public void render(int width, int height, Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_OFF);
		Color original = g.getColor();
		g.setColor(getBackground());
		g.fillRect(0, 0, width, height);
		g.setColor(original);
		super.paintChildren(g);
		if (graph != null) {
			for (Line2D l : graph.getLines()) {
				int x1 = (int) l.getP1().getX();
				int y1 = (int) l.getP1().getY();
				int x2 = (int) l.getP2().getX();
				int y2 = (int) l.getP2().getY();
				g.drawLine(x1, y1, x2, y2);
			}
		}
		for (Rectangle2D r : getRectangles()) {
			Color c = Color.LIGHT_GRAY;
			if (rectangleColourMap.containsKey(r)) {
				c = rectangleColourMap.get(r);
			}
			g.setPaint(c);
			g.fill(r);
			g.setPaint(Color.BLACK);
			g.draw(r);
		}
		drawConstraints();
		
	}
	
	@Override
	public void paintComponent(Graphics gOld) {
		Graphics2D g = (Graphics2D) gOld;
		Dimension size = getSize();
		render(size.width, size.height, g);
	}
	
	ArrayList<Rectangle2D> getRectangles() {
		if (graph != null) {
			return graph.getRectangles();
		}
		return rectangles;
	}
	
	private void setUpDrawingGraphics() {
		g = (Graphics2D) getGraphics();
		g.setColor(Color.black);
	}
	
	public void mousePressed(MouseEvent evt) {
		int x = evt.getX();
		int y = evt.getY();
		constraints = null;
		if (dragging == true)
			return;
		if (x > 0 && x < getWidth() && y > 0 && y < getHeight()) {
			prevX = x;
			prevY = y;
			dragging = true;
			setUpDrawingGraphics();
		}
		
	}
	
	public void drawConstraints() {
		if (constraints == null)
			return;
		g = (Graphics2D) getGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_OFF);
		Color originalColour = g.getColor();
		for (Constraint c : constraints) {
			c.colour = Color.BLUE;
			try {
				if (c.isViolated()) {
					c.colour = Color.RED;
					g.setColor(c.colour);
					Rectangle2D r1 = (Rectangle2D) c.left.data.get(Rectangle2D.class);
					Rectangle2D r2 = (Rectangle2D) c.right.data
										.get(Rectangle2D.class);
					// Chunk chunk = (Chunk)c.left.data.get(Chunk.class);
					g.drawLine((int) r1.getMinX(), (int) r1.getMinY(), (int) r2.getMinX(), (int) r2.getMinY());
				} else
					if (c.isTight()) {
						c.colour = Color.GREEN;
					}
			} catch (NullPointerException e) {
			}
		}
		g.setColor(originalColour);
	}
	
	public void mouseReleased(MouseEvent evt) {
		if (dragging == false)
			return;
		dragging = false;
		g.dispose();
		g = null;
		rectangles.add(rect);
		rect = null;
	}
	
	public void mouseDragged(MouseEvent evt) {
		if (dragging == false)
			return;
		int x = Math.min(evt.getX(), getSize().width - 1);
		x = Math.max(x, 0);
		int y = Math.min(evt.getY(), getSize().height - 1);
		y = Math.max(y, 0);
		paintComponent(g);
		Rectangle r = new Rectangle(Math.min(prevX, x), Math.min(prevY, y), Math.abs(x
							- prevX), Math.abs(y - prevY));
		g.drawRect(r.x, r.y, r.width, r.height);
		rect = r;
	}
	
	public void mouseEntered(MouseEvent evt) {
	}
	
	public void mouseExited(MouseEvent evt) {
	}
	
	public void mouseClicked(MouseEvent evt) {
	}
	
	public void mouseMoved(MouseEvent evt) {
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable p, Object arg1) {
		constraints = ((QPRectanglePlacement) p).constraints;
		paintComponent(getGraphics());
	}
	
	public int print(Graphics g, PageFormat pf, int pi) throws PrinterException {
		if (pi >= 1) {
			return Printable.NO_SUCH_PAGE;
		}
		Graphics2D g2d = (Graphics2D) g;
		g2d.translate(pf.getImageableX(), pf.getImageableY());
		g2d.translate(pf.getImageableWidth() / 2, pf.getImageableHeight() / 2);
		Dimension d = new Dimension();
		for (Rectangle2D r : getRectangles()) {
			d.height = Math.max((int) r.getMaxY(), d.height);
			d.width = Math.max((int) r.getMaxX(), d.width);
		}
		double scale = Math.min(pf.getImageableWidth() / d.width, pf
							.getImageableHeight()
							/ d.height);
		if (scale < 1.0) {
			g2d.scale(scale, scale);
		}
		g2d.translate(-d.width / 2.0, -d.height / 2.0);
		render(d.width, d.height, g2d);
		return Printable.PAGE_EXISTS;
	}
}
