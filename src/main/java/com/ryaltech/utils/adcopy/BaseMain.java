package com.ryaltech.utils.adcopy;

import java.io.InputStream;
import java.util.Properties;

import javax.naming.NamingException;
import javax.naming.ldap.LdapContext;

import com.ryaltech.utils.ldap.Ldap;

public class BaseMain {
	private String sourceHost,targetHost,sourcePrincipalDn, targetPrincipalDn, sourceCredential, targetCredential;
	int sourcePort, targetPort;
	String[] sourceBaseDns;
	
	Ldap ldap = new Ldap();
	BaseMain(){
		InputStream inStream = BaseMain.class
				.getResourceAsStream("/config.properties");
		Properties props;
		try {
			props = new Properties();
			props.load(inStream);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		} finally {
			try {
				inStream.close();
			} catch (Exception ex) {
			}
		}
		sourceHost = props.getProperty("source.host");
		sourcePrincipalDn = props.getProperty("source.principalDN");
		sourceCredential = props.getProperty("source.credential");
		sourcePort = Integer.parseInt(props.getProperty("source.port"));
		sourceBaseDns = props.getProperty("source.baseDNs").split(";");
		
		targetHost = props.getProperty("target.host");
		targetPrincipalDn = props.getProperty("target.principalDN");
		targetCredential = props.getProperty("target.credential");
		targetPort = Integer.parseInt(props.getProperty("target.port"));
		
	}
	

	public String[] getBaseDNs(){
		return sourceBaseDns;
	}
	public String escape(String str) {

		str = str
				.replaceAll(
						"\\\\0ACNF:[a-f0-9]{8}+-[a-f0-9]{4}+-[a-f0-9]{4}+-[a-f0-9]{4}+-[a-f0-9]{12}+",
						"");
		str = str.replace("/", "\\/");
		return str;

	}

	LdapContext getTargetLdapContext() throws NamingException {
		return ldap.connect(targetHost, targetPort, "", targetPrincipalDn, targetCredential);
	}

	LdapContext getSourceLdapContext() throws NamingException {
		return ldap.connect(sourceHost, sourcePort, "", sourcePrincipalDn, sourceCredential);
	}

}
