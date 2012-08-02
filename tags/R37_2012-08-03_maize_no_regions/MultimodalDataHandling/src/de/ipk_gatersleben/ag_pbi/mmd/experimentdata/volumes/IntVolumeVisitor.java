package de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes;

public interface IntVolumeVisitor {
	void visit(int x, int y, int z, int value) throws Exception;
}
