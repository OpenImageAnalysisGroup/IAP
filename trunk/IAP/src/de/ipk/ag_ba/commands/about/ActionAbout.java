package de.ipk.ag_ba.commands.about;

import java.util.ArrayList;

import org.StringManipulationTools;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
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
				"<code><b>"
				// +"<font color='#E58A0E'>"
				+ StringManipulationTools.stringReplace(
						StringManipulationTools.getStringList(IAPmain.getMainInfoLines(), "<br>"),
						" ", "&nbsp;")
				// + "</font>"
				+ "</b></code></td></tr></table>");
		ll.add("<html><font face=\"Sans,Tohama,Arial\">Privacy</font>" +
				"<hr><font face=\"Sans,Tohama,Arial\">" +
				"IAP does not contain any logging functionality or hidden features which could harm your privacy.<br>" +
				"It connects to the internet only for network related functionality on users request.</font>");
		ll.add("<html><font face=\"Sans,Tohama,Arial\">Disclaimer of Warranties</font>" +
				"<hr><font face=\"Sans,Tohama,Arial\">"
				+
				"You acknowledge and agree that the use of IAP is at your sole risk and that the <br>" +
				"entire risk as to satisfactory quality, performance, accuracy and effort is with you. <br>" +
				"It can not be guaranteed, that the functions contained in this software will meet <br>" +
				"your requirements, that the operation of IAP will be uninterrupted or error free, <br>" +
				"or that errors in the software will be corrected.</font>");
		MainPanelComponent mp = new MainPanelComponent(ll);// , new Color(0, 0, 20));
		return mp;
	}
	
	@Override
	public String getDefaultTitle() {
		return "About";
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
		
		Book webUrl = new Book(null, "Source Code<br>" +
				"<font color='gray'><small>(SourceForge)</small></font></center>",
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
		
		rr.add(new NavigationButton(new ActionAboutPlugins("List of system plugins"), guiSetting));
		rr.add(new NavigationButton(new ActionAboutLicense("List of external library licenses"), guiSetting));
		
		rr.add(new NavigationButton(new ActionFeedback("Send feedback mail"), guiSetting));
		
		return rr;
	}
}
