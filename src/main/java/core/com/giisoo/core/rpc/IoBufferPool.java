package com.giisoo.core.rpc;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.mina.core.buffer.IoBuffer;

// TODO: Auto-generated Javadoc
/**
 * The Class IoBufferPool.
 */
public class IoBufferPool {

	/** The Constant pool. */
	private static final ConcurrentLinkedQueue<IoBuffer> pool =  new ConcurrentLinkedQueue<IoBuffer>();
	
	/** The Max pool size. */
	public static int MaxPoolSize = 10000;
	
	/** The _this. */
	private static IoBufferPool _this; 
	
	/** The Constant key. */
	static final Object key = new Object();

	
	/**
	 * Creates the new buffer.
	 *
	 * @return the io buffer
	 */
	private static IoBuffer createNewBuffer() {
		IoBuffer b = IoBuffer.allocate(10240);
		b.setAutoExpand(true);
		b.setAutoShrink(false);
		return b;
	}

	/**
	 * Gets the single instance of IoBufferPool.
	 *
	 * @return single instance of IoBufferPool
	 */
	public static IoBufferPool getInstance(){
		return _this;
	}
	
	/**
	 * Inits the.
	 */
	public static void init(){
		for(int i=0;i<MaxPoolSize;i++){
			pool.add(createNewBuffer());
		}
		
		_this  = new IoBufferPool();
	}
	
	/**
	 * Borrow buffer.
	 *
	 * @return the io buffer
	 */
	public IoBuffer borrowBuffer(){
		IoBuffer buffer = pool.poll(); 
		if(buffer!=null)
			return buffer;
		return createNewBuffer();
	}

	/**
	 * Return buffer.
	 *
	 * @param buffer the buffer
	 */
	public void returnBuffer(IoBuffer buffer){
		if(pool.size()<MaxPoolSize)
			pool.add(buffer);
	}
	
}
