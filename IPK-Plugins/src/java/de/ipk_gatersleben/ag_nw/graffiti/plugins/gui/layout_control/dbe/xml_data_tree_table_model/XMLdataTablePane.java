/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 18.11.2003
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.xml_data_tree_table_model;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.xml.transform.TransformerException;

import org.ErrorMsg;
import org.graffiti.plugin.XMLHelper;
import org.jdom.JDOMException;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.SubstanceInterface;

/**
 * @author Christian Klukas
 */
public class XMLdataTablePane extends JComponent {
	
	private static final long serialVersionUID = 1L;
	String myCompoundID;
	String mySubstrateName;
	String myFormula;
	
	public XMLdataTablePane(ExperimentInterface experiment, final JTabbedPane parentPane) {
		
		double border = 5;
		double[][] size = { { border, TableLayoutConstants.FILL, border }, // Columns
				{ border, TableLayoutConstants.PREFERRED, TableLayoutConstants.FILL, border } }; // Rows
		
		this.setLayout(new TableLayout(size));
		
		JButton closeTab = new JButton("Close this Tab");
		closeTab.setOpaque(false);
		final JComponent thisTab = this;
		closeTab.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				parentPane.remove(thisTab);
			}
		});
		
		this.add(closeTab, "1,1");
		
		/*
		 * String mapping=""; try { if (mappedDataList!=null) for (Iterator
		 * it=mappedDataList.iterator(); it.hasNext(); ) { // w3c node ! not a
		 * gravisto node ! Node n = (Node) it.next();
		 * mapping+="***********"+MyXMLhelper.getOuterXml(n); } this.add(new
		 * JEditorPane(mapping), "1,2"); } catch (IOException e) {
		 * ErrorMsg.addErrorMessage(e.getLocalizedMessage()); } JTextArea jta =
		 * new JTextArea(mapping); jta.setLineWrap(true);
		 * jta.setWrapStyleWord(true); jta.setAutoscrolls(true); this.add(new
		 * JScrollPane(jta), "1,2");
		 */

		/*
		 * System.out.println("-----"); try{
		 * System.out.println(XMLHelper.getOuterXml(doc.getFirstChild())); }
		 * catch(Exception e) { System.out.println("xml output error"); }
		 */

		JTabbedPane dataPanes = new JTabbedPane();
		
		int mapCnt = 0;
		for (SubstanceInterface n : experiment) {
			mapCnt++;
			dataPanes.addTab("Mapping " + mapCnt, getXMLeditView(n));
		}
		this.add(dataPanes, "1,2");
		this.revalidate();
		
	}
	
	private JComponent getXMLeditView(SubstanceInterface n) {
		JEditorPane jep = null;
		try {
			org.w3c.dom.Document doc = XMLHelper.getDocumentFromXMLstring(n.getXMLstring());
			jep = new JEditorPane("text/plain", XMLHelper.getOuterXmlPretty(doc.getFirstChild()));
		} catch (IOException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (TransformerException e) {
			ErrorMsg.addErrorMessage(e);
		} catch (JDOMException e) {
			ErrorMsg.addErrorMessage(e);
		}
		if (jep == null)
			jep = new JEditorPane("text/plain", "XML Transformation failed for: " + n.getXMLstring());
		return new JScrollPane(jep);
	}
}
