package placement;

public interface Placement {
	
	public abstract Constraint addConstraint(String u, String v, double sep);
	
	public abstract float solve() throws Exception;
	
	public abstract Constraints getConstraints();
	
	public abstract Variables getVariables();
	
}