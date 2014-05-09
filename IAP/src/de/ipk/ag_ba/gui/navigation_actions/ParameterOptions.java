package de.ipk.ag_ba.gui.navigation_actions;

public class ParameterOptions {
	
	private final String description;
	private final Object[] parameters;
	
	public ParameterOptions(String description, Object[] parameters) {
		this.description = description;
		this.parameters = parameters;
	}
	
	public boolean userRequestNeeded() {
		return parameters != null && parameters.length > 0;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Object[] getParameterField() {
		return parameters;
	}
}
