/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.pinyin;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class PinyinTokenizer.
 *
 * @author gxm
 */
public class PinyinTokenizer {

    /** The trie. */
    StandardTree trie = new StandardTree();

    /**
     * Instantiates a new pinyin tokenizer.
     *
     * @param filename the filename
     */
    public PinyinTokenizer(String filename) {
        initPY(filename);
    }

    /**
     * Tokenize.
     *
     * @param pinyin the pinyin
     * @return the string[]
     */
    public String[] tokenize(String pinyin) {
        String[] array = pinyin.split("[\\s\\pP~]+");
        List<String> forwordTokenizeRes = new ArrayList<String>();
        List<String> backwordTokenizeRes = new ArrayList<String>();

        for (String token : array) {
            List<String> tmpForwordTokenizeRes = getPathMax(token, trie);
            List<String> tmpBackwordTokenizeRes = getPathopposite(token, trie);
            forwordTokenizeRes.addAll(tmpForwordTokenizeRes);
            backwordTokenizeRes.addAll(tmpBackwordTokenizeRes);
        }

        if (forwordTokenizeRes.size() > backwordTokenizeRes.size()) {
            return backwordTokenizeRes.toArray(new String[0]);
        } else {
            return forwordTokenizeRes.toArray(new String[0]);
        }
    }

    /**
     * Inits the py.
     *
     * @param filename the filename
     */
    private void initPY(String filename) {
        try {
            BufferedReader reader = getReader(filename);
            String temp = null;
            while ((temp = reader.readLine()) != null) {
                temp = temp.trim();
                trie.insert(temp);
            }
            reader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Gets the path max.
     *
     * @param a the a
     * @param trie the trie
     * @return the path max
     */
    private List<String> getPathMax(String a, StandardTree trie) {
        List<String> result = new ArrayList<String>();
        for (int i = 0; i < a.length(); i++) {
            int step = 8;
            if (a.length() - i < 8) {
                step = a.length() - i;
            }
            for (; step > 0; step--) {
                String pyword = a.substring(i, i + step);
                if (trie.fullMatch(pyword)) {
                    result.add(pyword);
                    i = i + step - 1;
                    break;
                }
            }
        }

        return result;
    }

    /**
     * Gets the pathopposite.
     *
     * @param a the a
     * @param trie the trie
     * @return the pathopposite
     */
    private List<String> getPathopposite(String a, StandardTree trie) {
        List<String> resultOpp = new ArrayList<String>();
        List<String> result = new ArrayList<String>();
        for (int i = a.length(); i > 0; i--) {
            int step = 8;
            if (i < 8) {
                step = i;
            }

            for (; step > 0; step--) {
                String pyword = a.substring(i - step, i);
                if (trie.fullMatch(pyword)) {
                    result.add(pyword);
                    i = i - step + 1;
                    break;
                }
            }
        }

        for (int i = result.size() - 1; i >= 0; i--) {
            resultOpp.add(result.get(i));
        }

        return resultOpp;
    }

    /**
     * Gets the reader.
     *
     * @param haspath the haspath
     * @return the reader
     */
    private BufferedReader getReader(String haspath) {
        BufferedReader br = null;
        InputStream is = null;
        File f = new File(haspath);

        try {
            if (f.isFile() && f.exists()) {
                br = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
            } else {
                is = this.getClass().getClassLoader().getResourceAsStream("pinyin.dic");
                br = new BufferedReader(new InputStreamReader(is));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return br;
    }
    /*
    private void getPath(String a, StandardTrie trie) {
    List<String> resultmax = new ArrayList<String>();
    List<String> result = new ArrayList<String>();
    for (int i = 0; i < a.length(); i++) {
    int step = 8;
    int tempS = step;
    int currentS = step;
    if (a.length() - i < 8) {
    step = a.length() - i;
    }
    for (; step > 0; step--) {
    String pyword = a.substring(i, i + step);
    if (trie.fullMatch(pyword)) {
    tempS = step;
    int k = i + step - 2;
    int step2 = 5;
    if (a.length() - k < 5) {
    step2 = a.length() - k;
    }
    int end = i + step + 2;
    if (i + step + 2 > a.length()) {
    end = a.length();
    }
    boolean flag = true;
    for (; k < end; k++) {

    for (; step2 > 0; step2--) {
    String py = a.substring(i, k);
    String pywordtemp = a.substring(k, k + step2);
    if ((trie.fullMatch(pywordtemp)) && (trie.fullMatch(pywordtemp))) {
    flag = false;

    result.add(py);
    result.add(pywordtemp);
    i = k + step2 - 1;
    break;
    }
    }
    }
    if (flag) {
    i = i + step - 1;
    result.add(pyword);
    break;
    }
    }
    }
    }

    System.out.println("the final results are: ");
    for (int i = 0; i < result.size(); i++) {
    System.out.println(result.get(i));
    }
    }
     * */
}
