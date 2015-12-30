/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.pinyin;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO: Auto-generated Javadoc
/**
 * 预先解析idx文件，后来设计对idx文件的解析由数据库完成，此段代码没有意义。.
 *
 * @author ray
 */
public class Idx {

    /**
     * Instantiates a new idx.
     *
     * @param title the title
     * @param content the content
     */
    public Idx(String title, String content) {
        text = "#DRETITLE " + title + "\n#DRECONTENT\n" + content + "\n#DREENDDOC";
        parseSingleDoc();
    }

    /**
     * Instantiates a new idx.
     *
     * @param text the text
     */
    public Idx(String text) {
        this.text = text;
        parseSingleDoc();
    }

    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle() {
        if (null != IDX) {
            return getTagValue("DRETITLE");
        }
        return null;
    }

    /**
     * Gets the reference.
     *
     * @return the reference
     */
    public String getReference() {
        if (null != IDX) {
            return getTagValue("DREREFERENCE");
        }
        return null;
    }

    /**
     * Gets the tag value.
     *
     * @param Tag the tag
     * @return the tag value
     */
    public String getTagValue(String Tag) {
        if (null != IDX) {
            List<String> set = IDX.get(Tag.toUpperCase());
            if (null != set && set.iterator().hasNext()) {
                return set.iterator().next();
            }
        }
        return null;
    }

    /**
     * Gets the tag values.
     *
     * @param Tag the tag
     * @return the tag values
     */
    public List<String> getTagValues(String Tag) {
        if (null != IDX) {
            return IDX.get(Tag.toUpperCase());
        }
        return null;
    }

    /**
     * add or insert a drefiled into idx,<p>
     * if you add a reserved field which name start with DRE, it will replace old value.<p>
     * idx.addField("sitedomain","www.163.com");<p>
     * ...<p>
     * #DREFIELD SITEDOMAIN="www.163.com"<p>
     * ...<p>
     *
     * @param fieldname the fieldname
     * @param value the value
     */
    public void addField(String fieldname, String value) {
        fieldname = fieldname.toUpperCase();
//        if (!isReservedField(fieldname)) {
            put(fieldname, value);
//        } else {
//            System.out.println(fieldname + " is a reserved field, you can not modify it.");
//        }
    }

    /**
     * Removes the field.
     *
     * @param fieldname the fieldname
     */
    public void removeField(String fieldname) {
        fieldname = fieldname.toUpperCase();
        if (!isReservedField(fieldname)) {
            if (IDX.containsKey(fieldname)) {
                List<String> list = IDX.get(fieldname);
                if (null != list) {
                    list.clear();
                }

                IDX.remove(fieldname);
            }
        } else {
            System.out.println(fieldname + " is a reserved field, you can not modify it.");
        }
    }

    /**
     * Removes the field.
     *
     * @param fieldname the fieldname
     * @param value the value
     */
    public void removeField(String fieldname, String value) {
        fieldname = fieldname.toUpperCase();
        if (!isReservedField(fieldname)) {
            if (IDX.containsKey(fieldname)) {
                List<String> list = IDX.get(fieldname);
                if (null != list && list.contains(value)) {
                    list.remove(value);
                }
            }
        } else {
            System.out.println(fieldname + " is a reserved field, you can not modify it.");
        }
    }

    /**
     * 解析IDX文件，把一篇文档从中找出来.
     *
     * @param text the text
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static String parsePlainText(BufferedReader text) throws IOException {
        StringBuffer sb = new StringBuffer();

        String line = null;

        line = text.readLine();
        if (null == line) {
            return null;
        }

        sb.append(line).append('\n');

        int startPos = line.indexOf("#DREREFERENCE");
        while (startPos < 0) {
            line = text.readLine();
            if (null == line) {
                return null;
            }
            sb.append(line).append('\n');
            startPos = sb.indexOf("#DREREFERENCE");
        }

        line = text.readLine();
        if (null == line) {
            return null;
        }
        sb.append(line).append('\n');
        int endPos = line.indexOf("#DREENDDOC");
        while (endPos < 0) {
            line = text.readLine();
            if (null == line) {
                return null;
            }
            sb.append(line).append('\n');
            endPos = sb.indexOf("#DREENDDOC");
        }

        if (startPos >= 0 && endPos > 0) {
            return sb.substring(startPos, endPos + 10);
        } else {
            return null;
        }
    }

    /**
     * Parses the single doc.
     */
    public void parseSingleDoc() {
        if (null == text) {
            return;
        }
        String doc = text;

        IDX = new LinkedHashMap<String, List<String>>();

        int startPos = doc.indexOf("#DRE");
        int endPos = doc.indexOf("\n#", startPos);

        while (startPos >= 0 && endPos > 0) {
            String str = doc.substring(startPos, endPos).trim();

            Matcher m = drefieldPattern.matcher(str);
            if (m.matches()) {
                String tag = m.group(1).trim().toUpperCase();
                String value = m.group(2).trim();
                put(tag, value);
            } else {
                int multiline = str.indexOf('\n');
                String line = multiline >= 0 ? str.substring(0, multiline) : str;
                m = drefieldPattern2.matcher(line);
                if (m.matches()) {
                    String tag = m.group(1).trim().toUpperCase();
                    String value = str.substring(tag.length() + 1).trim();
                    put(tag, value);
                }
            }

            startPos = doc.indexOf("#DRE", endPos);
            if (startPos >= 0) {
                endPos = doc.indexOf("\n#", startPos);
            }
        }

        return;
    }

    /**
     * Put.
     *
     * @param tag the tag
     * @param value the value
     */
    private void put(String tag, String value) {
        List<String> list = null;
        if (IDX.containsKey(tag)) {
            list = IDX.get(tag);
        }

        if (null == list) {
            list = new ArrayList<String>();
            list.add(value);
            IDX.put(tag, list);
        } else {
            if (tag.startsWith("DRE")) {
                list.clear();
                list.add(value);
            } else {
                if (!list.contains(value)) {
                    list.add(value);
                }
            }
        }
    }

    /**
     * Gets the dre fields string.
     *
     * @return the dre fields string
     */
    private String getDreFieldsString() {
        StringBuffer doc = new StringBuffer();

        Set<String> keySet = IDX.keySet();
        List<String> reservedKeys = new ArrayList<String>();
        List<String> fieldKeys = new ArrayList<String>();
        for (String key : keySet) {
            if (isReservedField(key)) {
                reservedKeys.add(key);
            } else {
                fieldKeys.add(key);
            }
        }
        for (String key : reservedKeys) {
            List<String> v = IDX.get(key);
            String b = " ";
            if ("DRECONTENT".equals(key)) {
                b = "\n";
                Collections.sort(fieldKeys);
                for (String k : fieldKeys) {
                    List<String> values = IDX.get(k);
                    if (null != values) {
                        for (String value : values) {
                            doc.append("#DREFIELD ").append(k).
                                    append("=\"").append(value.replaceAll("\"", "")).append("\"\n");
                        }
                    }
                }
            }
            doc.append("#").append(key).
                    append(b).append(v.get(0)).append("\n");
        }

        return doc.toString();
    }

    /**
     * Checks if is reserved field.
     *
     * @param tag the tag
     * @return true, if is reserved field
     */
    private boolean isReservedField(String tag) {
        if (tag.startsWith("DRE")) {
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return getDreFieldsString() + "\n#DREENDDOC";
    }
    
    /** The Constant drefieldPattern. */
    private final static Pattern drefieldPattern = Pattern.compile("#DREFIELD\\s*(.*?)=\\\"(.*?)\\\".*");
    
    /** The Constant drefieldPattern2. */
    private final static Pattern drefieldPattern2 = Pattern.compile("#(DRE[a-zA-Z0-9]+).*");
    
    /** The idx. */
    private Map<String, List<String>> IDX = null;
    
    /** The text. */
    private String text = null;

    static {
        //BaseData.loadLog4jConfigFile();
    }
}
