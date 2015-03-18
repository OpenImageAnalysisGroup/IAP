/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
/*
 * Created on 08.06.2005 by Christian Klukas
 */
package org;

public enum Release {
	DEBUG, RELEASE_IPK, RELEASE_PUBLIC, RELEASE_CLUSTERVIS, KGML_EDITOR;
	
	private static MultipleFileLoader fileLoader = null;
	
	public static void setFileLoadHelper(MultipleFileLoader fileLoader) {
		Release.fileLoader = fileLoader;
	}
	
	public static MultipleFileLoader getFileLoaderHelper() {
		return fileLoader;
	}
	
	@Override
	public String toString() {
		switch (this) {
			case DEBUG:
				return "IPK DEBUG VERSION";
			case RELEASE_CLUSTERVIS:
				return "IPK Cluster Visualization Tool";
			case RELEASE_PUBLIC:
				return "VANTED";
			case KGML_EDITOR:
				return "KGML-ED";
			case RELEASE_IPK:
				return "DBE-Gravisto";
			default:
				return name();
		}
	}
}
