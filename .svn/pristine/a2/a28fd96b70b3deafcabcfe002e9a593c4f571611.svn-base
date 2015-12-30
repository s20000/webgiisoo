/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.mdc.command;

import com.giisoo.framework.mdc.Request;
import com.giisoo.framework.mdc.Response;
import com.giisoo.framework.mdc.TConn;

public class NOP extends Command {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final byte COMMAND = NOP;

	/* (non-Javadoc)
	 * @see com.giisoo.framework.mdc.command.Command#onRequest(com.giisoo.framework.mdc.Request, com.giisoo.framework.mdc.Response, com.giisoo.framework.mdc.TConn)
	 */
	@Override
	public boolean onRequest(Request in, Response out, TConn d)
			throws Exception {
		out.clear();
		return true;
	}

}
