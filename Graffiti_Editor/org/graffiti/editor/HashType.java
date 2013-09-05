package org.graffiti.editor;

public enum HashType {
	SHA512("SHA-512"), MD5("MD5");
	
	private String n;
	
	HashType(String n) {
		this.n = n;
	}
	
	@Override
	public String toString() {
		return n;
	}
}
