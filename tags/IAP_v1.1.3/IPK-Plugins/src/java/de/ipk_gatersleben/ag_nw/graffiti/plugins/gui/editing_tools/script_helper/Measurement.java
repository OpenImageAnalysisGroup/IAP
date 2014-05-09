package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

public interface Measurement extends MappingDataEntity {
	
	public double getValue();
	
	public SampleInterface getParentSample();
	
	public int getReplicateID();
	
}
