package de.ipk.ag_ba.commands;

import java.util.ArrayList;

import org.StringManipulationTools;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.commands.datasource.Book;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.webstart.IAPmain;

/**
 * @author Christian Klukas
 */
public class ActionAbout extends AbstractNavigationAction {
	
	public ActionAbout(String tooltip) {
		super(tooltip);
	}
	
	private NavigationButton src;
	
	@Override
	public void performActionCalculateResults(NavigationButton src) throws Exception {
		this.src = src;
	}
	
	@Override
	public MainPanelComponent getResultMainPanel() {
		ArrayList<String> ll = new ArrayList<String>();
		ll.add("<html>" +
				"<table><tr><td>" + // bgcolor='black'
				"<code>"
				// +"<font color='#E58A0E'>"
				+ StringManipulationTools.stringReplace(
						StringManipulationTools.getStringList(IAPmain.getMainInfoLines(), "<br>"),
						" ", "&nbsp;")
				// + "</font>"
				+ "</code></td></tr></table>");
		MainPanelComponent mp = new MainPanelComponent(ll);// , new Color(0, 0, 20));
		return mp;
	}
	
	@Override
	public String getDefaultTitle() {
		return "Help";
	}
	
	@Override
	public String getDefaultImage() {
		return "img/ext/gpl2/Accessories-Dictionary-64.png";
	}
	
	@Override
	public ArrayList<NavigationButton> getResultNewActionSet() {
		ArrayList<NavigationButton> rr = new ArrayList<NavigationButton>();
		Book book = new Book(null, "<html><center>" +
				"User Documentation<br>" +
				"<font color='gray'><small>(online PDF)</small></font></center>",
				new IOurl("http://iap.ipk-gatersleben.de/documentation.pdf"),
				"img/dataset.png");
		rr.add(book.getNavigationButton(src));
		
		Book webUrl = new Book(null, "Source Code",
				new IOurl("http://sourceforge.net/projects/iapg2p"),
				"img/ext/gpl2/Gtk-Dnd-Multiple-64.png");
		rr.add(webUrl.getNavigationButton(src));
		
		Book webUrl2 = new Book(null, "IAP Website",
				new IOurl("http://iap.ipk-gatersleben.de/"),
				"img/browser.png");
		rr.add(webUrl2.getNavigationButton(src));
		
		Book webUrl3 = new Book(null, "Research Group Image Analysis",
				new IOurl("http://www.ipk-gatersleben.de/en/dept-molecular-genetics/image-analysis/"),
				"img/browser.png");
		rr.add(webUrl3.getNavigationButton(src));
		
		return rr;
	}
}
