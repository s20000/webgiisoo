package com.giisoo.core.stat;

import java.util.HashMap;
import java.util.Map;

public class Tree {

    Map<String, Attribute> attr = new HashMap<String, Attribute>();

    public Attribute get(String name) {

        Attribute a = attr.get(name);
        if (a == null) {
            a = new Attribute();
            attr.put(name, a);
        }
        return a;

    }

    public static class Attribute {
        Map<String, Object> attr = new HashMap<String, Object>();

        public Attribute set(String name, Object val) {
            attr.put(name, val);
            return this;
        }

        public Object get(String name) {
            return attr.get(name);
        }

        public Tree create() {
            Tree t = (Tree) attr.get("_tree");
            if (t == null) {
                attr.put("_tree", t);
            }
            return t;
        }

    }
}
