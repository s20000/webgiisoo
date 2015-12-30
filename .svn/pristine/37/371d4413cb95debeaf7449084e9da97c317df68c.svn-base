/*
 *   WebGiisoo, a java web foramewrok.
 *   Copyright (C) <2014>  <giisoo inc.>
 *
 */
package com.giisoo.framework.mdc.command;

import com.giisoo.framework.mdc.*;

/**
 * 
 * @author yjiang
 *
 */
public class Bye extends Command {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  public final byte COMMAND = BYE;

  /* (non-Javadoc)
   * @see com.giisoo.framework.mdc.command.Command#onRequest(com.giisoo.framework.mdc.Request, com.giisoo.framework.mdc.Response, com.giisoo.framework.mdc.TConn)
   */
  @Override
  public boolean onRequest(Request req, Response resp, TConn d) {

    log.debug("[" + d.getClientId() + "] say bye -" + d.getRemoteIp());

    d.close();

    return true;
  }

  /* (non-Javadoc)
   * @see com.giisoo.framework.mdc.command.Command#onResponse(com.giisoo.framework.mdc.Request, com.giisoo.framework.mdc.TConn)
   */
  @Override
  public boolean onResponse(Request in, TConn d) throws Exception {
    /**
     * cleanup the resource
     */
    App.bye(d);

    TConn.release();

    return true;
  }

}
