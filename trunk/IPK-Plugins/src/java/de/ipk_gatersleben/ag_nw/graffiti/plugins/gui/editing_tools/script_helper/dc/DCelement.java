package de.ipk_gatersleben.ag_nw.graffiti.plugins.gui.editing_tools.script_helper.dc;

/**
 * @author klukas
 *         Field names, definition and comment text taken from DC definition documentation.
 *         Information based on DCMI Metadata Terms (2012-06-14): http://dublincore.org/documents/dcmi-terms/#H5
 */
public enum DCelement {
	abstract_("Abstract",
			"A summary of the resource.", null),
	accessRights("Access Rights",
			"Information about who can access the resource or an indication of its security status.",
			"Access Rights may include information regarding access or restrictions based on privacy, security, or other policies."),
	accrualMethod("Accrual Method",
			"The method by which items are added to a collection.", null),
	accrualPeriodicity("Accrual Periodicity",
			"The frequency with which items are added to a collection.", null),
	accrualPolicy("Accrual Policy",
			"The policy governing the addition of items to a collection.", null),
	alternative("Alternative Title",
			"An alternative name for the resource.",
			"The distinction between titles and alternative titles is application-specific."),
	audience("Audience",
			"A class of entity for whom the resource is intended or useful.", null),
	available("Date Available",
			"Date (often a range) that the resource became or will become available.", null),
	bibliographicCitation("Bibliographic Citation",
			"A bibliographic reference for the resource.",
			"Recommended practice is to include sufficient bibliographic detail to identify the resource as unambiguously as possible."),
	conformsTo("Conforms To",
			"An established standard to which the described resource conforms.", null),
	contributor("Contributor",
			"An entity responsible for making contributions to the resource.",
			"Examples of a Contributor include a person, an organization, or a service."),
	coverage(
			"Coverage",
			"The spatial or temporal topic of the resource, the spatial applicability of the resource, or the jurisdiction under which the resource is relevant.",
			"Spatial topic and spatial applicability may be a named place or a location specified by its geographic coordinates. Temporal topic may be a named period, date, or date range. A jurisdiction may be a named administrative entity or a geographic place to which the resource applies. Recommended best practice is to use a controlled vocabulary such as the Thesaurus of Geographic Names [TGN]. Where appropriate, named places or time periods can be used in preference to numeric identifiers such as sets of coordinates or date ranges. "
					+
					"[TGN] http://www.getty.edu/research/tools/vocabulary/tgn/index.html"),
	created("Date Created",
			"Date of creation of the resource.", null),
	creator("Creator",
			"An entity primarily responsible for making the resource.",
			"Examples of a Creator include a person, an organization, or a service."),
	date("Date",
			"A point or period of time associated with an event in the lifecycle of the resource.",
			"Date may be used to express temporal information at any level of granularity. Recommended best practice is to use an encoding scheme, such as the " +
					"W3CDTF profile of ISO 8601 [W3CDTF]. [W3CDTF] http://www.w3.org/TR/NOTE-datetime"),
	dateAccepted("Date Accepted",
			"Date of acceptance of the resource.",
			"Examples of resources to which a Date Accepted may be relevant are a thesis (accepted by a university department) or an article (accepted by a journal)."),
	dateCopyrighted("Date Copyrighted",
			"Date of copyright.", null),
	dateSubmitted("Date Submitted",
			"Date of submission of the resource.",
			"Examples of resources to which a Date Submitted may be relevant are a thesis (submitted to a " +
					"university department) or an article (submitted to a journal)."),
	description("Description",
			"An account of the resource.",
			"Description may include but is not limited to: an abstract, a table of contents, a graphical representation, or a free-text account of the resource."),
	educationLevel("Audience Education Level",
			"A class of entity, defined in terms of progression through an educational or training context, for which the described resource is intended.", null),
	extent("Extent",
			"The size or duration of the resource.", null),
	format("Format",
			"The file format, physical medium, or dimensions of the resource.",
			"Examples of dimensions include size and duration. Recommended best practice is to use a controlled vocabulary such as the list " +
					"of Internet Media Types [MIME]. [MIME] http://www.iana.org/assignments/media-types/"),
	hasFormat("Has Format",
			"A related resource that is substantially the same as the pre-existing described resource, but in another format.", null),
	hasPart("Has Part",
			"A related resource that is included either physically or logically in the described resource.", null),
	hasVersion("Has Version",
			"A related resource that is a version, edition, or adaptation of the described resource.", null),
	identifier("Identifier",
			"An unambiguous reference to the resource within a given context.",
			"Recommended best practice is to identify the resource by means of a string conforming to a formal identification system."),
	instructionalMethod(
			"Instructional Method",
			"A process, used to engender knowledge, attitudes and skills, that the described resource is designed to support.",
			"Instructional Method will typically include ways of presenting instructional materials or conducting instructional activities, patterns of learner-to-learner and learner-to-instructor interactions, and mechanisms by which group and individual levels of learning are measured. Instructional methods include all aspects of the instruction and learning processes from planning and implementation through evaluation and feedback."),
	isFormatOf("Is Format Of",
			"A related resource that is substantially the same as the described resource, but in another format.", null),
	isPartOf("Is Part Of",
			"A related resource in which the described resource is physically or logically included.", null),
	isReferencedBy("Is Referenced By",
			"A related resource that references, cites, or otherwise points to the described resource.", null),
	isReplacedBy("Is Replaced By",
			"A related resource that supplants, displaces, or supersedes the described resource.", null),
	isRequiredBy("Is Required By",
			"A related resource that requires the described resource to support its function, delivery, or coherence.", null),
	issued("Date Issued",
			"Date of formal issuance (e.g., publication) of the resource.", null),
	isVersionOf("Is Version Of",
			"A related resource of which the described resource is a version, edition, or adaptation.",
			"Changes in version imply substantive changes in content rather than differences in format."),
	language("Language",
			"A language of the resource.",
			"Recommended best practice is to use a controlled vocabulary such as RFC 4646 [RFC4646]. [RFC4646] http://www.ietf.org/rfc/rfc4646.txt"),
	license("License",
			"A legal document giving official permission to do something with the resource.", null),
	mediator("Mediator",
			"An entity that mediates access to the resource and for whom the resource is intended or useful.",
			"In an educational context, a mediator might be a parent, teacher, teaching assistant, or care-giver."),
	medium("Medium",
			"The material or physical carrier of the resource.", null),
	modified("Date Modified",
			"Date on which the resource was changed.", null),
	provenance(
			"Provenance",
			"A statement of any changes in ownership and custody of the resource since its creation that are significant for its authenticity, integrity, and interpretation.",
			"The statement may include a description of any changes successive custodians made to the resource."),
	publisher("Publisher",
			"An entity responsible for making the resource available.",
			"Examples of a Publisher include a person, an organization, or a service."),
	references("References",
			"A related resource that is referenced, cited, or otherwise pointed to by the described resource.", null),
	relation("Relation",
			"A related resource.",
			"Recommended best practice is to identify the related resource by means of a string conforming to a formal identification system."),
	replaces("Replaces",
			"A related resource that is supplanted, displaced, or superseded by the described resource.", null),
	requires("Requires",
			"A related resource that is required by the described resource to support its function, delivery, or coherence.", null),
	rights("Rights",
			"Information about rights held in and over the resource.",
			"Typically, rights information includes a statement about various property rights associated with the resource, including intellectual property rights."),
	rightsHolder("Rights Holder",
			"A person or organization owning or managing rights over the resource.", null),
	source(
			"Source",
			"A related resource from which the described resource is derived.",
			"The described resource may be derived from the related resource in whole or in part. Recommended best practice is to identify the related resource by means of a string conforming to a formal identification system."),
	spatial("Spatial Coverage",
			"Spatial characteristics of the resource.", null),
	subject("Subject",
			"The topic of the resource.",
			"Typically, the subject will be represented using keywords, key phrases, or classification codes. Recommended best practice is to use a controlled vocabulary."),
	tableOfContents("Table Of Contents",
			"A list of subunits of the resource.", null),
	temporal("Temporal Coverage",
			"Temporal characteristics of the resource.", null),
	title("Title",
			"A name given to the resource.", null),
	type(
			"Type",
			"The nature or genre of the resource.",
			"Recommended best practice is to use a controlled vocabulary such as the DCMI Type Vocabulary [DCMITYPE]. " +
					"To describe the file format, physical medium, or dimensions of the resource, use the Format " +
					"element. [DCMITYPE] http://dublincore.org/documents/dcmi-type-vocabulary/"),
	valid("Date Valid",
			"Date (often a range) of validity of a resource.", null);
	
	private String label;
	private String definition;
	private String comment;
	
	private DCelement(String label, String definition, String comment) {
		this.label = label;
		this.definition = definition;
		this.comment = comment;
	}
	
	public static String getTermPrefix() {
		return "dcterms:";
	}
	
	public String getTermName() {
		if (this == abstract_)
			return "abstract";
		else
			return this.name();
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getDefinition() {
		return definition;
	}
	
	public String getComment() {
		return comment;
	}
	
	public boolean isNativeField() {
		switch (this) {
			case title:
			case type:
			case source:
			case relation:
			case creator:
			case date:
			case format:
			case identifier:
				return true;
			default:
				return false;
		}
	}
}