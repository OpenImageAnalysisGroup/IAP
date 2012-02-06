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

import java.util.Map;

import org.jdom.Attribute;
import org.jdom.Element;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.MappingDataEntity;

public class UsergroupData implements MappingDataEntity {
	
	private String groupname;
	
	public UsergroupData(String groupname) {
		this.setGroupname(groupname);
	}
	
	public void getStringOfChildren(StringBuilder r) {
		// TODO Auto-generated method stub
		
	}
	
	public void getXMLAttributeString(StringBuilder r) {
		// TODO Auto-generated method stub
		
	}
	
	public void setAttribute(Attribute attr) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean setData(Element xmlElement) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void setDataOfChildElement(Element childElement) {
		// TODO Auto-generated method stub
		
	}
	
	public void setGroupname(String groupname) {
		this.groupname = groupname;
	}
	
	public String getGroupname() {
		return groupname;
	}
	
	/*
	 * (non-Javadoc)
	 * @see
	 * de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper
	 * .MappingDataEntity#fillAttributeMap(java.util.Map)
	 */
	public void fillAttributeMap(Map<String, Object> attributeValueMap) {
		// TODO Auto-generated method stub
		
	}
	
}
