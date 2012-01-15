package placement;

import java.util.HashMap;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActiveSetPlacement extends Observable implements Placement {
	static Logger logger = Logger.getLogger("placement");
	
	Blocks blocks;
	
	boolean debugAnimation = true;
	
	/** list of constraints waiting to be processed (not yet assigned to blocks) */
	Constraints activeConstraints = new Constraints();
	
	/** canonical list of constraints */
	private Constraints constraints = new Constraints();
	
	HashMap<String, Variable> vlookup = new HashMap<String, Variable>();
	
	private DebugFrame debugFrame;
	
	private long sleepTime;
	
	public boolean split = false;
	
	public Constraint addConstraint(String u, String v, double sep) {
		Constraint c = new Constraint(vlookup.get(u), vlookup.get(v),
							(float) sep);
		constraints.add(c);
		return c;
	}
	
	void satisfyConstraints() {
		blocks.totalOrder();
		Block b = blocks.head;
		while (b != null) {
			blocks.mergeLeft(b, this);
			b = b.nextRight;
		}
	}
	
	public ActiveSetPlacement(Variable[] vs) {
		for (Variable v : vs) {
			v.inConstraints = new Constraints();
			v.outConstraints = new Constraints();
		}
		blocks = new Blocks(vs);
		for (Variable v : vs) {
			vlookup.put(v.name, v);
		}
	}
	
	public float solve() {
		sleepTime = 500;
		if (debugAnimation)
			debugFrame = new DebugFrame(blocks, constraints);
		// activeConstraints = blocks.getAllConstraints();
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("variables: " + blocks.getAllVariables());
			logger.fine("sorted constraints: " + activeConstraints);
		}
		animate();
		satisfyConstraints();
		assert constraints.violated().isEmpty() : "Violated constraints not resolved";
		if (logger.isLoggable(Level.FINER))
			logger.finer("merged->" + blocks);
		if (logger.isLoggable(Level.FINER))
			logger.finer("Cost:" + blocks.cost());
		
		while (split) {
			sleepTime = 500;
			animate();
			Constraint splitConstraint = blocks.splitOnce(this);
			if (splitConstraint == null)
				break;
			animate();
			assert constraints.violated().isEmpty() : "Violated constraints not resolved";
			activeConstraints.add(splitConstraint);
			if (logger.isLoggable(Level.FINER))
				logger.finer("split->" + blocks);
			if (logger.isLoggable(Level.FINER))
				logger.finer("Cost:" + blocks.cost());
		}
		
		animate();
		assert constraints.violated().isEmpty() : "Violated constraints not resolved";
		if (logger.isLoggable(Level.FINER))
			logger.finer("Final->" + blocks);
		if (logger.isLoggable(Level.FINE))
			logger.fine("Cost:" + blocks.cost());
		return blocks.cost();
	}
	
	void animate() {
		if (debugAnimation) {
			debugFrame.animate();
			setChanged();
			notifyObservers();
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Constraints getConstraints() {
		return constraints;
	}
	
	public Variables getVariables() {
		return blocks.getAllVariables();
	}
}
