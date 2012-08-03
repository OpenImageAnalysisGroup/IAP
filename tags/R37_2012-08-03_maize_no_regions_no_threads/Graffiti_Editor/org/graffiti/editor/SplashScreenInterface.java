/*
 * Created on 28.07.2004 by Christian Klukas
 */
package org.graffiti.editor;

import org.graffiti.util.ProgressViewer;

/**
 * @author Christian Klukas
 *         (c) 2004 IPK-Gatersleben
 */
public interface SplashScreenInterface extends ProgressViewer {
	/*
	 * @see org.graffiti.util.ProgressViewer#setMaximum(int)
	 */public abstract void setMaximum(int maximum);
	
	/*
	 * @see org.graffiti.util.ProgressViewer#setText(java.lang.String)
	 */public abstract void setText(String text);
	
	/*
	 * @see org.graffiti.util.ProgressViewer#setValue(int)
	 */public abstract void setValue(int value);
	
	/*
	 * @see org.graffiti.util.ProgressViewer#getValue()
	 */public abstract int getValue();
	
	/**
	 * @param b
	 */
	public abstract void setVisible(boolean b);
	
	public abstract void setInitialisationFinished();
}