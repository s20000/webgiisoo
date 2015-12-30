package com.giisoo.core.bean;

// TODO: Auto-generated Javadoc
/**
 * The Class TimeStamp.
 */
public class TimeStamp {

    /** The start. */
    long start;

    /**
     * Creates the.
     *
     * @return the time stamp
     */
    public static TimeStamp create() {
        return new TimeStamp();
    }

    /**
     * Instantiates a new time stamp.
     */
    public TimeStamp() {
        start = System.currentTimeMillis();
    }

    /**
     * Sets the.
     *
     * @param s
     *            the s
     * @return the time stamp
     */
    public TimeStamp set(long s) {
        start = s;

        return this;
    }

    /**
     * Past.
     *
     * @return the long
     */
    public long past() {
        return System.currentTimeMillis() - start;
    }

    /**
     * Gets the.
     *
     * @return the long
     */
    public long get() {
        return start;
    }

    /**
     * Reset.
     */
    public long reset() {
        long r = past();
        start = System.currentTimeMillis();
        return r;
    }
}
