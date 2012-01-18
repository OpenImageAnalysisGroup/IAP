/*
 * The following source code is part of the WilmaScope 3D Graph Drawing Engine
 * which is distributed under the terms of the GNU Lesser General Public License
 * (LGPL - http://www.gnu.org/copyleft/lesser.html).
 * As usual we distribute it with no warranties and anything you chose to do
 * with it you do at your own risk.
 * Copyright for this work is retained by Tim Dwyer and the WilmaScope organisation
 * (www.wilmascope.org) however it may be used or modified to work as part of
 * other software subject to the terms of the LGPL. I only ask that you cite
 * WilmaScope as an influence and inform us (tgdwyer@yahoo.com)
 * if you do anything really cool with it.
 * The WilmaScope software source repository is hosted by Source Forge:
 * www.sourceforge.net/projects/wilma
 * -- Tim Dwyer, 2001
 */
package placement;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeSet;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
abstract class Chunk<T extends Chunk> {
	T conj;
	
	Rectangle2D rect;
	
	Variable v;
	
	Chunk leftNeighbour;
	
	Chunk rightNeighbour;
	
	ArrayList<Chunk> leftNeighbours = new ArrayList<Chunk>();
	
	ArrayList<Chunk> rightNeighbours = new ArrayList<Chunk>();
	
	abstract double getMax();
	
	abstract double getMin();
	
	abstract void setMin(double d);
	
	abstract double getLength();
	
	Chunk(Rectangle2D r) {
		this.rect = r;
	}
	
	Chunk(Rectangle2D r, T conjugate) {
		this.rect = r;
		this.conj = conjugate;
	}
	
	void addLeftNeighbour(Chunk n) {
		if (!leftNeighbours.contains(n)) {
			leftNeighbours.add(n);
		}
	}
	
	void addRightNeighbour(Chunk n) {
		if (!rightNeighbours.contains(n)) {
			rightNeighbours.add(n);
		}
	}
	
	void setNeighbours(ArrayList<Chunk> leftv, ArrayList<Chunk> rightv) {
		leftNeighbours = leftv;
		for (Chunk u : leftv) {
			u.addRightNeighbour(this);
		}
		rightNeighbours = rightv;
		for (Chunk u : rightv) {
			u.addLeftNeighbour(this);
		}
	}
	
	double overlap(Chunk b) {
		assert (b.getClass() == this.getClass());
		if (getMin() < b.getMin() && b.getMin() < getMax())
			return getMax() - b.getMin();
		if (b.getMin() < getMin() && getMin() < b.getMax())
			return b.getMax() - getMin();
		return 0;
	}
	
	double conjugateOverlap(Chunk c) {
		T a = conj;
		T b = (T) c.conj;
		assert (c.getClass() == this.getClass());
		if (a.getMin() <= b.getMin() && b.getMin() < a.getMax())
			return a.getMax() - b.getMin();
		if (b.getMin() <= a.getMin() && a.getMin() < b.getMax())
			return b.getMax() - a.getMin();
		return 0;
	}
	
	static Comparator<Chunk> comparator = new Comparator<Chunk>() {
		public int compare(Chunk a, Chunk b) {
			if (a.getMin() > b.getMin())
				return 1;
			if (a.getMin() < b.getMin())
				return -1;
			Chunk ac = a.conj;
			Chunk bc = b.conj;
			if (ac.getMin() > bc.getMin())
				return 1;
			if (ac.getMin() < bc.getMin())
				return -1;
			return 0;
		}
	};
	
	static Comparator<Chunk> conjComparator = new Comparator<Chunk>() {
		public int compare(Chunk a, Chunk b) {
			Chunk ac = a.conj;
			Chunk bc = b.conj;
			if (ac.getMin() > bc.getMin())
				return 1;
			if (ac.getMin() < bc.getMin())
				return -1;
			if (a.getMin() > b.getMin())
				return 1;
			if (a.getMin() < b.getMin())
				return -1;
			return 0;
		}
	};
	
	@Override
	public String toString() {
		return v.toString();
	}
}

class YChunk extends Chunk<XChunk> {
	static double g = 0;
	
	YChunk(Rectangle2D r, XChunk conjugate) {
		super(r, conjugate);
	}
	
	YChunk(Rectangle2D r) {
		super(r);
	}
	
	@Override
	public double getMax() {
		return rect.getMaxY() + g;
	}
	
	@Override
	public double getMin() {
		return rect.getMinY();
	}
	
	@Override
	public double getLength() {
		return rect.getHeight() + g;
	}
	
	@Override
	void setMin(double min) {
		if (rect instanceof Rectangle) {
			min = Math.ceil(min);
		}
		rect.setRect(rect.getMinX(), min, rect.getWidth(), rect.getHeight());
	}
}

class XChunk extends Chunk<YChunk> {
	static double g = 0;
	
	XChunk(Rectangle2D r, YChunk conjugate) {
		super(r, conjugate);
	}
	
	XChunk(Rectangle2D r) {
		super(r);
	}
	
	@Override
	public double getMax() {
		return rect.getMaxX() + g;
	}
	
	@Override
	public double getMin() {
		return rect.getMinX();
	}
	
	@Override
	public double getLength() {
		return rect.getWidth() + g;
	}
	
	@Override
	void setMin(double min) {
		if (rect instanceof Rectangle) {
			// because Rectangles have int coords!
			min = Math.ceil(min);
		}
		rect.setRect(min, rect.getMinY(), rect.getWidth(), rect.getHeight());
	}
	
}

@SuppressWarnings("unchecked")
class ChunkEdge implements Comparable<ChunkEdge> {
	Chunk chunk;
	
	boolean isStart;
	
	double position;
	
	ChunkEdge(Chunk c, boolean s, double p) {
		chunk = c;
		isStart = s;
		position = p;
	}
	
	public int compareTo(ChunkEdge arg) {
		if (this.position > arg.position)
			return 1;
		if (this.position < arg.position)
			return -1;
		return 0;
	}
}

@SuppressWarnings("unchecked")
public class QPRectanglePlacement extends Observable implements RectanglePlacement, Observer {
	static Logger logger = Logger.getLogger(QPRectanglePlacement.class.getName());
	
	Constraints constraints;
	
	private boolean completeConstraints;
	
	private boolean mosekPlacement;
	
	private boolean animate;
	
	private Chunk[] chunks;
	
	private Hashtable<Rectangle2D, Color> rectangleColourMap;
	
	private boolean orthogonalOrdering;
	
	private boolean splitRefinement;
	
	/**
	 * @param completeConstraints
	 * @param animate
	 */
	public QPRectanglePlacement(boolean split, boolean completeConstraints, boolean orthogonalOrdering,
						boolean mosekPlacement, double xgap, double ygap, boolean animate) {
		this.splitRefinement = split;
		this.completeConstraints = completeConstraints;
		this.mosekPlacement = mosekPlacement;
		this.animate = animate;
		this.orthogonalOrdering = orthogonalOrdering;
		XChunk.g = xgap;
		YChunk.g = ygap;
	}
	
	void place(ArrayList<Rectangle2D> rectangles,
						Hashtable<Rectangle2D, Color> colourMap) {
		rectangleColourMap = colourMap;
		XChunk[] xs = new XChunk[rectangles.size()];
		logger.fine("*****************Placing X");
		for (int i = 0; i < rectangles.size(); i++) {
			Rectangle2D r = rectangles.get(i);
			xs[i] = new XChunk(r, new YChunk(r));
		}
		allOverlaps = false;
		long t1 = System.currentTimeMillis();
		initVarsAndConstraints(xs);
		long t2 = System.currentTimeMillis();
		System.out.println("Gen hor. cs: time=" + (t2 - t1));
		chunks = xs;
		double cost = placement();
		long t3 = System.currentTimeMillis();
		System.out.println("Place hor.: cost=" + cost + " time=" + (t3 - t2));
		// /////////////////////////////
		YChunk[] ys = new YChunk[rectangles.size()];
		logger.fine("*****************Placing Y");
		for (int i = 0; i < rectangles.size(); i++) {
			Rectangle2D r = rectangles.get(i);
			ys[i] = new YChunk(r, new XChunk(r));
		}
		allOverlaps = true;
		long t4 = System.currentTimeMillis();
		initVarsAndConstraints(ys);
		long t5 = System.currentTimeMillis();
		System.out.println("Gen ver. cs: time=" + (t5 - t4));
		chunks = ys;
		cost = placement();
		long t6 = System.currentTimeMillis();
		System.out.println("Place ver.: cost=" + cost + " time=" + (t6 - t5));
		System.out.println("Total time=" + (t6 - t1));
	}
	
	boolean allOverlaps;
	
	void initVarsAndConstraints(Chunk[] chunks) {
		if (completeConstraints) {
			initVarsAndConstraintsComplete(chunks);
		} else {
			initVarsAndConstraintsMinimal(chunks);
			if (orthogonalOrdering) {
				addOrthogonalOrderingConstraints(chunks);
			}
		}
		setChanged();
		notifyObservers();
	}
	
	/**
	 * Adds a zero-length constraint between each pair of adjacent chunks (after
	 * sorting on position).
	 * 
	 * @param chunks
	 */
	void addOrthogonalOrderingConstraints(Chunk[] chunks) {
		Arrays.sort(chunks, Chunk.comparator);
		for (int i = 1; i < chunks.length; i++) {
			Chunk l = chunks[i - 1];
			Chunk r = chunks[i];
			constraints.add(new Constraint(l.v, r.v, 0));
		}
	}
	
	// n^2 time and potentially n^2 constraints
	void initVarsAndConstraintsComplete(Chunk[] chunks) {
		Arrays.sort(chunks, Chunk.comparator);
		for (int i = 0; i < chunks.length; i++) {
			Chunk r = chunks[i];
			r.v = new Variable("r" + i, r.getMin(), 1);
			r.v.data.put(Chunk.class, r);
			r.v.data.put(Rectangle2D.class, r.rect);
		}
		constraints = new Constraints();
		for (int i = 0; i < chunks.length - 1; i++) {
			Chunk l = chunks[i];
			for (int j = i + 1; j < chunks.length; j++) {
				Chunk r = chunks[j];
				if (needConstraint(l, r, allOverlaps)) {
					Variable vl = l.v;
					Variable vr = r.v;
					constraints.add(new Constraint(vl, vr, l.getLength()));
				}
			}
		}
	}
	
	/**
	 * Generate minimal set of separation constraints between blocks. Sweeps
	 * through the conjugate dimension (eg, y if we're processing x separation
	 * constraints) maintaining a set of open blocks sorted by x placement. For
	 * each opening edge of a block x, we determine x's neighbours (l and r) and
	 * if necessary, set x,l and r's neighbour pointers. When we process a
	 * closing edge of a block x, we create 0,1 or 2 constraints between x and
	 * it's neighbours, resetting the neighbours' neighbour pointers. Generates
	 * up to 2n constraints, O(n log n) time.
	 */
	void initVarsAndConstraintsMinimalOld(Chunk[] chunks) {
		// sort lexically, just makes variable naming easier to follow
		Arrays.sort(chunks, Chunk.conjComparator);
		ChunkEdge[] edges = new ChunkEdge[chunks.length * 2];
		for (int i = 0; i < chunks.length; i++) {
			Chunk c = chunks[i];
			c.v = new Variable("r" + i, c.getMin(), 1/* c.getLength() */);
			c.v.data.put(Chunk.class, c);
			c.v.data.put(Rectangle2D.class, c.rect);
			Chunk conj = c.conj;
			edges[i * 2] = new ChunkEdge(c, true, conj.getMin());
			edges[i * 2 + 1] = new ChunkEdge(c, false, conj.getMax());
		}
		Arrays.sort(edges);
		constraints = new Constraints();
		TreeSet<Chunk> head, tail, open = new TreeSet<Chunk>(Chunk.comparator);
		for (ChunkEdge e : edges) {
			Chunk l, r, x = e.chunk;
			if (e.isStart) {
				open.add(x);
				head = (TreeSet<Chunk>) open.headSet(x);
				if (!head.isEmpty()) {
					l = head.last();
					if (needConstraint(l, x, allOverlaps)) {
						l.rightNeighbour = x;
						x.leftNeighbour = l;
					}
				}
				tail = (TreeSet<Chunk>) open.tailSet(x);
				if (tail.size() > 1) { // tail starts with x, need second
					// element
					Iterator<Chunk> i = tail.iterator();
					i.next();
					r = i.next();
					if (needConstraint(x, r, allOverlaps)) {
						r.leftNeighbour = x;
						x.rightNeighbour = r;
					}
				}
			} else {
				open.remove(x);
				if ((l = x.leftNeighbour) != null) {
					constraints.add(new Constraint(l.v, x.v, l.getLength()));
					l.rightNeighbour = null;
				}
				if ((r = x.rightNeighbour) != null) {
					constraints.add(new Constraint(x.v, r.v, x.getLength()));
					r.leftNeighbour = null;
				}
			}
		}
	}
	
	void initVarsAndConstraintsMinimal(Chunk[] chunks) {
		// sort lexically, just makes variable naming easier to follow
		Arrays.sort(chunks, Chunk.conjComparator);
		ChunkEdge[] edges = new ChunkEdge[chunks.length * 2];
		for (int i = 0; i < chunks.length; i++) {
			Chunk c = chunks[i];
			c.v = new Variable("r" + i, c.getMin(), 1/* c.getLength() */);
			c.v.data.put(Chunk.class, c);
			c.v.data.put(Rectangle2D.class, c.rect);
			Chunk conj = c.conj;
			edges[i * 2] = new ChunkEdge(c, true, conj.getMin());
			edges[i * 2 + 1] = new ChunkEdge(c, false, conj.getMax());
		}
		Arrays.sort(edges);
		constraints = new Constraints();
		TreeSet<Chunk> open = new TreeSet<Chunk>(Chunk.comparator);
		for (ChunkEdge e : edges) {
			Chunk v = e.chunk;
			if (e.isStart) {
				open.add(v);
				v.setNeighbours(getLeftNeighbours(open, v), getRightNeighbours(
									open, v));
			} else {
				for (Iterator<Chunk> i = v.leftNeighbours.iterator(); i
									.hasNext();) {
					Chunk u = i.next();
					constraints.add(new Constraint(u.v, v.v, u.getLength()));
					u.rightNeighbours.remove(v);
				}
				for (Iterator<Chunk> i = v.rightNeighbours.iterator(); i
									.hasNext();) {
					Chunk u = i.next();
					constraints.add(new Constraint(v.v, u.v, v.getLength()));
					u.leftNeighbours.remove(v);
				}
				open.remove(v);
			}
		}
	}
	
	ArrayList<Chunk> getLeftNeighbours(TreeSet<Chunk> scanLine, Chunk v) {
		ArrayList<Chunk> lhs = new ArrayList<Chunk>(scanLine
							.headSet(v));
		ArrayList<Chunk> leftv = new ArrayList<Chunk>();
		for (int i = lhs.size() - 1; i >= 0; i--) {
			Chunk u = lhs.get(i);
			if (u.overlap(v) <= 0) {
				leftv.add(u);
				return leftv;
			}
			if (allOverlaps || u.overlap(v) <= u.conjugateOverlap(v)) {
				leftv.add(u);
			}
		}
		return leftv;
	}
	
	ArrayList<Chunk> getRightNeighbours(TreeSet<Chunk> scanLine, Chunk v) {
		ArrayList<Chunk> rhs = new ArrayList<Chunk>(scanLine
							.tailSet(v));
		ArrayList<Chunk> rightv = new ArrayList<Chunk>();
		for (int i = 1; i < rhs.size(); i++) {
			Chunk u = rhs.get(i);
			if (u.overlap(v) <= 0) {
				rightv.add(u);
				return rightv;
			}
			if (allOverlaps || u.overlap(v) <= u.conjugateOverlap(v)) {
				rightv.add(u);
			}
		}
		return rightv;
	}
	
	double placement() {
		float cost = 0;
		Variable[] vs = new Variable[chunks.length];
		for (int i = 0; i < chunks.length; i++) {
			vs[i] = chunks[i].v;
		}
		Placement p = null;
		if (mosekPlacement) {
			System.out.println("Running mosek with:\n   |V|=" + vs.length);
			// p = new MosekPlacement(vs);
		} else {
			System.out.println("Running AS with:\n   |V|=" + vs.length);
			p = new ActiveSetPlacement(vs);
			((ActiveSetPlacement) p).split = splitRefinement;
			((ActiveSetPlacement) p).debugAnimation = animate;
			((ActiveSetPlacement) p).addObserver(this);
		}
		for (Constraint c : constraints) {
			p.addConstraint(c.left.name, c.right.name, c.separation);
		}
		System.out.println("   |C|=" + p.getConstraints().size());
		try {
			cost = p.solve();
			// p = new MosekPlacement(p.getVariables(), p.getConstraints());
			// float mcost = p.solve();
			// assert (2 * Math.abs(cost - mcost) / (1 + mcost + cost) < 0.001) : "Solver did not find optimal solution!";
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		for (Chunk c : chunks) {
			c.setMin(c.v.getPosition());
		}
		return cost;
	}
	
	@Override
	public void addObserver(Observer o) {
		super.addObserver(o);
	}
	
	/**
	 * test if there needs to be a constraint between l and r if: - we are
	 * removing all overlaps and there is overlap in the conjugate axis - there
	 * is overlap in the conjugate axis but not in this axis - there is overlap
	 * in this axis and that overlap is less than that in the conjugate
	 * 
	 * @param l
	 *           leftChunk
	 * @param r
	 *           rightChunk
	 * @param all
	 *           true if we need to remove all overlaps
	 */
	boolean needConstraint(Chunk l, Chunk r, boolean all) {
		boolean overlap = l.overlap(r) > 0;
		boolean conjOverlap = l.conjugateOverlap(r) > 0;
		boolean conjOverlapGreaterThanThis = l.overlap(r) > 0
							&& l.overlap(r) < l.conjugateOverlap(r);
		if (all && conjOverlap)
			return true;
		if (conjOverlap && !overlap)
			return true;
		if (overlap && conjOverlapGreaterThanThis)
			return true;
		return false;
	}
	
	public void update(Observable arg0, Object arg1) {
		for (Chunk c : chunks) {
			c.setMin(c.v.getPosition());
			if (c.v.colour != null)
				rectangleColourMap.put(c.rect, c.v.colour);
		}
		setChanged();
		notifyObservers();
	}
	
	public void place(ArrayList<Rectangle2D> rectangles) {
		place(rectangles, null);
	}
	
}
