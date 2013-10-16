package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.ErrorMsg;

public class Messages
{
	private static final String BUNDLE_NAME = "de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.biopax.messages"; //$NON-NLS-1$
	
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
	
	private Messages()
	{
	}
	
	public static String getString(String key)
	{
		try
		{
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e)
		{
			ErrorMsg.addErrorMessage(e);
			return '!' + key + '!';
		}
	}
}
