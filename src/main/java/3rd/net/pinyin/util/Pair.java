package net.pinyin.util;

import java.util.Map;

// TODO: Auto-generated Javadoc
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * The Class Pair.
 *
 * @author ray
 * @param <K> the key type
 * @param <V> the value type
 */
public class Pair<K,V> implements Map.Entry<K,V> {
    
    /** The k. */
    final K k;
    
    /** The v. */
    V v = null;

    /**
     * Instantiates a new pair.
     *
     * @param k the k
     * @param v the v
     */
    public Pair(K k, V v){
        this.k = k;
        this.v = v;
    }

    /* (non-Javadoc)
     * @see java.util.Map.Entry#getKey()
     */
    @Override
    public K getKey() {
        return k;
    }

    /* (non-Javadoc)
     * @see java.util.Map.Entry#getValue()
     */
    @Override
    public V getValue() {
        return v;
    }

    /* (non-Javadoc)
     * @see java.util.Map.Entry#setValue(java.lang.Object)
     */
    @Override
    public V setValue(V value) {
        v = value;
        return v;
    }


}
