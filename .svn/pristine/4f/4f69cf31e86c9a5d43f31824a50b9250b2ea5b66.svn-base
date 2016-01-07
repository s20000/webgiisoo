/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.pinyin.util;

import java.util.*;

// TODO: Auto-generated Javadoc
/**
 * The Class Window.
 *
 * @author ray
 * @param <T> the generic type
 */
public class Window<T> {

    /** The size. */
    private int size = 0;
    
    /** The datas. */
    private List<T> datas = new ArrayList<T>();

    /**
     * Instantiates a new window.
     *
     * @param size the size
     */
    public Window(int size) {
        this.size = size;
    }

    /**
     * Clear.
     */
    public void clear() {
        datas.clear();
    }

    /**
     * Adds the.
     *
     * @param data the data
     */
    public void add(T data) {
        datas.add(data);
        if (datas.size() > size) {
            datas.remove(0);
        }
    }

    /**
     * To array.
     *
     * @param t the t
     * @return the t[]
     */
    public T[] toArray(T[] t) {
        return datas.toArray(t);
    }
}
