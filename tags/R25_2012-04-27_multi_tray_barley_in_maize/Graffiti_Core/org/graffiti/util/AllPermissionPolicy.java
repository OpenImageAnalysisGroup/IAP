package org.graffiti.util;

import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;

public class AllPermissionPolicy extends Policy {
	private Permissions p;
	
	public AllPermissionPolicy() {
		p = new Permissions();
		
		AllPermission ap = new AllPermission();
		
		p.add(ap);
	}
	
	@Override
	public PermissionCollection getPermissions(CodeSource cs) {
		return p;
	}
	
	@Override
	public void refresh() {
	}
}