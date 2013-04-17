package com.ryaltech.utils.ldap;

import javax.naming.directory.SearchResult;

public interface SearchCallback {
	void callback(SearchResult sr);
}