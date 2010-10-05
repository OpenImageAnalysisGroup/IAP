/*
Copyright (c) 2007 Health Market Science, Inc.

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
USA

You can contact Health Market Science at info@healthmarketscience.com
or at the following address:

Health Market Science
2700 Horizon Drive
Suite 200
King of Prussia, PA 19406
*/

package com.healthmarketscience.rmiio.exporter;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import com.healthmarketscience.rmiio.RemoteStreamServer;


/**
 * Default concrete implementation of RemoteStreamExporter which exports the
 * object for use with with standard RMI, via {@link UnicastRemoteObject}.
 * <p>
 * The default constructor will use a port configured by the system property
 * {@link #PORT_PROPERTY} if one is found, otherwise {@link #ANY_PORT} will be
 * used.
 *
 * @author James Ahlborn
 */
public class DefaultRemoteStreamExporter extends RemoteStreamExporter
{
  /** constant indicating that export can use any port */
  public static final int ANY_PORT = 0;

  /** system property used to determine the port to use for the default
      constructor.  if not given, {@link #ANY_PORT} is used. */
  public static final String PORT_PROPERTY =
    "com.healthmarketscience.rmiio.exporter.port";

  /** port number to use when exporting streams */
  private final int _port;
  
  public DefaultRemoteStreamExporter() {
    this(getDefaultPort());
  }

  public DefaultRemoteStreamExporter(int port) {
    _port = port;
  }

  public int getPort() {
    return _port;
  }

  @Override
  protected Object exportImpl(RemoteStreamServer<?,?> server)
    throws RemoteException
  {
    return UnicastRemoteObject.exportObject(server, getPort());
  }
  
  @Override
  protected void unexportImpl(RemoteStreamServer<?,?> server)
    throws Exception
  {
    UnicastRemoteObject.unexportObject(server, true);
  }

  /**
   * Determines the port to use for the default constructor.  If the system
   * property {@link #PORT_PROPERTY} has a valid integer it will be returned,
   * otherwise {@link #ANY_PORT} will be returned.
   * @return a port number
   */
  private static int getDefaultPort()
  {
    return Integer.getInteger(PORT_PROPERTY, ANY_PORT);
  }
  
}
