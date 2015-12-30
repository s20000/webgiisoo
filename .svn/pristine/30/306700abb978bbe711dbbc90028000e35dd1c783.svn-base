package com.giisoo.core.index;

import java.sql.*;
import java.util.*;

import com.giisoo.core.bean.Bean;

// TODO: Auto-generated Javadoc
/**
 * The Class PendIndex.
 */
public class PendIndex extends Bean {

  /**
   * Creates the.
   *
   * @param s the s
   * @param id the id
   */
  public static void create(Class<? extends Searchable> s, String id) {
    Connection c = null;
    PreparedStatement stat = null;
    ResultSet r = null;

    try {
      c = Bean.getConnection();
      stat = c.prepareStatement("select 1 from tblpendindex where clazz=? and id=?");
      stat.setString(1, s.getSimpleName());
      stat.setString(2, id);
      r = stat.executeQuery();
      if (r.next()) {
        return;
      }
      r.close();
      r = null;
      stat.close();
      stat = c.prepareStatement("insert into tblpendindex(clazz, id, created) values(?, ?, ?)");
      stat.setString(1, s.getSimpleName());
      stat.setString(2, id);
      stat.setLong(3, System.currentTimeMillis());
      stat.executeUpdate();
    } catch (Exception e) {
      log.error(e.getMessage(), e);
    } finally {
      Bean.close(r, stat, c);
    }
  }

  /**
   * Eldest.
   *
   * @param s the s
   * @param num the num
   * @return the list
   */
  public static List<String> eldest(Class<? extends Searchable> s, int num) {
    Connection c = null;
    PreparedStatement stat = null;
    ResultSet r = null;

    try {
      c = Bean.getConnection();
      String name = s.getSimpleName();
      stat = c.prepareStatement("select id from tblpendindex where clazz=? and (not id is null) order by created limit ?");
      stat.setString(1, name);
      stat.setInt(2, num);
      List<String> list = new ArrayList<String>(num);
      r = stat.executeQuery();
      while (r.next()) {
        list.add(r.getString("id"));
      }
      r.close();
      r = null;
      stat.close();
      stat = c.prepareStatement("delete from tblpendindex where clazz=? and id=?");
      for (String id : list) {
        stat.setString(1, name);
        stat.setString(2, id);
        stat.addBatch();
      }
      stat.executeBatch();

      return list;

    } catch (Exception e) {
      log.error(e.getMessage(), e);
    } finally {
      Bean.close(r, stat, c);
    }

    return null;
  }

}
