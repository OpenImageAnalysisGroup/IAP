package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.ReleaseInfo;
import org.StringManipulationTools;

/**
 * A "DublinCore" mapping helper to access experiment header fields.
 * 
 * @author klukas
 */
public class DCexperimentHeader {
	public enum DCelements {
		/**
		 * The name given to the resource. (name of experiment)
		 */
		TITLE,
		
		/**
		 * The topic of the content of the resource. (extracted from remark field subject#)
		 */
		SUBJECT,
		
		/**
		 * An account of the content of the resource. (extracted from remark field description#)
		 */
		DESCRIPTION,
		
		/**
		 * The nature or genre of the content of the resource. (experiment type)
		 */
		TYPE,
		
		/**
		 * A Reference to a resource from which the present resource is derived. (experiment source database ID).
		 */
		SOURCE,
		
		/**
		 * A reference to a related resource. (experiment origin database ID).
		 */
		RELATION,
		
		/**
		 * The extend or scope of the content of the resource. Coverage will typically include spatial location,
		 * temporal period or jurisdiction. (extracted from remark field coverage#)
		 */
		COVERAGE,
		
		/**
		 * An entity primarily responsible for making the content of the resource. (experiment coordinator)
		 */
		CREATOR,
		
		/**
		 * The entity responsible for making the resource available.
		 * (extracted from remark field publisher#)
		 */
		PUBLISHER,
		
		/**
		 * An entity responsible for making contributions to the content of the resource.
		 * (experiment field import user name)
		 */
		CONTRIBUTOR,
		
		/**
		 * Information about rights held in and over the resource.
		 * (extracted from remark field rights#)
		 */
		RIGHTS,
		
		/**
		 * A data associated with an event in the life cycle of the resource. Typically, Data will be
		 * the creation or availability of the resource.
		 * (experiment import date)
		 */
		DATE,
	}
	
	private final ExperimentHeaderInterface ehi;
	
	public DCexperimentHeader(ExperimentHeaderInterface ehi) {
		this.ehi = ehi;
	}
	
	/**
	 * @return The name given to the resource. (name of experiment)
	 */
	public String getTitle() {
		return ehi.getExperimentName();
	}
	
	/**
	 * @return The topic of the content of the resource. (extracted from remark field subject#)
	 */
	public String getSubject() {
		return StringManipulationTools.getAnnotationProcessor(ehi.getRemark()).getAnnotationField("subject");
	}
	
	/**
	 * @return An account of the content of the resource. (extracted from remark field description#)
	 */
	public String getDescription() {
		return StringManipulationTools.getAnnotationProcessor(ehi.getRemark()).getAnnotationField("description");
	}
	
	/**
	 * @return The nature or genre of the content of the resource. (experiment type)
	 */
	public String getType() {
		return ehi.getExperimentType();
	}
	
	/**
	 * @return A Reference to a resource from which the present resource is derived. (experiment source database ID).
	 */
	public String getSource() {
		return ehi.getDatabaseId();
	}
	
	/**
	 * @return A reference to a related resource. (experiment origin database ID).
	 */
	public String getRelation() {
		return ehi.getOriginDbId();
	}
	
	/**
	 * @return The extend or scope of the content of the resource. Coverage will typically include spatial location,
	 *         temporal period or jurisdiction. (extracted from remark field coverage#)
	 */
	public String getCoverage() {
		return StringManipulationTools.getAnnotationProcessor(ehi.getRemark()).getAnnotationField("coverage");
	}
	
	/**
	 * @return An entity primarily responsible for making the content of the resource. (experiment coordinator)
	 */
	public String getCreator() {
		return ehi.getCoordinator();
	}
	
	/**
	 * @return The entity responsible for making the resource available.
	 *         (extracted from remark field publisher#)
	 */
	public String getPublisher() {
		return StringManipulationTools.getAnnotationProcessor(ehi.getRemark()).getAnnotationField("publisher");
	}
	
	/**
	 * @return An entity responsible for making contributions to the content of the resource.
	 *         (experiment field import user name)
	 */
	public String getContributor() {
		return ehi.getImportusername();
	}
	
	/**
	 * @return Information about rights held in and over the resource.
	 *         (extracted from remark field rights#)
	 */
	public String getRights() {
		return StringManipulationTools.getAnnotationProcessor(ehi.getRemark()).getAnnotationField("rights");
	}
	
	/**
	 * @return A data associated with an event in the life cycle of the resource. Typically, Data will be
	 *         the creation or availability of the resource.
	 *         (experiment import date)
	 */
	public String getDate() {
		TimeZone tz = TimeZone.getTimeZone("UTC");
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
		df.setTimeZone(tz);
		return df.format(ehi.getImportdate());
	}
	
	/**
	 * @return The physical or digital manifestation of the resource.
	 *         ("IAP:Integrated Analysis Platform:Version String")
	 */
	public String getFormat() {
		return "IAP:Integrated Analysis Platform:V" + ReleaseInfo.IAP_VERSION_STRING;
	}
	
	/**
	 * @return An unambiguous reference to the resource within a given context.
	 *         (experiment database ID)
	 */
	public String getIdentifier() {
		return ehi.getDatabaseId();
	}
	
	/**
	 * @return A language of the intellectual content of the resource.
	 *         (extracted from remark field language#)
	 *         (e.g. "en-US")
	 */
	public String getLanguage() {
		return StringManipulationTools.getAnnotationProcessor(ehi.getRemark()).getAnnotationField("language");
	}
	
	/**
	 * @return A class of entity for whom the resource is intended or useful.
	 *         (extracted from remark field audience#)
	 */
	public String getAudience() {
		return StringManipulationTools.getAnnotationProcessor(ehi.getRemark()).getAnnotationField("audience");
	}
	
	public String getProvenance() {
		return null;
	}
}
