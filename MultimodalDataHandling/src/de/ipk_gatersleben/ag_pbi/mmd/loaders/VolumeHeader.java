/*************************************************************************************
 * The MultimodalDataHandling Add-on is (c) 2008-2010 Plant Bioinformatics Group,
 * IPK Gatersleben, http://bioinformatics.ipk-gatersleben.de
 * The source code for this project, which is developed by our group, is
 * available under the GPL license v2.0 available at
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.html. By using this
 * Add-on and VANTED you need to accept the terms and conditions of this
 * license, the below stated disclaimer of warranties and the licenses of
 * the used libraries. For further details see license.txt in the root
 * folder of this project.
 ************************************************************************************/
package de.ipk_gatersleben.ag_pbi.mmd.loaders;

import de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes.VolumeColorDepth;

public class VolumeHeader {
	
	// public int bit16 = -1;
	public double voxeldimx = 30;
	public double voxeldimy = 15;
	public double voxeldimz = 15;
	public int voxelnbrx = 100;
	public int voxelnbry = 100;
	public int voxelnbrz = 100;
	public boolean intelByteOrder = false;
	public String unit;
	public int offset = 0;
	public VolumeColorDepth fileType = VolumeColorDepth.BIT8;
	public CheckSize checkFileSize = CheckSize.EXACT;
	
	public enum CheckSize {
		EXACT, APPROXIMATE, NONE
		
	}
	
}
