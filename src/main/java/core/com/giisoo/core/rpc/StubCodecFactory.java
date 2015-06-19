package com.giisoo.core.rpc;

import java.io.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.*;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.*;

// TODO: Auto-generated Javadoc
/**
 * A factory for creating StubCodec objects.
 */
public class StubCodecFactory implements ProtocolCodecFactory {
	
	/** The encoder. */
	private ProtocolEncoder encoder;
	
	/** The decoder. */
	private ProtocolDecoder decoder;
	
	/** The max len. */
	public static int maxLen=-1;
	// private final ConcurrentLinkedQueue<IoBuffer> buffers = new ConcurrentLinkedQueue<IoBuffer>();
	// public static final int initLen = 100 * 1000;

	/**
	 * Instantiates a new stub codec factory.
	 */
	public StubCodecFactory() {
		encoder = new StubResEncoder();
		decoder = new StubResDecoder();
		// for (int i = 0; i < 100; i++) {
		// buffers.add(createNewBuffer());
		// }
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.filter.codec.ProtocolCodecFactory#getEncoder(org.apache.mina.core.session.IoSession)
	 */
	public ProtocolEncoder getEncoder(IoSession ioSession) throws Exception {
		return encoder;
	}

	/* (non-Javadoc)
	 * @see org.apache.mina.filter.codec.ProtocolCodecFactory#getDecoder(org.apache.mina.core.session.IoSession)
	 */
	public ProtocolDecoder getDecoder(IoSession ioSession) throws Exception {
		return decoder;
	}

	/**
	 * The Class StubResDecoder.
	 */
	private class StubResDecoder extends ProtocolDecoderAdapter {

		/* (non-Javadoc)
		 * @see org.apache.mina.filter.codec.ProtocolDecoder#decode(org.apache.mina.core.session.IoSession, org.apache.mina.core.buffer.IoBuffer, org.apache.mina.filter.codec.ProtocolDecoderOutput)
		 */
		@Override
		public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out) throws Exception {
			try {
				int len = in.getInt();
				int flag = in.getInt();
				byte[] b = new byte[len];
				in.get(b);

				Object o = null;
				if ((flag & 0x01) == 1) {
					/**
					 * the data is compressed
					 */
					ByteArrayInputStream bis = new ByteArrayInputStream(b);
					GZIPInputStream zis = new GZIPInputStream(bis);
					ObjectInputStream ois = new ObjectInputStream(zis);
					o = ois.readObject();// readUnshared();

					ois.close();
					bis.close();
				} else {
					/**
					 * the data is not compressed
					 */
					ByteArrayInputStream bis = new ByteArrayInputStream(b);
					ObjectInputStream ois = new ObjectInputStream(bis);
					o = ois.readObject();// readUnshared();

					ois.close();
					bis.close();
				}
				out.write(o);

			} catch (Exception e) {
				throw e;
			}

		}

	}

	/**
	 * The Class StubResEncoder.
	 */
	private class StubResEncoder extends ProtocolEncoderAdapter {

		/* (non-Javadoc)
		 * @see org.apache.mina.filter.codec.ProtocolEncoder#encode(org.apache.mina.core.session.IoSession, java.lang.Object, org.apache.mina.filter.codec.ProtocolEncoderOutput)
		 */
		public void encode(IoSession session, Object o, ProtocolEncoderOutput out) throws Exception {
			IoBuffer iobuffer = IoBufferPool.getInstance().borrowBuffer();
			session.setAttribute(IoBufferPool.key, iobuffer);
			if (o == null) {
				o = Stub.NULL;
			}

			GifoxByteArrayOuputStream bos = (GifoxByteArrayOuputStream)session.getAttribute(ServerStub2.key_bout);
			GZIPOutputStream zos = new GZIPOutputStream(bos);
			ObjectOutputStream oos = new ObjectOutputStream(zos);
			try {
				int flag = 0;
				if (Stub.zip) {
					flag |= 0x01;
					oos.writeObject(o);// .writeUnshared(o);// writeObject(o);
					oos.flush();
					zos.finish();
					int len = bos.size();
					byte[] data = bos.getUnderlyingBuffer();
					if(len>maxLen) maxLen = len;
					iobuffer.putInt(len);
					iobuffer.putInt(flag);
					iobuffer.put(data,0,len);
				} else {
					flag &= 0xFE;
					oos.writeObject(o);
					oos.flush();
					oos.close();
					byte[] b = bos.getUnderlyingBuffer();
					int len = bos.size();
					iobuffer.putInt(len);
					iobuffer.putInt(flag);
					iobuffer.put(b,0,len);
				}
				iobuffer.flip();
				out.write(iobuffer);
				out.flush();
			} catch (IOException e) {
				throw e;
			}finally{
				bos.reset();
				zos.close();
				oos.close(); 
			}
		}

	}

}