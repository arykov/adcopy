package com.ryaltech.utils.adcopy;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import com.ryaltech.utils.ldap.Ldap;
import com.ryaltech.utils.ldap.SearchCallback;

public class CopyGroupMemberships extends BaseMain{
	/**
	 * @param args
	 * @throws NamingException
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception{
		CopyGroupMemberships main = new CopyGroupMemberships();
		for(String sourceBaseDn:main.getBaseDNs()){
			main.copyGroupMembership(sourceBaseDn);
		}
	}
	  void copyGroupMembership(String baseDn)throws Exception{
		
		final LdapContext ldapAD = getSourceLdapContext();
		final LdapContext ldapAdam = getTargetLdapContext();
		ldap.pagedFullSearch(new SearchCallback() {

			@Override
			public void callback(SearchResult sr) {
				try {
					Set<String> localMembers;
					String escapedName = escape(sr.getNameInNamespace());
					try {
						// check existence
						ldapAdam.lookup(escapedName);
						localMembers = getGroupMembers(ldap, escapedName,
								ldapAdam);

					} catch (Exception ex) {
						System.out.println("Missing group: " + escapedName);
						return;

					}

					Set<String> remoteMembers = getGroupMembers(sr);
					if (localMembers.containsAll(remoteMembers)){
						System.out.println("Group "+escapedName +" already has all required members");
						return;
					}
					Attributes attributes = new BasicAttributes();

					Attribute attribute = new BasicAttribute("member");
					

					for (String remoteMember : remoteMembers) {
						
						try {
							ldapAdam
									.lookup(remoteMember);
							System.out.println("Group "+escapedName+" will have a member "+remoteMember
									+ ".");
							attribute.add(remoteMember);
						} catch (Exception ex) {

							System.out.println("Group "+escapedName+" will be missing "+remoteMember
									+ " since it does not exist locally.");
						}
					}
					attributes.put(attribute);
					ldapAdam.modifyAttributes(escapedName,
							DirContext.REPLACE_ATTRIBUTE, attributes);
				} catch (Exception ex) {
					ex.printStackTrace();

				}
			}
		}, ldapAD, baseDn, "(&(objectClass=group))", "member");

	}

	 Set<String> getGroupMembers(SearchResult sr) throws Exception {
		final Set<String> members = new HashSet<String>();
		Attribute memberField = sr.getAttributes().get("member");
		if (memberField != null) {
			NamingEnumeration e = memberField.getAll();
			while (e.hasMore()) {
				members.add(escape((String) e.next()));

			}
		}
		return members;

	}

	Set<String> getGroupMembers(Ldap ldap, String groupDn,
			LdapContext ctx) throws Exception {
		final Set<String> members = new HashSet<String>();
		ldap.pagedFullSearch(new SearchCallback() {

			@Override
			public void callback(SearchResult sr) {
				// should only be one
				try {
					members.addAll(getGroupMembers(sr));
				} catch (Exception e) {
				}

			}
		}, ctx, groupDn, "(objectClass=group)", "member");
		return members;

	}

}
