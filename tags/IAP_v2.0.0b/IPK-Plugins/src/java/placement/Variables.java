/*
 * Created on 28/02/2005
 */
package placement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author dwyer
 */

public class Variables extends ArrayList<Variable> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Variables getSources() {
		Variables sources = new Variables();
		for (Variable v : this) {
			if (v.getInConstraints().size() == 0) {
				v.visited = true;
				sources.add(v);
			}
		}
		return sources;
	}
	
	double getMaxPos() {
		double max = 0;
		for (Variable v : this) {
			if (v.getPosition() > max)
				max = v.getPosition();
		}
		return max;
	}
	
	double getMinPos() {
		double min = Double.MAX_VALUE;
		for (Variable v : this) {
			if (v.getPosition() < min)
				min = v.getPosition();
		}
		return min;
	}
	
	boolean contains(String label) {
		for (Variable v : this) {
			if (v.name.equals(label)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Sorts the variables in this list from lowest constraintDepth to highest.
	 */
	void sortOnConstraintDepth() {
		Collections.sort(this, new Comparator<Variable>() {
			public int compare(Variable v1, Variable v2) {
				if (v1.constraintDepth > v2.constraintDepth)
					return 1;
				if (v1.constraintDepth < v2.constraintDepth)
					return -1;
				return 0;
			}
			
		});
	}
}
