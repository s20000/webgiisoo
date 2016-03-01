package com.giisoo.core.bean;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * the {@code Pool} is pool class, use to database connection, network or
 * something else
 * 
 * @author joe
 *
 * @param <E>
 */
public class Pool<E> {
    static Log log = LogFactory.getLog(Pool.class);

    private List<E> list = new ArrayList<E>();
    private int initial = 10;
    private int max = 10;
    private int created = 0;
    @SuppressWarnings("rawtypes")
    private ICreator creator;

    /**
     * create a pool by the params and creator
     * 
     * @param initial
     * @param max
     * @param creator
     * @return the Pool
     */
    public static <T> Pool<T> create(int initial, int max, ICreator<?> creator) {
        Pool<T> p = new Pool<T>();
        p.initial = initial;
        p.max = max;
        p.creator = creator;
        p.init();
        return p;
    }

    @SuppressWarnings("unchecked")
    private synchronized void init() {
        for (int i = 0; i < initial; i++) {
            E t = (E) creator.create();
            if (t != null) {
                list.add(t);
            }
        }
        created = list.size();
    }

    /**
     * release the object to pool
     * 
     * @param t
     */
    @SuppressWarnings("unchecked")
    public synchronized void release(E t) {
        if (t == null) {
            created--;
        } else {
            creator.cleanup(t);
            list.add(t);
        }
    }

    /**
     * get a object from the pool
     * 
     * @param timeout
     * @return the object, return null if failed, or timeout
     */
    @SuppressWarnings("unchecked")
    public synchronized E get(long timeout) {
        try {
            if (list.size() == 0) {
                if (created < max) {
                    E t = (E) creator.create();
                    if (t != null) {
                        created++;
                        return t;
                    }
                }
                this.wait(timeout);
            }

            if (list.size() > 0) {
                return list.remove(0);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return null;
    }

    /**
     * the {@code ICreator} Interface used to create the Object<E> in pool
     * 
     * @author joe
     *
     * @param <T>
     */
    public interface ICreator<T> {
        /**
         * the method of create the Object<T>
         * 
         * @return T
         */
        public T create();

        /**
         * the method of cleanup the object
         * 
         * @param t
         */
        public void cleanup(T t);

    }
}
