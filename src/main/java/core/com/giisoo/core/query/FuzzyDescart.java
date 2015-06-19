package com.giisoo.core.query;

import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * The Class FuzzyDescart.
 */
public class FuzzyDescart {

  /** The list. */
  List<String> list;
  
  /** The desc list. */
  List<List<String>> descList;

  /**
   * Instantiates a new fuzzy descart.
   *
   * @param list the list
   */
  public FuzzyDescart(List<String> list) {
    this.list = list;
    _parse();
  }

  /**
   * _parse.
   */
  private void _parse() {
    if (list == null) {
      return;
    }

    List<Set<String>> temp = new ArrayList<Set<String>>(list.size());
    for (String s : list) {
      Set<String> l1 = Fuzzy.getFuzzy(s);
      if (l1 == null) {
        l1 = new HashSet<String>(1);
        l1.add(s);
      }

      if (!temp.contains(l1)) {
        temp.add(l1);
      }
    }

    descList = descart(temp);

  }

  /**
   * Descart.
   *
   * @param list the list
   * @return the list
   */
  private List<List<String>> descart(List<Set<String>> list) {
    List<List<String>> rlist = new ArrayList<List<String>>();

    for (Set<String> l1 : list) {
      List<List<String>> temp = new ArrayList<List<String>>(l1.size() * (rlist.size() + 1));

      if (rlist.size() == 0) {
        for (String s : l1) {
          List<String> t2 = new ArrayList<String>(1);
          if (!t2.contains(s)) {
            t2.add(s);
          }
          temp.add(t2);
        }
      } else {
        for (List<String> l2 : rlist) {
          if (l1.size() == 1) {
            l2.addAll(l1);
            temp.add(l2);
          } else {
            for (String s : l1) {
              List<String> l3 = new ArrayList<String>(l2);
              if (!l3.contains(s)) {
                l3.add(s);
              }

              temp.add(l3);
            }
          }
        }
      }

      rlist = temp;
    }

    return rlist;
  }

  /** The it. */
  Iterator<List<String>> it;

  /**
   * Next.
   *
   * @return the list
   */
  public List<String> next() {
    if (descList == null)
      return null;

    if (it.hasNext()) {
      return it.next();
    }

    return null;
  }

  /**
   * First.
   *
   * @return the list
   */
  public List<String> first() {
    it = descList.iterator();
    return next();
  }
}
