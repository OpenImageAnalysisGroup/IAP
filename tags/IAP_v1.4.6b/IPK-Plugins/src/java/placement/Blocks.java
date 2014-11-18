/*
 * Created on 24/02/2005
 */
package placement;

/**
 * Merges two blocks
 * 
 * @author dwyer
 */
class Blocks {
	
	/**
	 * Examine the active constraints of each block. If a constraint is found,
	 * across which its container block may be split such that the new blocks
	 * can be moved without violating the constraint to better satisfy the
	 * desired positions, then the split and moves are carried out. Returns
	 * after the first such operation.
	 * 
	 * @return Constraint across which split occured or null if there was no
	 *         split.
	 */
	Constraint splitOnce(ActiveSetPlacement debug) {
		Block b = head;
		while (b != null) {
			b.inConstraintsPriorityQueue = null;
			b.outConstraintsPriorityQueue = null;
			b = b.nextRight;
		}
		b = head;
		while (b != null) {
			b.computeLagrangeMultipliers();
			Constraint c = null;
			double minLM = Double.MAX_VALUE;
			for (Constraint a : b.activeConstraints) {
				if (a.lagrangeMultiplier < minLM) {
					minLM = a.lagrangeMultiplier;
					c = a;
				}
			}
			if (c != null && c.lagrangeMultiplier < 0) {
				int prevBlockCount = size();
				Block l = new Block(), r = new Block();
				b.setUpInConstraints();
				b.setUpOutConstraints();
				b.split(c, l, r);
				r.position = b.position;
				r.weightedPosition = r.position * r.weight;
				l.nextLeft = b.nextLeft;
				if (l.nextLeft != null) {
					l.nextLeft.nextRight = l;
				} else {
					head = l;
				}
				l.nextRight = r;
				r.nextLeft = l;
				r.nextRight = b.nextRight;
				if (r.nextRight != null) {
					r.nextRight.nextLeft = r;
				}
				debug.animate();
				assert (prevBlockCount == size() - 1);
				mergeLeft(l, debug);
				// r may have been merged!
				r = c.right.container;
				r.position = r.desiredPosition();
				r.weightedPosition = r.position * r.weight;
				debug.animate();
				mergeRight(r, debug);
				r.setUpInConstraints();
				for (Constraint s : r.inConstraintsPriorityQueue.getAll()) {
					assert (!s.isViolated());
				}
				return c;
			}
			b = b.nextRight;
		}
		return null;
	}
	
	Block head = null;
	
	synchronized void mergeLeft(Block b, ActiveSetPlacement debug) {
		b.setUpInConstraints();
		Constraint c = b.findMaxInConstraint();
		while (c != null && c.isViolated()) {
			int prevBlockCount = size();
			c = b.inConstraintsPriorityQueue.deleteMax();
			Block l = c.left.container;
			assert (l != b);
			double distToL = c.left.offset + c.separation - c.right.offset;
			if (b.variables.size() > l.variables.size()) {
				b.merge(l, c, -distToL);
			} else {
				l.merge(b, c, distToL);
				Block tmp = b;
				b = l;
				l = tmp;
			}
			delete(l);
			debug.animate();
			assert (prevBlockCount == size() + 1);
			assert (b.activeConstraints.violated().isEmpty());
			c = b.findMaxInConstraint();
		}
	}
	
	void delete(Block b) {
		if (b == head) {
			head = b.nextRight;
			head.nextLeft = null;
		} else {
			b.nextLeft.nextRight = b.nextRight;
		}
		if (b.nextRight != null) {
			b.nextRight.nextLeft = b.nextLeft;
		}
	}
	
	synchronized void mergeRight(Block b, ActiveSetPlacement debug) {
		b.setUpOutConstraints();
		Constraint c = b.findMaxOutConstraint();
		while (c != null && c.isViolated()) {
			int prevBlockCount = size();
			c = b.outConstraintsPriorityQueue.deleteMax();
			Block r = c.right.container;
			double distToR = c.left.offset + c.separation - c.right.offset;
			if (b.variables.size() > r.variables.size()) {
				b.merge(r, c, distToR);
			} else {
				r.merge(b, c, -distToR);
				Block tmp = b;
				b = r;
				r = tmp;
			}
			delete(r);
			debug.animate();
			assert (prevBlockCount == size() + 1);
			// b.setUpOutConstraints();
			c = b.findMaxOutConstraint();
		}
	}
	
	Blocks(Variable[] vars) {
		for (Variable v : vars) {
			add(new Block(v));
		}
	}
	
	void add(Block b) {
		b.nextLeft = null;
		b.nextRight = null;
		if (head != null) {
			b.nextRight = head;
			head.nextLeft = b;
		}
		head = b;
	}
	
	Blocks() {
	}
	
	float cost() {
		float c = 0;
		Block b = head;
		while (b != null) {
			c += b.cost();
			b = b.nextRight;
		}
		return c;
	}
	
	Variables getAllVariables() {
		Variables vs = new Variables();
		Block b = head;
		while (b != null) {
			vs.addAll(b.variables);
			b = b.nextRight;
		}
		return vs;
	}
	
	int size() {
		int s = 0;
		Block b = head;
		while (b != null) {
			s += 1;
			b = b.nextRight;
		}
		return s;
	}
	
	@Override
	public String toString() {
		return "" + size();
	}
	
	/**
	 * DFS search of variables in the constraint DAG. From each variable
	 * constraints are processed from largest separation to smallest, and the
	 * constraint traversed if the current end depth is less than the depth
	 * computed from this DFS.
	 * 
	 * @param v
	 *           current dfs node
	 */
	private void dfsVisit(Variable v) {
		v.visited = true;
		// Assumes no merging has yet been done.
		assert v.container.variables.size() == 1;
		for (Constraint c : v.outConstraints) {
			assert c.left == v;
			if (!c.right.visited) {
				dfsVisit(c.right);
			}
		}
		add(v.container);
	}
	
	/**
	 * Computes a total ordering of constraints by depth-first search of the
	 * directed acyclic graph where nodes are variables and constraints b>=a+1
	 * are edges directed from a to b. Assumes no merging has yet been done.
	 */
	public void totalOrder() {
		Variables vars = getAllVariables();
		head = null;
		for (Variable v : vars) {
			v.visited = false;
		}
		for (Variable v : vars.getSources()) {
			dfsVisit(v);
		}
	}
}
