/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.app.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.giisoo.core.bean.Bean;
import com.giisoo.framework.common.Temp;
import com.giisoo.framework.web.Model;

/**
 * Web接口： /temp/[filename], 下载临时目录中的文件
 * 
 * @author joe
 * 
 */
public class temp extends Model {

	/* (non-Javadoc)
	 * @see com.giisoo.framework.web.Model#onGet()
	 */
	public void onGet() {

		log.debug("temp: " + this.path);
		if (this.path == null) {
			this.notfound();
			return;
		}

		String[] ss = this.path.split("/");
		if (ss.length != 2) {
			this.notfound();
			return;
		}

		String name = ss[1];
		File f = Temp.get(ss[0], name);
		if (!f.exists()) {
			this.notfound();
			return;
		}

		try {
			String range = this.getString("RANGE");
			long total = f.length();
			long start = 0;
			long end = total;
			if (range != null) {
				ss = range.split("(=|-)");
				if (ss.length > 1) {
					start = Bean.toLong(ss[1]);
				}

				if (ss.length > 2) {
					end = Math.min(total, Bean.toLong(ss[2]));
				}
			}

			if (end <= start) {
				end = start + 16 * 1024;
			}

			if (method.isMdc()) {
				this.set("Content-Range", "bytes " + start + "-" + end + "/"
						+ total);
			} else {
				this.setHeader("Content-Range", "bytes " + start + "-" + end
						+ "/" + total);
			}

			log.info(start + "-" + end + "/" + total);

			this.setContentType("application/octet");
			
			this.addHeader("Content-Disposition", "attachment; filename=\""
					+ name + "\"");

			InputStream in = new FileInputStream(f);
			OutputStream out = this.getOutputStream();
			Model.copy(in, out, start, end, true);

			return;

		} catch (Exception e) {
			log.error(f.getAbsolutePath(), e);
		}

		this.notfound();
	}

}
