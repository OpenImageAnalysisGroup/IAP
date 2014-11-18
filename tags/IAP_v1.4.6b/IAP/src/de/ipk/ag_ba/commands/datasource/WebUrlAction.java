package de.ipk.ag_ba.commands.datasource;

import java.io.StringWriter;
import java.util.ArrayList;

import org.AttributeHelper;
import org.StringManipulationTools;
import org.apache.commons.io.IOUtils;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.IAPservice;
import de.ipk.ag_ba.mongo.MongoDB;

public final class WebUrlAction extends AbstractUrlNavigationAction {
	IOurl trueURL = null;
	String htmlTextPanel = null;
	private final IOurl bookURL;
	
	public WebUrlAction(IOurl bookURL, String tooltip) {
		super(tooltip);
		this.bookURL = bookURL;
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Hyperlink-Internet-Search-64.png";
	}
	
	@Override
	public void performActionCalculateResults(NavigationButton src) {
		IOurl referenceURL = bookURL;
		if (trueURL == null)
			if (referenceURL.endsWith(".url") || referenceURL.endsWith(".webloc"))
				try {
					trueURL = IAPservice.getURLfromWeblocFile(referenceURL);
				} catch (Exception e) {
					MongoDB.saveSystemErrorMessage("Could not read webloc-file from " + referenceURL + ".", e);
					AttributeHelper.showInBrowser(referenceURL);
					return;
				}
			else
				if (referenceURL.endsWith(".txt") || referenceURL.endsWith(".htm") || referenceURL.endsWith(".html"))
					try {
						StringWriter writer = new StringWriter();
						IOUtils.copy(referenceURL.getInputStream(), writer);
						htmlTextPanel = writer.toString();
						if (referenceURL.endsWith(".txt"))
							htmlTextPanel = StringManipulationTools.txt2html(htmlTextPanel);
					} catch (Exception e) {
						MongoDB.saveSystemErrorMessage("Could not read content from " + referenceURL + ".", e);
						AttributeHelper.showInBrowser(referenceURL);
						return;
					}
				else
					trueURL = referenceURL;
		AttributeHelper.showInBrowser(trueURL);
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		if (htmlTextPanel != null)
			return new MainPanelComponent(htmlTextPanel);
		return super.getResultMainPanel();
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewNavigationSet(ArrayList<NavigationButton> currentSet) {
		return null;
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		return null;
	}
	
	@Override
	public IOurl getURL() {
		IOurl referenceURL = bookURL;
		if (trueURL == null)
			if (referenceURL.endsWith(".url") || referenceURL.endsWith(".webloc"))
				try {
					trueURL = IAPservice.getURLfromWeblocFile(referenceURL);
				} catch (Exception e) {
					MongoDB.saveSystemErrorMessage("Could not read webloc-file from " + referenceURL + ".", e);
					return trueURL;
				}
			else
				trueURL = referenceURL;
		return trueURL;
	}
}