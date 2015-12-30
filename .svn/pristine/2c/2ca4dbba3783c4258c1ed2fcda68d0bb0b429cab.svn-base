/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.console;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.giisoo.utils.image.GImage;

public class Ttest {

	// public static String replace(String src, String regex, String
	// replacement) {
	//
	// Pattern pattern = new Pattern(regex);
	// Replacer replacer = pattern.replacer(replacement);
	// String result = replacer.replace(src);
	// return result;
	//
	// }

	/**
	 * The main method.
	 * 
	 * @param args
	 *            the arguments
	 */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		String r = "/asdasdasd/$1/$2";
		String m = "/service/(.*)/tt1/(.*)";
		String s = "/service/aaaa/tt1/ssss.htm";

		System.out.println(s.matches(m));
		// System.out.println(replace(s, m, r));

		// test();
		//
		// JSONObject jo = new JSONObject();
		// jo.put("hh", "汉字");
		// s = jo.toString();
		// System.out.println(s);
		// jo = JSONObject.fromObject(s);
		// System.out.println(jo.get("hh"));

		// MQ.init(Config.getConfig());
		// MQ.create("test", new ICallback() {
		//
		// public void run(Object... o) {
		// // TODO Auto-generated method stub
		//
		// }
		//
		// }, 0);

		// s = "get/access";
		// String path = "get/access";
		// Pattern p = Pattern.compile(s);// "经度(.*)纬度(.*)");
		// Matcher m1 = p.matcher(path);// "经度1121212纬度4343543");
		//
		// if (m1.find()) {
		//
		// Object[] params = new Object[m1.groupCount()];
		// System.out.println(params.length);
		// for (int i = 0; i < params.length; i++) {
		// params[i] = m1.group(i + 1);
		// System.out.println(params[i]);
		// }
		// }
		//
		// jo = new JSONObject();
		// jo.put("a", 1);
		// jo.put("b", 1);
		// System.out.println(jo.toString());
		//
		// jo = new JSONObject();
		// jo.put("b", 1);
		// jo.put("a", 1);
		// System.out.println(jo.toString());

		s = "/task/get";
		System.out.println("match: " + s.matches("/task/get"));

		String s1 = "http://mmbiz.qpic.cn/mmbiz/M01j67pich4nE9EOkyJzGroSWhDJuITr2pj0snFibNXJibE8j5QFlbWNqAVJPkmmHuoTNZia6DnPWV2dhyo6iaP6u2A/0";
		String s2 = "http://mmbiz.qpic.cn/mmbiz/M01j67pich4nE9EOkyJzGroSWhDJuITr2IseE9t9IXZaegCzKUeopNM0iaWCFgXS2PHfwHLmIgryQqzxzXh6ia1ug/0";

		try {

			File jpegFile = new File(s1);
			Metadata metadata = JpegMetadataReader.readMetadata(new URL(s2)
					.openStream());
			Iterator<Directory> it = metadata.getDirectories().iterator();
			while (it.hasNext()) {
				Directory exif = it.next();
				Iterator<Tag> tags = exif.getTags().iterator();
				while (tags.hasNext()) {
					Tag tag = (Tag) tags.next();
					if ("Orientation".equals(tag.getTagName())) {
						System.out.println(tag.getTagName() + "="
								+ tag.getDescription());
						String desc = tag.getDescription();
						if(desc.indexOf("Rotate 90") > 0 || desc.indexOf("Rotate 270") > 0) {
							System.out.println("rotate");
						} else {
							System.out.println("normal");
						}
					}
				}
			}

			Point p = GImage.size(new URL(s1));
			System.out.println("s1={w:" + p.x + ", h:" + p.y + "}");

			p = GImage.size(new URL(s2));
			System.out.println("s2={w:" + p.x + ", h:" + p.y + "}");

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void test() {
		try {

			DefaultHttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(
					"http://ggfw.jshrss.gov.cn/qlyg/web/jnjd_zxcj.jsp?cxtype=1");

			HttpParams p = post.getParams();

			p.setParameter("sfz", "342426197301103028");
			p.setParameter("zkz", "1405171000000043148");
			p.setParameter("type", "1");
			p.setParameter("zs", "");
			p.setParameter("button.x", "0");
			p.setParameter("button.y", "0");
			post.setParams(p);

			HttpResponse resp = client.execute(post);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					resp.getEntity().getContent(), "gbk"));
			String line = reader.readLine();
			while (line != null) {
				System.out.println(line);
				line = reader.readLine();
			}
			reader.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
