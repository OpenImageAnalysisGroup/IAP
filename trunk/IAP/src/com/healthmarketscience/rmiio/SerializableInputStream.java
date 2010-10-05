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

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

/**
 * An additional layer around a RemoteInputStream which makes it Serializable
 * and an InputStream.  In general, this extra layer is not necessary and
 * <em>I do not recommend using this class</em>.  However, in the odd case
 * where the callee really wants to get something which is already an
 * InputStream, this class can be useful.  This is basically just a wrapper
 * around a call to {@link RemoteInputStreamClient#wrap}.
 *
 * @see <a href="{@docRoot}/overview-summary.html#Usage_Notes">Usage Notes</a>
 *
 * @author James Ahlborn
 */
public class SerializableInputStream extends InputStream
  implements Serializable, RemoteClient
{
  private static final long serialVersionUID = -8922181237767770749L;

  /** the handle to the actual remote interface */
  private final RemoteInputStream _remoteIn;
  /** optional client-side RemoteRetry policy */
  private transient RemoteRetry _retry;
  /** the actual client-side InputStream implementation, initialized on demand
      by a call to any one of the InputStream methods. */
  private transient InputStream _localIn;
  
  public SerializableInputStream(InputStream localIn)
    throws IOException
  {
    // note, we do not need to export here, as it will be handled
    // automagically when the _remoteIn field is serialized.  this makes it
    // very easy to consume this input stream locally or remotely.
    this(new GZIPRemoteInputStream(localIn));
  }

  public SerializableInputStream(RemoteInputStream remoteIn) {
    _remoteIn = remoteIn;
  }

  /**
   * @return the the actual client-side InputStream implementation, creating
   *         if necessary.  This call synchronizes on this object for the
   *         initialization call only.  All other synchronization of actual
   *         stream calls is handled by the implementation class created here.
   */
  private synchronized InputStream getLocalIn()
    throws IOException
  {
    if(_localIn == null) {
      _localIn = RemoteInputStreamClient.wrap(_remoteIn, _retry);
    }
    return _localIn;
  }

  public synchronized void setRemoteRetry(RemoteRetry retry) {
    _retry = retry;
  }

  @Override
  public int available()
    throws IOException
  {
    return getLocalIn().available();
  }

  @Override
  public int read()
    throws IOException
  {
    return getLocalIn().read();
  }

  @Override
  public int read(byte[] b)
    throws IOException
  {
    return read(b, 0, b.length);
  }

  @Override
  public int read(byte[] buf, int pos, int len)
    throws IOException
  {
    return getLocalIn().read(buf, pos, len);
  }

  @Override
  public long skip(long len)
    throws IOException
  {
    return getLocalIn().skip(len);
  }

  @Override
  public void close()
    throws IOException
  {
    getLocalIn().close();
  }
  
}
