/*
 * (c) COPYRIGHT 1999 World Wide Web Consortium
 * (Massachusetts Institute of Technology, Institut National de Recherche
 * en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 * $Id: ContentCondition.java,v 1.1 2011-01-31 09:02:20 klukas Exp $
 */
package org.w3c.css.sac;

/**
 * @version $Revision: 1.1 $
 * @author Philippe Le Hegaret
 * @see Condition#SAC_CONTENT_CONDITION
 */
public interface ContentCondition extends Condition {
	/**
	 * Returns the content.
	 */
	public String getData();
}
