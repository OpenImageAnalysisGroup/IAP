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
import java.util.zip.GZIPInputStream;
import java.io.OutputStream;

import com.healthmarketscience.rmiio.util.PipeBuffer;

/**
 * Concrete implementation of a RemoteOutputStreamServer which expects to
 * receive compressed data, which it will write directly to the underlying
 * OutputStream.
 *
 * @see <a href="{@docRoot}/overview-summary.html#Usage_Notes">Usage Notes</a>
 * @see #writeReplace
 *
 * @author James Ahlborn
 */
public class GZIPRemoteOutputStream extends RemoteOutputStreamServer 
{
  private static final long serialVersionUID = 20080212L;

  /** input stream from which GZIPInputStream reads compressed data */
  private transient final PipeBuffer.InputStreamAdapter _packetIStream;
  /** input stream which is used to uncompress data before writing to the
      underlying OutputStream */
  private transient GZIPInputStream _gzipIStream;
  /** temporary buffer used to copy data from the GZIPInputStream to the
      underlying OutputStream */
  private transient final byte[] _transferBuf;
  /** <code>true</code> iff the underlying OutputStream has reached EOF,
      <code>false</code> otherwise. */
  private transient boolean _outEOF = false;

  
  public GZIPRemoteOutputStream(OutputStream out) {
    this(out, DUMMY_MONITOR);
  }
  
  public GZIPRemoteOutputStream(
    OutputStream out,
    RemoteStreamMonitor<RemoteOutputStreamServer> monitor)
  {
    super(out, monitor);
    _packetIStream = new PipeBuffer.InputStreamAdapter(
        RemoteOutputStreamClient.DEFAULT_CHUNK_SIZE);
    _packetIStream.createPipeBuffer();
    // the _transferBuf will hold uncompressed data which will get compressed
    // and pushed into the _packetIStream.  we want the input buffer size
    // (uncompressed) to roughly correspond to the output buffer size
    // (compressed).  since this is all highly dependent on the data, we'll
    // just pull a number out of the air (doubling the output buffer size).
    _transferBuf = new byte[(int)(RemoteOutputStreamClient.DEFAULT_CHUNK_SIZE
                                  * 2)];
  }

  public boolean usingGZIPCompression()
  {
    return true;
  }

  /**
   * Copies data from the GZIPInputStream into the underlying OutputStream,
   * reading at most maxReadLen bytes at a time from the GZIPInputStream, and
   * continuing until the PipeBuffer.InputStreamAdapter has been consumed or
   * all remaining data has been consumed (iff finish is <code>true</code>).
   */
  private void flushPacket(int maxReadLen, boolean finish)
    throws IOException
  {
    // will be called synchronized
    
    // now, force data to be read from packet buffer
    int totRead = 0;
    int readLen = maxReadLen;
    while(!_outEOF &&
          ((_packetIStream.available() > 0) || finish)) {

      // read as much as specified
      if((totRead + readLen) > _transferBuf.length) {
        readLen = _transferBuf.length - totRead;
      }
      int numRead = _gzipIStream.read(_transferBuf, totRead, readLen);

      if(numRead > 0) {
        
        totRead += numRead;

        if(totRead == _transferBuf.length) {
          // flush the transfer buf
          totRead = 0;
          readLen = maxReadLen;
          _out.write(_transferBuf, 0, _transferBuf.length);
          _monitor.localBytesMoved(this, _transferBuf.length);
        }
        
      } else {
        
        // all done
        _outEOF = true;
      }        
        
    }

    // write out any remaining bytes in the transfer buffer
    if(totRead > 0) {
      _out.write(_transferBuf, 0, totRead);
      _monitor.localBytesMoved(this, totRead);
    }

  }

  @Override
  protected void closeImpl(boolean writeSuccess)
    throws IOException
  {
    // if not all data was written successfully, flushing the underlying
    // stream could cause deadlock (and isn't worth it anyway), so skip
    // flushing in that case
    if(writeSuccess) {
      synchronized(getLock()) {
        try {
          // don't bother flushing unless we actually wrote something!
          if(_gzipIStream != null) {
            // first, flush our gzip stream (read as much as possible at a
            // time)
            flushPacket(_transferBuf.length, true);
          }
        } catch(IOException ignored) {
          if(LOG.isDebugEnabled()) {
            LOG.debug("Ignoring exception while flushing stream", ignored);
          }
        }
      }
    }
    
    // now, let super class close
    super.closeImpl(writeSuccess);
  }

  @Override
  protected void flushImpl()
    throws IOException
  {
    // note the best we can do here is flush the underlying stream, we cannot
    // flush our gzip stream (or we may deadlock).

    synchronized(getLock()) {
      _out.flush();
    }
  }

  @Override
  protected void writePacket(byte[] packet)
    throws IOException
  {
    // will be called synchronized

    // set new data
    _packetIStream.getBuffer().writePacket(packet, 0, packet.length);

    if(_gzipIStream == null) {
      // cannot instantiate GZIPInputStream until we actually have data,
      // which is kind of an ugly design (violates my rule of do as little
      // as possible in the constructor)...
      _gzipIStream =
        new GZIPInputStream(_packetIStream,
                            RemoteOutputStreamClient.DEFAULT_CHUNK_SIZE);
    }
      
    // we have to be careful here or we will deadlock.  we want to read
    // byte-by-byte from the gzip stream until the data is consumed from the
    // _packetIStream, and then stop (even though there will probably still be
    // data buffered in the gzip stream).
    flushPacket(1, false);
  }
  
}
