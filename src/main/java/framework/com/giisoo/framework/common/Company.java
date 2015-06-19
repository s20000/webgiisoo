package com.giisoo.framework.common;

import java.util.List;

import com.giisoo.core.bean.Bean;
import com.giisoo.core.bean.Beans;
import com.giisoo.core.bean.DBMapping;
import com.giisoo.core.bean.UID;
import com.giisoo.core.bean.X;
import com.mongodb.BasicDBObject;

@DBMapping(collection = "company")
public class Company extends Bean {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int getId() {
		return this.getInt(X._ID);
	}

	public static int create(V v) {
		int id = (int) UID.next("company");
		if (Bean.insertCollection(
				v.set(X._ID, id).set("created", System.currentTimeMillis()),
				Company.class) > 0) {
			return id;
		}

		return -1;
	}

	public static int delete(int id) {
		return Bean
				.delete(new BasicDBObject().append(X._ID, id), Company.class);
	}

	public static int update(int id, V v) {
		return Bean.updateCollection(id, v, Company.class);
	}

	public static Beans<Company> load(BasicDBObject query,
			BasicDBObject orderby, int offset, int limit) {
		if (query == null) {
			query = new BasicDBObject();
		}
		return Bean.load(query, orderby, offset, limit, Company.class);
	}

	public static Company load(BasicDBObject query, BasicDBObject order) {
		return Bean.load(query, order, Company.class);
	}

	public static Company load(int id) {
		return Bean.load(new BasicDBObject().append(X._ID, id), Company.class);
	}

	public Beans<Department> load(BasicDBObject orderby, int offset, int limit) {
		return Department.load(
				new BasicDBObject().append("companyid", this.getId()), orderby,
				offset, limit);
	}

	public List<Department> getDepartments() {
		Beans<Department> bs = Department.load(
				new BasicDBObject().append("companyid", this.getId()),
				new BasicDBObject().append("name", 1), 0, 1000);

		if (bs != null) {
			return bs.getList();
		}
		return null;
	}

	@DBMapping(collection = "department")
	public static class Department extends Bean {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public long getId() {
			return this.getLong(X._ID);
		}

		public static long create(int companyid, V v) {
			long id = (((long) companyid) << 32) + (int) UID.next("department");
			Bean.insertCollection(v.set(X._ID, id).set("companyid", companyid)
					.set("created", System.currentTimeMillis()),
					Department.class);

			return id;
		}

		public static int delete(long id) {
			return Bean.delete(new BasicDBObject().append(X._ID, id),
					Department.class);
		}

		public static int update(long id, V v) {
			return Bean.updateCollection(id, v, Department.class);
		}

		public static Beans<Department> load(BasicDBObject query,
				BasicDBObject orderby, int offset, int limit) {
			return Bean.load(query, orderby, offset, limit, Department.class);
		}

		public static Department load(BasicDBObject query, BasicDBObject order) {
			return Bean.load(query, order, Department.class);
		}

		public static Department load(long id) {
			return Bean.load(new BasicDBObject().append(X._ID, id),
					Department.class);
		}

		public Company getCompany() {
			if (this.get("company_obj") == null) {
				Company c = Company.load(this.getInt("companyid"));
				this.set("company_obj", c);
			}

			return (Company) this.get("company_obj");
		}

	}

}
