package com.ryaltech.utils.adcopy;

import java.io.IOException;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.LdapContext;

import com.ryaltech.utils.ldap.SearchCallback;

public class CopyGroups extends BaseMain{
	/**
	 * @param args
	 * @throws NamingException
	 * @throws IOException
	 */
	public static void main(String[] args) throws Exception{
		CopyGroups main = new CopyGroups();
		for(String sourceBaseDn:main.getBaseDNs()){
			main.copyGroups(sourceBaseDn);
		}
	}
	public  void copyGroups(String baseDn) throws Exception{
		
		final LdapContext ldapAD = getSourceLdapContext();
		final LdapContext ldapAdam = getTargetLdapContext();
		ldap.pagedFullSearch(new SearchCallback() {

			@Override
			public void callback(SearchResult sr) {
				try {
					String escapedName = escape(sr.getNameInNamespace());
					
					try{
						//already exists
						ldapAdam.lookup(escapedName);
						return;
					}catch(Exception ex){
						System.out.print("Trying to create missing group: "+escapedName);
						
						Attributes attributes=new BasicAttributes();
						Attribute objectClass=new BasicAttribute("objectClass");
						objectClass.add("top");
						attributes.put(objectClass);
						objectClass=new BasicAttribute("objectClass");
						objectClass.add("group");
						attributes.put(objectClass);
						
						Attribute cn=new BasicAttribute("cn");						
						cn.add(escape(sr.getAttributes().get("cn").get().toString().split("\n")[0]));						
						attributes.put(cn);
						
						try {
							ldapAdam.createSubcontext(escapedName,
									attributes);
							System.out.println(": SUCCESS");
						} catch (Exception ex1) {
							System.out.println(": FAIL");
							System.err.println("Failed to create: "+sr.getNameInNamespace()+" due to:");
							ex1.printStackTrace(System.err);
						}
						
					}
					
				} catch (Exception ex) {
				}
			}
		}, ldapAD, baseDn, "(&(objectClass=group))", "cn");

	}
}
