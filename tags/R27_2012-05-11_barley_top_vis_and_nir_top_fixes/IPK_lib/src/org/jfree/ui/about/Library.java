package org.jfree.ui.about;

/**
 * Library specification moved to base package to allow more control
 * over the boot process.
 * 
 * @deprecated shadow class for deprecation
 */
public class Library extends org.jfree.base.Library {

	/**
	 * Creates a new library reference.
	 * 
	 * @param name
	 *           the name.
	 * @param version
	 *           the version.
	 * @param licence
	 *           the licence.
	 * @param info
	 *           the web address or other info.
	 */
	public Library(final String name, final String version, final String licence, final String info) {
		super(name, version, licence, info);
	}

	/**
	 * Constructs a library reference from a ProjectInfo object.
	 * 
	 * @param project
	 *           information about a project.
	 */
	public Library(final ProjectInfo project) {

		this(project.getName(), project.getVersion(),
							project.getLicenceName(), project.getInfo());
	}
}
