package de.ipk_gatersleben.ag_pbi.datahandling;

import java.util.ArrayList;
import java.util.List;

import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings.ChartComponent;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.editcomponents.chart_settings.ChartComponentManager;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TemplateFile;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.layout_control.dbe.TemplateFileManager;

public class Template {
	
	private ArrayList<TemplateFile> templateFiles;
	private TemplateLoader templateLoader;
	private List<ChartComponent> templateComponents;
	
	public Template() {
	}
	
	public void registerTemplate() {
		if (templateFiles != null)
			for (TemplateFile tf : templateFiles)
				TemplateFileManager.getInstance().addTemplateFile(tf);
		if (templateLoader != null)
			TemplateManager.getInstance().addTemplate(this);
		if (templateComponents != null && templateComponents.size() > 0)
			for (ChartComponent cc : templateComponents)
				ChartComponentManager.getInstance().registerChartComponent(cc);
		
	}
	
	public ArrayList<TemplateFile> getTemplateFile() {
		return templateFiles;
	}
	
	public TemplateLoader getTemplateLoader() {
		return templateLoader;
	}
	
	public void setTemplateLoader(TemplateLoader templateLoader) {
		this.templateLoader = templateLoader;
	}
	
	/**
	 * Don't forget to register the created {@link TemplateFile} after creating this object.
	 */
	public void addTemplateFile(TemplateFile templateFile) {
		if (templateFiles == null)
			templateFiles = new ArrayList<TemplateFile>();
		templateFiles.add(templateFile);
	}
	
	public void addTemplateChartComponent(ChartComponent templateComponent) {
		if (this.templateComponents == null)
			templateComponents = new ArrayList<ChartComponent>();
		this.templateComponents.add(templateComponent);
	}
	
}
