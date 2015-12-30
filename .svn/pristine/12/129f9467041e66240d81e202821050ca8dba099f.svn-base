package com.giisoo.core.rpc;

import java.rmi.RemoteException;

// TODO: Auto-generated Javadoc
/**
 * The Class IRemote.
 */
public abstract class IRemote {

	/**
	 * Remote host.
	 *
	 * @return the string
	 */
	public String remoteHost() {
		Stub s = Stub.get();
		if (s != null) {
			return s.getHost();
		}

		return null;
	}

	/**
	 * Gets the stub.
	 *
	 * @return the stub
	 */
	public Stub getStub() {
		return Stub.get();
	}

	/**
	 * Echo.
	 *
	 * @param i the i
	 * @return the integer
	 * @throws RemoteException the remote exception
	 */
	public Integer echo(Integer i) throws RemoteException {
		return i;
	}
}
