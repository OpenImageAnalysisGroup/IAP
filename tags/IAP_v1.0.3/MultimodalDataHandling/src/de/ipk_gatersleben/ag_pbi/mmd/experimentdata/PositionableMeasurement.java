package de.ipk_gatersleben.ag_pbi.mmd.experimentdata;

public interface PositionableMeasurement {
	
	public String getPositionIn3D();
	
	public void setPositionIn3D(String positionIn3D);
	
	public String getRotation();
	
	public void setRotation(String rotation);
	
}