package com.giisoo.core.index;

import java.util.List;

import org.apache.commons.configuration.Configuration;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Field.Store;

import com.giisoo.core.bean.*;
import com.giisoo.core.conf.Config;

/**
 * The Class Searchable.
 */
public abstract class Searchable extends Bean {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 2L;

	/** The date. */
	long date;

	/** The score. */
	float score;

	/**
	 * Gets the score.
	 * 
	 * @return the score
	 */
	public float getScore() {
		return score;
	}

	/**
	 * Sets the score.
	 * 
	 * @param score
	 *            the new score
	 */
	public void setScore(float score) {
		this.score = score;
	}

	/**
	 * Gets the date.
	 * 
	 * @return the date
	 */
	public long getDate() {
		return date;
	}

	/**
	 * Sets the date.
	 * 
	 * @param date
	 *            the new date
	 */
	public void setDate(long date) {
		this.date = date;
	}

	/**
	 * Gets the id.
	 * 
	 * @return the id
	 */
	public abstract String getId();

	/**
	 * Load.
	 * 
	 * @param id
	 *            the id
	 * @return true, if successful
	 */
	public abstract boolean load(String id);

	/**
	 * Checks if is valid.
	 * 
	 * @return true, if is valid
	 */
	public boolean isValid() {
		return true;
	}

	/**
	 * Group.
	 * 
	 * @return the int
	 */
	protected int group() {
		return Integer.MAX_VALUE;
	}

	/**
	 * Touch.
	 */
	public void touch() {
		PendIndex.create(this.getClass(), getId());
	}

	/**
	 * Gets the location.
	 * 
	 * @return the location
	 */
	public Location getLocation() {
		return null;
	}

	/**
	 * Sets the distance.
	 * 
	 * @param meter
	 *            the new distance
	 */
	public void setDistance(int meter) {

	}

	/**
	 * To doc.
	 * 
	 * @param doc
	 *            the doc
	 * @return the document
	 */
	@SuppressWarnings("deprecation")
	protected Document toDoc(Document doc) {
		SearchableField[] ff = this.getSearchableFields();
		if (ff != null) {
			for (SearchableField f : ff) {
				if (f == null)
					continue;

				String v = this.getValue(f);
				if (v != null) {
					// Field f1 = new StringField(f.field, v, Field.Store.YES);
					Field f1 = new Field(f.field, v, Field.Store.YES,
							Field.Index.ANALYZED,
							Field.TermVector.WITH_POSITIONS_OFFSETS);
					doc.add(f1);
				}
			}

			// BytesRef br = new BytesRef(group());
			// SortedDocValuesField f1 = new SortedDocValuesField(X.GROUP, br);
			IntField f1 = new IntField(X.GROUP, group(), Store.YES);

			doc.add(f1);

		}

		return doc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o instanceof Searchable) {
			return getId().equals(((Searchable) o).getId());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return getId().hashCode();
	}

	/**
	 * Better than.
	 * 
	 * @param o
	 *            the o
	 * @return true, if successful
	 */
	public boolean betterThan(Searchable o) {
		return true;
	}

	/**
	 * Removes the.
	 */
	public void remove() {
	}

	/**
	 * The Class SearchableField.
	 */
	public static class SearchableField {

		/** The field. */
		public String field;

		/** The boost. */
		public int boost;

		public String display;

		/** The simliarfactor. */
		public int simliarfactor;

		/**
		 * Instantiates a new searchable field.
		 * 
		 * @param field
		 *            the field
		 * @param boost
		 *            the boost
		 */
		public SearchableField(String field, int boost) {
			this.field = field;
			this.boost = boost;
		}

		/**
		 * Instantiates a new searchable field.
		 * 
		 * @param field
		 *            the field
		 * @param boost
		 *            the boost
		 * @param simliarfactor
		 *            the simliarfactor
		 */
		public SearchableField(String field, int boost, int simliarfactor) {
			this.field = field;
			this.boost = boost;
			this.simliarfactor = simliarfactor;
		}
	}

	/**
	 * 
	 * by default, get the searchablefield from the configuration file, the
	 * mechanism is: Class.searchable=field:boost:simliarfactor:display
	 * 
	 * @return SearchableField[]
	 */
	@SuppressWarnings("unchecked")
	public SearchableField[] getSearchableFields() {
		if (searchablefields == null) {
			Configuration conf = Config.getConfig();
			List<String> list = conf.getList(this.getClass().getName()
					+ ".searchable");
			SearchableField[] tt = new SearchableField[list.size()];
			for (int i = 0; i < list.size(); i++) {
				String[] ss = list.get(i).split(":");
				if (ss.length > 2) {
					tt[i] = new SearchableField(ss[0], Integer.parseInt(ss[1]),
							Integer.parseInt(ss[2]));
				} else if (ss.length > 1) {
					tt[i] = new SearchableField(ss[0], Integer.parseInt(ss[1]));
				}
				if (ss.length > 3) {
					tt[i].display = "{" + ss[3] + "}";
				}
			}
			searchablefields = tt;
		}
		return searchablefields;
	}

	private static SearchableField[] searchablefields;

	/**
	 * by default, get the value from the map
	 * 
	 * @param f
	 * @return String
	 */
	public String getValue(SearchableField f) {
		return (String) this.get(f.field);
	}

	/**
	 * by default, set the value to map
	 * 
	 * @param f
	 * @param value
	 */
	public void setValue(SearchableField f, String value) {
		this.set(f.field, value);
	}

}
