package de.ipk_gatersleben.ag_nw.graffiti.plugins.ios.ldap;

/*******************************************************************************
 * Copyright (c) 2003-2007 Network Analysis Group, IPK Gatersleben
 *******************************************************************************/
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

/**
 * Demonstrates how to retrieve an attribute of a named object.
 * usage: java Getattr
 */
class Getattr {
	public static void main(String[] args) {
		// Identify service provider to use
		Hashtable<String, String> env = new Hashtable<String, String>(11);
		env.put(Context.INITIAL_CONTEXT_FACTORY,
							"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, "ldap://ldap1.ipk-gatersleben.de:389");
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_CREDENTIALS, "<pass>");
		env.put(Context.SECURITY_PRINCIPAL, "uid=klukas,o=agnw,dc=ipk-gatersleben.de,dc=de,ou=people");
		try {
			
			// Create the initial directory context
			DirContext ctx = new InitialDirContext(env);
			
			// Ask for all attributes of the object
			Attributes attrs = ctx.getAttributes("cn=klukas"); // dc=ipk-gatersleben.de,cn=klukas, ou=people
			
			NamingEnumeration<?> ne = attrs.getIDs();
			for (String id = (String) ne.next(); ne.hasMore();) {
				String val = attrs.get(id).toString();
				System.out.println(id + " = " + val);
			}
			
			// Find the surname ("sn") and print it
			System.out.println("sn: " + attrs.get("sn").get());
			
			// Close the context when we're done
			ctx.close();
		} catch (NamingException e) {
			System.err.println("Problem getting attribute: " + e);
		}
	}
}
