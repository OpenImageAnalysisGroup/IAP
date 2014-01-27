package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import org.jdom.Element;

public interface MappingDataEntity extends AttributeValuePairSupport, FileAttachmentSupport {
	
	static final String ANNO_DIV = "|";
	
	public boolean setData(Element xmlElement);
	
	public void setAttribute(MyAttribute attr);
	
	public void setDataOfChildElement(Element childElement);
	
	public void getXMLAttributeString(StringBuilder r);
	
	public void getStringOfChildren(StringBuilder r);
	
}
