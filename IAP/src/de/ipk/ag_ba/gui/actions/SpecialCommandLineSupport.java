package de.ipk.ag_ba.gui.actions;

public interface SpecialCommandLineSupport {
	/**
	 * @return true, if preparation OK and execution should continue.
	 * @throws Exception
	 */
	public boolean prepareCommandLineExecution() throws Exception;
	
	public void postProcessCommandLineExecution();
	
}
