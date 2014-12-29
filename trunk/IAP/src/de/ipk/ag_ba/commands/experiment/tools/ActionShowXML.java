package de.ipk.ag_ba.commands.experiment.tools;

import java.io.IOException;
import java.util.ArrayList;

import javax.xml.transform.TransformerException;

import org.ErrorMsg;
import org.StringManipulationTools;
import org.jdom.JDOMException;
import org.w3c.dom.Document;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.experiment.view_or_export.ActionDataProcessing;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.ExperimentReferenceInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.Experiment;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentInterface;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.JDOM2DOM;

/**
 * @author klukas
 */
public class ActionShowXML extends AbstractNavigationAction implements ActionDataProcessing {
	private ExperimentReferenceInterface experiment;
	private NavigationButton src;
	ArrayList<String> xmlOutput = new ArrayList<String>();
	
	public ActionShowXML() {
		super("Show Experiment-Data as XML");
	}
	
	@Override
	public void performActionCalculateResults(final NavigationButton src) {
		this.src = src;
		xmlOutput.clear();
		try {
			getStatusProvider().setCurrentStatusText1("Generate XML...");
			ExperimentInterface res = experiment.getData(false, getStatusProvider());
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
				xmlOutput.add(
						StringManipulationTools.stringReplace(
								StringManipulationTools.UnicodeToHtml(xml),
								" ", "&nbsp;"));
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
	
	@Override
	public boolean isImageAnalysisCommand() {
		return false;
	}
	
	@Override
	public void setExperimentReference(ExperimentReferenceInterface experimentReference) {
		this.experiment = experimentReference;
	}
}