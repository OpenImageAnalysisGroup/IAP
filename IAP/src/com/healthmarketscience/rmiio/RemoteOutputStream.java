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

package com.healthmarketscience.rmiio;

import java.io.Flushable;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Remote interface which allows exporting an OutputStream-like interface over
 * the network.  When combined with the {@link RemoteOutputStreamServer}
 * subclasses on the server side and the {@link RemoteOutputStreamClient} on
 * the client side, this class provides a true remote OutputStream (in other
 * words, should never be used alone, use the utility classes).
 * <p>
 * Note that all methods on this interface are idempotent (when used
 * correctly), and can therefore be retried as necessary in the face of
 * RemoteExceptions.
 * <p>
 * An actual instance of this class is not intended for use by more than one
 * client, and should be treated in a similar manner to an "un-synchronized"
 * local interface.
 *
 * @author James Ahlborn
 */
public interface RemoteOutputStream extends Remote, Flushable
{

  /**
   * Returns <code>true</code> if the stream is using GZIP compression over
   * the wire.
   *
   * @return <code>true</code> iff the stream data is compressed,
   *         <code>false</code> otherwise
   */
  public boolean usingGZIPCompression()
    throws IOException, RemoteException;
  
  /**
   * Closes the output stream and releases the resources for this server
   * object.  Note that the remote object <i>may no longer be accessible</i>
   * after this call (depending on the implementation), so clients should not
   * attempt to use this stream after making this call.
   *
   * @param writeSuccess <code>true</code> iff all data was sent successfully
   *                     from the client, <code>false</code> otherwise
   */
  public void close(boolean writeSuccess)
    throws IOException, RemoteException;

  /**
   * Flushes the output stream and forces as much as possible of any buffered
   * bytes to be written out.  Some of the layers of buffering may not be able
   * to be flushed, so this method should not be depended upon to do much.
   * The close() method will do any final flushing necessary (and can be
   * depended upon).
   */
  public void flush()
    throws IOException, RemoteException;

  /**
   * Writes the next chunk of data to this stream.
   *
   * The given packetId parameter (if used correctly) allows this operation to
   * be idempotent.  This parameter must be a monotonically increasing,
   * positive integer.  If the client fails to write a given packet, it may
   * reattempt to write the same packet by giving the same packetId as from
   * the failed call.  However, only the current packet may be reattempted
   * (the client cannot attempt to write any other previous packets).  When
   * writing a new packet, the caller does not need to give a sequential id,
   * just a greater one (hence the term monotonically increasing).
   *
   * @param packet iff the packetId was the same one from the last read call,
   *               this chunk of data is ignored.  Otherwise, writes this new
   *               chunk of data to the stream.
   * @param packetId client specified id for this packet
   */
  public void writePacket(byte[] packet, int packetId)
    throws IOException, RemoteException;
  
}
