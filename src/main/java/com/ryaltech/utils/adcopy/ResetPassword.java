package com.ryaltech.utils.adcopy;

import java.io.IOException;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import com.ryaltech.utils.ldap.Ldap;

public class ResetPassword extends BaseMain {

	/**
	 * @param args
	 * @throws NamingException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws NamingException, IOException {
		Ldap ldap = new Ldap();
		LdapContext ctx = new ResetPassword().getTargetLdapContext();

		
		ldap.pagedFullSearch(new AdamPasswordResetCallback(ctx, "newPassword"),
				ctx, "DC=com", "(&(ObjectCategory=person))");
		ctx.close();

	}

}
