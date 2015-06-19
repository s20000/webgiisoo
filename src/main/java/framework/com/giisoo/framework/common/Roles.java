/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.common;

import java.util.*;

import com.giisoo.core.bean.*;

/**
 * group roles
 * 
 * @author yjiang
 * 
 */
public class Roles extends Bean {

	/**
   * 
   */
	private static final long serialVersionUID = 1L;

	private Set<String> access;

	List<Role> list;

	public List<Role> getList() {
		return list;
	}

	public Set<String> getAccesses() {
		return access;
	}

	/**
	 * Instantiates a new roles.
	 * 
	 * @param roles
	 *            the roles
	 */
	public Roles(List<Integer> roles) {
		if (access == null) {
			access = new HashSet<String>();
			list = Role.loadAll(roles);

			for (Role r : list) {
				List<String> names = Role.getAccess(r.id);
				access.addAll(names);
			}
		}
	}

	/**
	 * Checks for access.
	 * 
	 * @param name
	 *            the name
	 * @return true, if successful
	 */
	public boolean hasAccess(String... name) {
		if (name == null) {
			return true;
		}

		for (String s : name) {
			if (access.contains(s)) {
				return true;
			}

			/**
			 * test the name exists in while access? if not then add it in DB
			 */
			if (s != null && !"".equals(s)) {
				Access.set(s);
			}

			/**
			 * check if has admin ?
			 */
			int i = s.lastIndexOf(".");
			if (i > 0) {
				String s1 = s.substring(0, i) + ".admin";
				if (access.contains(s1)) {
					return true;
				}
			}
		}

		return access.contains("access.admin");
	}

}
