package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.dc;

import java.awt.event.ActionEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TimeZone;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.ReleaseInfo;
import org.StringManipulationTools;

import de.ipk_gatersleben.ag_nw.graffiti.MyInputHelper;
import de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.ExperimentHeaderInterface;

/**
 * A "DublinCore" mapping helper to access experiment header fields.
 * 
 * @author klukas
 */
public class DCexperimentHeader {
	private final ExperimentHeaderInterface ehi;
	
	public Collection<String> getDCfield(DCelements field) {
		switch (field) {
			case date:
				ArrayList<String> dateList = new ArrayList<String>();
				dateList.add(getDate());
				return dateList;
			case creator:
				return StringManipulationTools.getStringListFromArray(split(getCreator()));
			case format:
				return StringManipulationTools.getStringListFromArray(split(getFormat()));
			case identifier:
				return StringManipulationTools.getStringListFromArray(split(getIdentifier()));
			case relation:
				return StringManipulationTools.getStringListFromArray(split(getRelation()));
			case source:
				return StringManipulationTools.getStringListFromArray(split(getSource()));
			case title:
				return StringManipulationTools.getStringListFromArray(split(getTitle()));
			case type:
				return StringManipulationTools.getStringListFromArray(split(getType()));
			default:
				return StringManipulationTools.getStringListFromArray(
						split(StringManipulationTools.getAnnotationProcessor(
								ehi.getAnnotation()).getAnnotationField(field.name())));
		}
	}
	
	private String[] split(String val) {
		if (val == null)
			return null;
		else
			return val.split("\\|");
	}
	
	public void setDCfield(DCelements field, Collection<String> newValues) {
		StringManipulationTools.getAnnotationProcessor(ehi.getAnnotation())
				.replaceAnnotationField(field.name(), StringManipulationTools.getStringList(newValues, "|"));
	}
	
	public void addDCfieldValue(DCelements field, Collection<String> newValues) {
		Collection<String> currentValues = getDCfield(field);
		if (currentValues == null || currentValues.isEmpty())
			currentValues = newValues;
		else
			currentValues.addAll(newValues);
		
		StringManipulationTools.getAnnotationProcessor(ehi.getAnnotation())
				.replaceAnnotationField(field.name(), StringManipulationTools.getStringList(currentValues, "|"));
	}
	
	public DCexperimentHeader(ExperimentHeaderInterface ehi) {
		this.ehi = ehi;
	}
	
	/**
	 * @return The name given to the resource. (name of experiment)
	 */
	private String getTitle() {
		return ehi.getExperimentName();
	}
	
	/**
	 * @return The nature or genre of the content of the resource. (experiment type)
	 */
	private String getType() {
		return ehi.getExperimentType();
	}
	
	/**
	 * @return A Reference to a resource from which the present resource is derived. (experiment source database ID).
	 */
	private String getSource() {
		return ehi.getDatabaseId();
	}
	
	/**
	 * @return A reference to a related resource. (experiment origin database ID).
	 */
	private String getRelation() {
		return ehi.getOriginDbId();
	}
	
	/**
	 * @return An entity primarily responsible for making the content of the resource. (experiment coordinator)
	 */
	private String getCreator() {
		return ehi.getCoordinator();
	}
	
	/**
	 * @return A data associated with an event in the life cycle of the resource. Typically, Data will be
	 *         the creation or availability of the resource.
	 *         (experiment import date)
	 */
	private String getDate() {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		df.setTimeZone(tz);
		return df.format(ehi.getImportdate());
	}
	
	/**
	 * @return The physical or digital manifestation of the resource.
	 *         ("IAP:Integrated Analysis Platform:Version String")
	 */
	private String getFormat() {
		return "IAP:Integrated Analysis Platform:V" + ReleaseInfo.IAP_VERSION_STRING;
	}
	
	/**
	 * @return An unambiguous reference to the resource within a given context.
	 *         (experiment database ID)
	 */
	private String getIdentifier() {
		return ehi.getDatabaseId();
	}
	
	public String getHTMLoverview() {
		int rows = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("<html><table><tr><th>Field</th><th>Value</th></tr>");
		for (DCelements field : DCelements.values()) {
			Collection<String> valueList = getDCfield(field);
			if (valueList != null) {
				for (String value : valueList) {
					if (value != null && !value.isEmpty()) {
						if (value.length() > 80)
							value = value.substring(0, 77) + "...";
						sb.append("<tr><td>" + field.getLabel() + "</td><td>" + value + "</td></tr>");
					}
				}
			}
		}
		sb.append("</table>");
		if (rows > 0)
			return sb.toString();
		else
			return "<html>(no additional meta data information defined)";
	}
	
	public JComponent getEditButton(JLabel display, final String title) {
		Action action = new AbstractAction(title) {
			private static final long serialVersionUID = 1L;
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ArrayList<Object> descAndValue = new ArrayList<Object>();
				for (DCelements elem : DCelements.values()) {
					if (elem.isNativeField())
						continue;
					Collection<String> value = getDCfield(elem);
					if (value == null || value.isEmpty()) {
						value = new ArrayList<String>();
						value.add("");
					}
					JLabel desc = new JLabel("<html><b>" + elem.getLabel() + "</b><br><smalL><font color='gray'>" +
							StringManipulationTools.getWordWrap(elem.getDefinition(), 40));
					String comment = elem.getComment();
					if (comment != null && !comment.isEmpty())
						desc.setText(desc.getText() + " (...)@@<html>" + StringManipulationTools.getWordWrap(comment, 60));
					else
						desc.setText(desc.getText() + "@@");
					descAndValue.add(desc);
					descAndValue.add(StringManipulationTools.getStringList(value, " | "));
				}
				MyInputHelper
						.getInput(
								"Listed are additional annotation fields as defined by the Dublin Core (R) Metadata Initiative.<br>"
										+
										"Some of the default experiment header fields are automatically mapped to DCMI-terms, these are not included in this list.<br><br>"
										+ "Important: At the moment the following characters can't be used in any field text: <b>;</b>, <b>#</b>.<br>" +
										"These characters are automatically replaced by <b>_</b>.<br><br>" +
										"Fields with a description, ending with (...), provide additional documentation, move the mouse over the according label<br>" +
										"in order to display the field definition comments." +
										"<br><br>" +
										"Edit meta data (use '|' to split multiple items):<br><br>",
								"Annotation",
								descAndValue.toArray());
			}
		};
		JButton res = new JButton(action);
		res.setToolTipText("Click to edit the assigned meta data");
		return res;
	}
}
