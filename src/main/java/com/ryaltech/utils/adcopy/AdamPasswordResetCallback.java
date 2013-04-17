package com.ryaltech.utils.adcopy;

import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchResult;

import com.ryaltech.utils.ldap.SearchCallback;

public class AdamPasswordResetCallback implements SearchCallback {

	private ModificationItem[] mods = new ModificationItem[2];
	private int counter=1;
	private DirContext ctx;

	AdamPasswordResetCallback(DirContext ctx, String newPassword) {
		this.ctx = ctx;
		mods = new ModificationItem[2];
		mods[0] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
				new BasicAttribute("userPassword", newPassword));
		mods[1] = new ModificationItem(DirContext.REPLACE_ATTRIBUTE,
				new BasicAttribute("msDS-UserAccountDisabled", "FALSE"));
	}

	@Override
	public void callback(SearchResult sr) {			
		String dn = sr.getNameInNamespace();
		if (!"CN=admin,DC=com".equals(dn)) {
			System.out.print((counter++) + ". Resetting password  for user: "
					+ dn);
			try {
				ctx.modifyAttributes(dn, mods);
				System.out.println(": DONE");
			} catch (Exception ex) {
				System.out.println(": FAILED");
			}
		}

	}

}
