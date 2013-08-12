package de.ipk_gatersleben.ag_pbi.datahandling;

import java.util.ArrayList;
import java.util.Collection;

public class TemplateManager {
	
	private static Collection<Template> templates;
	private static TemplateManager instance;
	
	private TemplateManager() {
		super();
		templates = new ArrayList<Template>();
	}
	
	public static TemplateManager getInstance() {
		if (instance == null)
			instance = new TemplateManager();
		return instance;
	}
	
	public void addTemplate(Template template) {
		templates.add(template);
	}
	
	public static Collection<Template> getTemplates() {
		return templates;
	}
	
	public static String[] getAllValidExtensions() {
		ArrayList<String> validExtensions = new ArrayList<String>();
		for (Template t : templates)
			for (String ext : t.getTemplateLoader().getValidExtensions())
				validExtensions.add(ext);
		return validExtensions.toArray(new String[] {});
	}
}
