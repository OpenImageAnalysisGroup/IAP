package org;

/**
 * @author Christian Klukas
 */
public interface ErrorMessageProcessor {
	public void reportError(String errorMessage);
	
	public void reportError(Exception exception);
}
