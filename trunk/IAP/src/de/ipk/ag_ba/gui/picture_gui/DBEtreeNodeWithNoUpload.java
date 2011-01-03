/*******************************************************************************
 * The DBE2 Add-on is (c) 2009-2010 Plant Bioinformatics Group, IPK Gatersleben,
 * http://bioinformatics.ipk-gatersleben.de
 * The source code for this project which is developed by our group is available
 * under the GPL license v2.0 (http://www.gnu.org/licenses/old-licenses/gpl-2.0.html).
 * By using this Add-on and VANTED you need to accept the terms and conditions of
 * this license, the below stated disclaimer of warranties and the licenses of the used
 * libraries. For further details see license.txt in the root folder of this project.
 ******************************************************************************/
/*
 * Created on 20.04.2004
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package de.ipk.ag_ba.gui.picture_gui;

/**
 * @author klukas
 *         To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Generation - Code and Comments
 */
public class DBEtreeNodeWithNoUpload extends MongoTreeNodeBasis {
	
	private final String name;
	
	/**
	 * @param string
	 */
	public DBEtreeNodeWithNoUpload(String name, boolean readOnly) {
		super(readOnly);
		this.name = name;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name;
	}
}
