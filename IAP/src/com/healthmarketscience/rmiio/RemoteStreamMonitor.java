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


/**
 * Interface for monitoring the progress of a remote stream, such as
 * {@link RemoteInputStream} or {@link RemoteOutputStream}.
 *
 * @author James Ahlborn
 */
public interface RemoteStreamMonitor<StreamServerType> {

  /**
   * Called when an IOException is thrown by one of the stream methods.
   *
   * @param stream the stream on which the exception was thrown
   * @param e the thrown exception
   */
  public void failure(StreamServerType stream, Exception e);

  /**
   * Called when some bytes are transferred over the wire by the given stream.
   *
   * Note, as some streams use compression over the wire, the total number of
   * bytes moved/skipped may not equal the total number of <i>local</i> bytes
   * moved/skipped.
   *
   * @param stream the stream for which the bytes are being transferred
   * @param numBytes number of bytes transferred
   * @param isReattempt indicates if this is the first attempt
   *                    (<code>false</code>) or a subsequent attempt
   *                    (<code>true</code>)
   */
  public void bytesMoved(StreamServerType stream, int numBytes, boolean isReattempt);

  /**
   * Called when some bytes are skipped for transfer over the wire by the
   * given stream.  Will not be called for output streams.
   *
   * Note, as some streams use compression over the wire, the total number of
   * bytes moved/skipped may not equal the total number of <i>local</i> bytes
   * moved/skipped.
   *
   * @param stream the stream for which the bytes are being skipped
   * @param numBytes number of actual bytes skipped
   * @param isReattempt indicates if this is the first attempt
   *                    (<code>false</code>) or a subsequent attempt
   *                    (<code>true</code>)
   */
  public void bytesSkipped(StreamServerType stream, long numBytes,
                           boolean isReattempt);

  /**
   * Called when some bytes are moved to/from the local stream.
   *
   * Note, as some streams use compression over the wire, the total number of
   * bytes moved/skipped may not equal the total number of <i>local</i> bytes
   * moved/skipped.
   *
   * @param stream the remote stream for which the bytes are being moved
   * @param numBytes number of bytes moved
   */
  public void localBytesMoved(StreamServerType stream, int numBytes);

  /**
   * Called when some bytes from the local stream are skipped.  Will not be
   * called for output streams.
   *
   * Note, as some streams use compression over the wire, the total number of
   * bytes moved/skipped may not equal the total number of <i>local</i> bytes
   * moved/skipped.
   *
   * @param stream the stream for which the bytes are being skipped
   * @param numBytes number of actual bytes skipped
   */
  public void localBytesSkipped(StreamServerType stream, long numBytes);

  /**
   * Called when the given stream is closed.  The clean parameter indicates
   * whether or not the transfer completed successfully.
   *
   * @param stream the stream for which the bytes are being skipped
   * @param clean <code>true</code> iff all data was sent successfully over
   *              the wire and the stream was closed, <code>false</code>
   *              otherwise.
   */
  public void closed(StreamServerType stream, boolean clean);
  
}
