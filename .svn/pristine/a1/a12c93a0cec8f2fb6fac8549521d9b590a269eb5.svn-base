package com.giisoo.core.rpc;

import java.io.*;

import com.giisoo.utils.base.IoStream;

// TODO: Auto-generated Javadoc
/**
 * The Class Command.
 */
public class Command implements Externalizable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The cmd. */
	String cmd;
	
	/** The params. */
	Object[] params;

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder(cmd).append("(");

		if (params != null) {
			for (int i = 0; i < params.length; i++) {
				Object o = params[i];
				if (i > 0) {
					sb.append(",");
				}
				if (o instanceof String) {
					sb.append("\"").append(o).append("\"");
				} else {
					sb.append(o);
				}
			}
		}

		return sb.append(")").toString();
	}

	/* (non-Javadoc)
	 * @see java.io.Externalizable#readExternal(java.io.ObjectInput)
	 */
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		cmd = IoStream.readString(in);
		params = (Object[]) in.readObject();
	}

	/* (non-Javadoc)
	 * @see java.io.Externalizable#writeExternal(java.io.ObjectOutput)
	 */
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		IoStream.writeString(out, cmd);
		out.writeObject(params);
	}
}
