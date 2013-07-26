package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe;

import java.awt.Color;
import java.awt.Component;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JTabbedPane;

import org.FolderPanel;
import org.JLabelJavaHelpLink;

public class TemplateFileManager {
	
	private static TemplateFileManager instance;
	private ArrayList<TemplateFile> templates;
	private FolderPanel buttonPanelExcelTemplates = null;
	
	private TemplateFileManager() {
		super();
		templates = new ArrayList<TemplateFile>();
	}
	
	public static TemplateFileManager getInstance() {
		if (instance == null)
			instance = new TemplateFileManager();
		return instance;
	}
	
	public Component getTemplateFolderPanel() {
		return buttonPanelExcelTemplates;
	}
	
	private void refreshFolderPanel() {
		if (buttonPanelExcelTemplates == null) {
			buttonPanelExcelTemplates =
								new FolderPanel(
													"Data Input Templates", true, true, false,
													JLabelJavaHelpLink.getHelpActionListener("inputformats"));
			buttonPanelExcelTemplates.setFrameColor(new JTabbedPane().getBackground(), Color.BLACK, 0, 5);
			buttonPanelExcelTemplates.setBackground(null);
			// buttonPanelExcelTemplates.setBackground(Color.white);
			buttonPanelExcelTemplates.setRowColSpacing(5, 0);
			
		}
		
		buttonPanelExcelTemplates.clearGuiComponentList();
		
		for (TemplateFile tf : templates)
			buttonPanelExcelTemplates.addGuiComponentRow(null, tf.getButton(), false);
		buttonPanelExcelTemplates.layoutRows();
	}
	
	public void addTemplate(String title, URL url, RunnableForFile openAfterSaving) {
		templates.add(new TemplateFile(title, url, openAfterSaving));
		refreshFolderPanel();
	}
	
	public void addTemplateFile(TemplateFile templateFile) {
		templates.add(templateFile);
		refreshFolderPanel();
	}
	
}
