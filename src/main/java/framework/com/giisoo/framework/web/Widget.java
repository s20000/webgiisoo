package com.giisoo.framework.web;

import java.util.HashMap;
import java.util.Map;

import com.giisoo.core.bean.X;

public class Widget {

	String name;

	private static Map<String, Widget> widgets = new HashMap<String, Widget>();

	public static void register(Widget w) {
		widgets.put(w.name, w);
	}

	public static Widget get(String name) {
		return widgets.get(name);
	}

	public String toString(Model m) {
		return X.EMPTY;
	}

	public String getScreenshot() {
		return X.EMPTY;
	}

}
