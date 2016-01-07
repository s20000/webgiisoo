package com.giisoo.core.cache;

// TODO: Auto-generated Javadoc
/**
 * The Class DefaultCachable.
 */
public class DefaultCachable implements Cachable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The _age. */
    private long _age = System.currentTimeMillis();

    private long expired = -1;

    /*
     * (non-Javadoc)
     * 
     * @see com.giisoo.cache.Cachable#age()
     */
    @Override
    public long age() {
        return System.currentTimeMillis() - _age;
    }

    @Override
    public void setExpired(int t) {
        expired = t * 1000 + System.currentTimeMillis();
    }

    public boolean expired() {
        return expired > 0 && expired < System.currentTimeMillis();
    }
}