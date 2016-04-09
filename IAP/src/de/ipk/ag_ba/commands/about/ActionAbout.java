package de.ipk.ag_ba.commands.about;

import java.awt.Color;
import java.awt.FlowLayout;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import org.StringManipulationTools;
import org.graffiti.plugin.io.resources.IOurl;

import de.ipk.ag_ba.commands.AbstractNavigationAction;
import de.ipk.ag_ba.commands.datasource.Book;
import de.ipk.ag_ba.gui.MainPanelComponent;
import de.ipk.ag_ba.gui.navigation_model.NavigationButton;
import de.ipk.ag_ba.gui.util.FlowLayoutImproved;
import de.ipk.ag_ba.gui.webstart.IAPmain;
import de.ipk.ag_ba.server.task_management.SystemAnalysisExt;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.workflow.WorkflowHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.misc.threading.SystemAnalysis;

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
		String f = "<font face=\"Sans,Tohama,Arial\">";
		String fe = "</font>"; // + " / " + Runtime.class.getPackage().getImplementationVersion()
		ll.add("<html><font face=\"Sans,Tohama,Arial\">System Environment</font>"
				+ "<hr>"
				+ "<table><tr><td>" + f + "Java Version" + fe + "</td><td>" + f + System.getProperty("java.version") + fe + "</td></tr>"
				+ "<tr><td>" + f + "Operating System" + fe + "</td><td>" + f + SystemAnalysis.getOperatingSystem() + fe + "</td></tr>"
				+ "<tr><td>" + f + "User Name" + fe + "</td><td>" + f + SystemAnalysis.getUserName() + fe + "</td></tr>"
				+ "<tr><td>" + f + "Instance Network Name" + fe + "</td><td>" + f + SystemAnalysisExt.getHostNameNiceNoError() + fe + "</td></tr>"
				+ "<tr><td>" + f + "System Load" + fe + "</td><td>" + f + StringManipulationTools.formatNumber(SystemAnalysisExt.getRealSystemCpuLoad(), 1) + fe
				+ "</td></tr></table>");
		MainPanelComponent mp = new MainPanelComponent(ll);// , new Color(0, 0, 20));
		
		boolean addThreeD = false;
		if (addThreeD) {
			ArrayList<JComponent> infos = new ArrayList<JComponent>();
			// infos.add(new AnimateLogoIAP().getFX(false));
			
			for (String txt : ll) {
				final JEditorPane jep = MainPanelComponent.getTextComponent(Color.WHITE, txt);
				infos.add(jep);
			}
			JComponent jp = new JPanel(new FlowLayoutImproved(FlowLayout.LEFT, 20, 20));
			jp.setOpaque(false);
			
			for (JComponent jc : infos)
				jp.add(jc);
			
			jp.validate();
			
			mp = new MainPanelComponent(jp);
		}
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
		
		rr.add(new NavigationButton(new ActionShowRSS("Show content of RSS news feeds"), guiSetting));
		
		Book book = new Book(null, "<html><center>" +
				"User Documentation<br>" +
				"<font color='gray'><small>(online PDF)</small></font></center>",
				new IOurl("http://openimageanalysisgroup.github.io/IAP/documentation.pdf"),
				"img/dataset.png");
		rr.add(book.getNavigationButton(src));
		
		Book webUrl = new Book(null, "Source Code<br>" +
				"<font color='gray'><small>(GitHub)</small></font></center>",
				new IOurl("https://github.com/OpenImageAnalysisGroup/IAP"),
				"img/ext/gpl2/Gtk-Dnd-Multiple-64.png");
		rr.add(webUrl.getNavigationButton(src));
		
		Book webUrl2 = new Book(null, "IAP Website",
				new IOurl("http://openimageanalysisgroup.github.io/IAP/"),
				"img/browser.png");
		rr.add(webUrl2.getNavigationButton(src));
		
		// Book webUrl3 = new Book(null, "Research Group Image Analysis",
		// new IOurl("http://www.ipk-gatersleben.de/en/dept-molecular-genetics/image-analysis/"),
		// "img/browser.png");
		// rr.add(webUrl3.getNavigationButton(src));
		
		JButton b = WorkflowHelper.getAddOnManagerButton();
		if (b.isEnabled())
			rr.add(new NavigationButton(new ActionAddOnManager("Install/configure Add-ons", b), guiSetting));
		rr.add(new NavigationButton(new ActionAboutPlugins("List of system plugins"), guiSetting));
		rr.add(new NavigationButton(new ActionAboutLicense("List of external library licenses"), guiSetting));
		
		rr.add(new NavigationButton(new ActionFeedback("Send feedback mail"), guiSetting));
		rr.add(new NavigationButton(new ActionJavaFX("JavaFX Test"), guiSetting));
		
		return rr;
	}
}
