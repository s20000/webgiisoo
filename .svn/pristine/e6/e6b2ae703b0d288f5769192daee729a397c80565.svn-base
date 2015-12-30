/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web.admin;

import java.io.*;
import java.util.zip.*;

import com.giisoo.core.bean.UID;
import com.giisoo.framework.web.*;

public class exportupgrade extends Model {

  static String ROOT = "/tmp/export/";
  static String exclude = "(.ini|conf/.*.properties)";
  static String[] source = new String[] { "/modules", "/conf", "/WEB-INF" };

  /* (non-Javadoc)
   * @see com.giisoo.framework.web.Model#onGet()
   */
  @Override
  @Require(login = true, access = "access.export")
  public void onGet() {
    /**
     * zip all in the war file and store in temp, let's user the download it
     */

    synchronized (ROOT) {
      try {
        String method = this.path;
        if ("delete".equals(method)) {
          String name = this.getString("name");
          File f = new File(Model.HOME + ROOT + name);
          if (f.exists() && f.getAbsolutePath().startsWith(Model.HOME)) {
            f.delete();
          }
        }

        // Create the ZIP file
        long seq = UID.next("export.upgrade.seq");

        long last = 0;
        boolean changed = false;

        String prev = ROOT + "webgiisoo_baseline.zip";
        File f = new File(Model.HOME + prev);
        if (f.exists()) {
          last = f.lastModified();
          prev = ROOT + "webgiisoo_upgrade_" + (seq - 1) + ".zip";
          f = new File(Model.HOME + prev);
          if (f.exists()) {
            /**
             * check any files changed compared with last war
             */
            for (String s : source) {
              if (hasFile(s, f.lastModified())) {
                changed = true;
                String target = "webgiisoo_upgrade_" + seq + ".zip";
                f = new File(Model.HOME + ROOT + target);
                break;
              }
            }
          } else {
            /**
             * not found, create base line
             */
            changed = true;
            String target = "webgiisoo_upgrade_" + seq + ".zip";
            f = new File(Model.HOME + ROOT + target);
          }

        } else {
          /**
           * create baseline
           */
          UID.set("export.upgrade.seq", 0);
          changed = true;
        }

        if (changed && !f.exists()) {
          f.getParentFile().mkdirs();
          ZipOutputStream out = new ZipOutputStream(new FileOutputStream(f));
          // Compress the files
          int total = 0;
          for (String s : source) {
            total += addFile(out, s, last);
          }

          // Complete the ZIP file
          try {
            out.close();
          } catch (Exception e) {

          }
          if (total == 0) {
            f.delete();
            UID.set("export.upgrade.seq", seq);
          }
        } else {
          UID.set("export.upgrade.seq", seq);
        }

        this.set("list", f.getParentFile().listFiles());

        show("/admin/export.upgrade.index.html");

      } catch (IOException e) {
        log.error(e.getMessage(), e);
        error(e);
      }
    }
  }

  private boolean hasFile(String filename, long lastmodified) {
    File f = new File(Model.HOME + filename);
    if (f.isFile() && f.lastModified() > lastmodified && !f.getName().matches(exclude)) {
      return true;
    } else if (f.isDirectory()) {
      String[] list = f.list();
      if (list != null) {
        for (String s : list) {
          if (hasFile(filename + "/" + s, lastmodified)) {
            return true;
          }
        }
      }
    }

    return false;
  }

  private int addFile(ZipOutputStream out, String filename, long lastmodified) {

    File f = new File(Model.HOME + filename);
    if (f.isFile() && f.lastModified() > lastmodified && !f.getName().matches(exclude)) {
      FileInputStream in = null;
      try {
        in = new FileInputStream(f);

        // Add ZIP entry to output stream.
        out.putNextEntry(new ZipEntry(filename));

        // Transfer bytes from the file to the ZIP file
        int len;
        byte[] buf = new byte[1024];
        while ((len = in.read(buf)) > 0) {
          out.write(buf, 0, len);
        }

        // Complete the entry
        out.closeEntry();

        return 1;
      } catch (Exception e) {
        log.error(e.getMessage(), e);
      } finally {
        if (in != null) {
          try {
            in.close();
          } catch (IOException e) {
            log.error(e);
          }
        }
      }
    } else if (f.isDirectory()) {
      String[] list = f.list();
      int i = 0;
      if (list != null) {
        for (String s : list) {
          i += addFile(out, filename + "/" + s, lastmodified);
        }
      }
      return i;
    }

    return 0;
  }

}
