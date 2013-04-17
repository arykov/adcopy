package com.ryaltech.utils.ldap;

import java.io.IOException;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;
import javax.naming.ldap.PagedResultsControl;
import javax.naming.ldap.PagedResultsResponseControl;

public class Ldap {
	

	
	public LdapContext connect(String host, int port, String baseDn,
			String principalDn, String credential) throws NamingException {
		Hashtable env = new Hashtable();
		env.put(Context.INITIAL_CONTEXT_FACTORY,
				"com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL,
				String.format("ldap://%s:%s/%s", host, port, baseDn));

		env.put(Context.REFERRAL, "follow");
		
		
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, principalDn);
		env.put(Context.SECURITY_CREDENTIALS, credential);
		
		LdapContext ctx = new InitialLdapContext(env, null);
		return ctx;

	}

	public void pagedFullSearch(SearchCallback callback, LdapContext ctx,
			String baseDn, String filter, String... attributes)
			throws NamingException, IOException {
		final int pageSize = 100;
		ctx.setRequestControls(new Control[] { new PagedResultsControl(
				pageSize, Control.CRITICAL) });

		SearchControls ctls = new SearchControls();
		ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
		ctls.setDerefLinkFlag(true);

		ctls.setReturningAttributes(attributes);

		// Search for objects using the filter

		
		byte[] cookie = null;

		do {
			NamingEnumeration<SearchResult> answer = ctx.search(baseDn, filter,
					ctls);
			while (answer.hasMore()) {
				callback.callback(answer.next());
			}
			Control[] controls = ctx.getResponseControls();
			if (controls != null) {
				for (int j = 0; j < controls.length; j++) {
					if (controls[j] instanceof PagedResultsResponseControl) {
						PagedResultsResponseControl prrc = (PagedResultsResponseControl) controls[j];
						cookie = prrc.getCookie();
						ctx.setRequestControls(new Control[] { new PagedResultsControl(
								pageSize, cookie, Control.CRITICAL) });
					}
				}
			}
		} while (cookie != null);

	}

}
