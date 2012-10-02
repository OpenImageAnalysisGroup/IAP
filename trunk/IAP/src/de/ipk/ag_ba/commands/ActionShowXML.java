package de.ipk.ag_ba.commands;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.transform.TransformerException;

import org.ErrorMsg;
import org.StringManipulationTools;
import org.jdom.JDOMException;
import org.w3c.dom.Document;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReference;
import de.ipk.ag_ba.mongo.MongoDB;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.JDOM2DOM;

/**
 * @author klukas
 */
public class ActionShowXML extends AbstractNavigationAction {
	
	private MongoDB m;
	private ExperimentReference experiment;
	private NavigationButton src;
	ArrayList<String> xmlOutput = new ArrayList<String>();
	
	public ActionShowXML(MongoDB m, ExperimentReference experiment) {
		super("Show Experiment-Data as XML");
		this.m = m;
		this.experiment = experiment;
	}
	
	public ActionShowXML() {
		super("Show Experiment as XML");
	}
	
	@Override
	public void performActionCalculateResults(final NavigationButton src) {
		this.src = src;
		xmlOutput.clear();
		try {
			getStatusProvider().setCurrentStatusText1("Generate XML...");
			ExperimentInterface res = experiment.getData(m);
			for (Document document : Experiment.getDocuments(res, getStatusProvider(), false)) {
				String xml = "";
				try {
					xml += document != null ? JDOM2DOM.getOuterXmlPretty(document) : "(XML == null)";
				} catch (IOException e1) {
					xml = "(" + e1.getMessage() + ")";
				} catch (TransformerException e1) {
					xml = "(" + e1.getMessage() + ")";
				} catch (JDOMException e1) {
					xml = "(" + e1.getMessage() + ")";
				}
				xmlOutput.add(StringManipulationTools.UnicodeToHtml(xml));
			}
		} catch (Exception e) {
			ErrorMsg.addErrorMessage(e);
		}
		getStatusProvider().setCurrentStatusText1("Processing finished");
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>(currentSet);
		res.add(src);
		return res;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> res = new ArrayList<NavigationButton>();
		return res;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		return new MainPanelComponent(xmlOutput);
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Gnome-Text-X-Script-64.png";
	}
	
	@Override
	public String getDefaultTitle() {
		return "Show XML";
	}
}