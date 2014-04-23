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
package de.ipk_gatersleben.ag_pbi.mmd.experimentdata.volumes;

public enum VolumeColorDepth {
	BIT8("8 Bit", 1), BIT16("16 Bit", 2), RGB("RGB", 3), RGBA("RGBA", 4);
	
	private String name;
	private int bytes;
	
	private VolumeColorDepth(String name, int bytes) {
		this.name = name;
		this.bytes = bytes;
	}
	
	public String getName() {
		return name;
	}
	
	public static VolumeColorDepth getDepthFromString(String colordepth) {
		for (VolumeColorDepth d : values())
			if (d.getName().equals(colordepth))
				return d;
		return null;
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public int getBytes() {
		return bytes;
	}
	
	public boolean isRGBorRGBA() {
		return this == RGB || this == RGBA;
	}
	
}
