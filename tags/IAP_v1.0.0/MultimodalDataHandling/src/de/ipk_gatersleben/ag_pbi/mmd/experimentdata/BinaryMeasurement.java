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
package de.ipk_gatersleben.ag_pbi.mmd.experimentdata;

import java.io.InputStream;

import org.graffiti.plugin.io.resources.IOurl;
import org.graffiti.plugin.io.resources.ResourceIOManager;

/**
 * The labelfield-methods may be used to provide segmentation/cluster
 * information for
 * volumes, images and networks
 * 
 * @author Hendrik Rohn, Christian Klukas
 */
public interface BinaryMeasurement {
	
	/**
	 * @return The complete URL like file name, to access any data source at any
	 *         system, any place and any time.
	 *         Use the {@link ResourceIOManager} to get an {@link InputStream} or
	 *         a nice filename.
	 */
	public IOurl getURL();
	
	public void setURL(IOurl url);
	
	public IOurl getLabelURL();
	
	public void setLabelURL(IOurl url);
	
}