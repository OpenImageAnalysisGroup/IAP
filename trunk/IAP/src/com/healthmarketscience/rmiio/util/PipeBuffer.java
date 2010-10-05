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

package com.healthmarketscience.rmiio.util;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.util.LinkedList;

import com.healthmarketscience.rmiio.PacketInputStream;
import com.healthmarketscience.rmiio.PacketOutputStream;

/**
 * Utility class for implementing a pipe which will never block (at the
 * expense of possibly utilizing more memory).  This is useful for single
 * threaded pipe implementations (the java.io pipe implementations require
 * separate threads for reading and writing due to the possibility of the
 * writer or reader blocking).  Additionally, packet based read/write support
 * is implemented, which can be a significant speed advantage when working in
 * situations were buffers can be passed around instead of copied (e.g. RMI
 * usage).  In general, this implementation favors speed over memory usage, so
 * buffers will not be copied if at all possible, even if they are not making
 * the most efficient usage of memory.  This class has no synchronization as
 * it is designed for use by a single thread.
 *
 * @author James Ahlborn
 */
public class PipeBuffer {

  /** default target size of packets returned from packet related methods and
      internal buffer allocation */
  public static final int DEFAULT_PACKET_SIZE = 1024;

  /** the target size of packets returned from packet related methods and
      internal buffer allocation */
  private final int _packetSize;
    /** the total number of written bytes currently held by this object */
  private long _totalBytes;
  /** the current List of ByteWrappers */
  private final LinkedList<ByteWrapper> _buffers =
    new LinkedList<ByteWrapper>();
  /** convenience flag for communicating reader close */
  private boolean _gotReadEOF;
  /** convenience flag for communicating writer close */
  private boolean _gotWriteEOF;

  
  public PipeBuffer() {
    this(DEFAULT_PACKET_SIZE);
  }

  /**
   * @param packetSize "suggested" size for packets returned from {@link
   *                   #readPacket} as well as buffers allocated internally.
   *                   in the interest of speed, actual packet sizes and
   *                   internal buffer sizes may vary from this value.
   */
  public PipeBuffer(int packetSize) {
    _packetSize = packetSize;
  }

  /** @return if {@link #closeRead} has been called */
  public boolean isReadClosed() { return _gotReadEOF; }

  /**
   * Indicates that the "reader" of this PipeBuffer is finished.  Calling this
   * method does not affect the operation of this PipeBuffer, it merely causes
   * {@link #isReadClosed} to return <code>true</code> from now on.  Useful
   * for coordinating between reader and writer of the PipeBuffer.
   */
  public void closeRead() { _gotReadEOF = true; }
  
  /** @return if {@link #closeWrite} has been called */
  public boolean isWriteClosed() { return _gotWriteEOF; }
  
  /**
   * Indicates that the "writer" of this PipeBuffer is finished.  Calling this
   * method does not affect the operation of this PipeBuffer, it merely causes
   * {@link #isWriteClosed} to return <code>true</code> from now on.  Useful
   * for coordinating between reader and writer of the PipeBuffer.
   */
  public void closeWrite() { _gotWriteEOF = true; }
  
  public int getPacketSize() { return _packetSize; }

  /** @return <code>true</code> if there are bytes to read in the buffer,
      <code>false</code> otherwise */
  public boolean hasRemaining() {
    return(_totalBytes > 0);
  }
  
  /** @return the number of bytes which can be read from this buffer */
  public long remaining() {
    return _totalBytes;
  }

  private void addLast(ByteWrapper bb) {
    _buffers.addLast(bb);
  }
    
  private void removeFirst(boolean canKeep) {
    // ditch the first buffer unless it is the only one left, we are allowed
    // to keep it, and it is at least as big as our packetSize
    if(!canKeep || (_buffers.size() != 1) ||
       (_buffers.getFirst().capacity() < _packetSize)) {
      _buffers.removeFirst();
    } else {
      _buffers.getFirst().clear();
    }
  }

  /**
   * Reads a packet of data from this buffer.  This method will always return
   * data unless there is no data at all in this buffer, in which case a
   * BufferUnderflowException will be thrown.  As such, the actual size of the
   * returned packet may vary.
   */
  public byte[] readPacket()
  {
    if(!hasRemaining()) {
      throw new BufferUnderflowException();
    }

    ByteWrapper bb = _buffers.getFirst();

    boolean canKeep = false;
    byte[] packet = null;
    if(bb.isFullToCapacity()) {
      // this buffer is completely full, just steal the internal buffer
      packet = bb.array();
    } else {
      // this buffer is partially full, need to copy
      packet = new byte[bb.readRemaining()];
      bb.read(packet, 0, packet.length);

      // since we copied the data, we can keep the buffer if desired
      canKeep = true;
    }
    removeFirst(canKeep);

    _totalBytes -= (long)packet.length;
    
    return packet;
  }

  /**
   * Reads the given number of bytes into the given buffer starting at the
   * given position.  Throws a BufferUnderflowException if there are fewer
   * bytes in this buffer than the given length.
   */
  public void read(byte[] buf, int pos, int len)
  {
    if(_totalBytes < (long)len) {
      throw new BufferUnderflowException();
    }
    checkPositionAndLength(pos, len, buf);

    int origLen = len;
    while(len > 0) {
      ByteWrapper bb = _buffers.getFirst();

      int numBytes = bb.read(buf, pos, len);
      pos += numBytes;
      len -= numBytes;

      if(!bb.hasReadRemaining()) {
        removeFirst(true);
      }
    }

    _totalBytes -= (long)origLen;
  }

  /**
   * Skips the given number of bytes in this buffer.  Throws a
   * BufferUnderflowException if there are fewer bytes in this buffer than the
   * given length.
   */
  public void skip(long len)
  {
    if(_totalBytes < len) {
      throw new BufferUnderflowException();
    }
    // bogus value...
    if(len < 0) {
      throw new IllegalArgumentException("bogus length given");
    }
    
    long origLen = len;
    while(len > 0) {
      ByteWrapper bb = _buffers.getFirst();

      long numBytes = bb.skip(len);
      len -= numBytes;

      if(!bb.hasReadRemaining()) {
        removeFirst(true);
      }
    }

    _totalBytes -= origLen;
  }
  
  /**
   * Writes a packet of data to this buffer, where the initial data in the
   * packet will start at the given position and have the given length.
   * Regardless of the given length and position, the entire given buffer will
   * now be owned by this buffer and should never be used again by the caller.
   * Note, this call will never block.
   */
  public void writePacket(byte[] buf, int pos, int len)
  {
    checkPositionAndLength(pos, len, buf);

    if(len > 0) {
      
      if((_totalBytes == 0) && (_buffers.size() > 0)) {
        // we have some empty buffers stashed away, but since we are writing a
        // packet, just ditch them
        _buffers.clear();
      }
        
      // just slap it onto the end (should i copy small buffers?)
      addLast(new ByteWrapper(buf, pos, pos + len));
      _totalBytes += (long)len;
    }    
  }

  /**
   * Writes the given number of bytes from the given buffer starting at the
   * given position.  Note, this call will never block.
   */
  public void write(byte[] buf, int pos, int len)
  {
    checkPositionAndLength(pos, len, buf);

    int origLen = len;
    while(len > 0) {

      if((_buffers.isEmpty()) || (!_buffers.getLast().hasWriteRemaining())) {
        // we ran out of buffers, allocate new buffer
        addLast(new ByteWrapper(Math.max(_packetSize, len)));
      }

      ByteWrapper bb = _buffers.getLast();
      int numBytes = bb.write(buf, pos, len);
      pos += numBytes;
      len -= numBytes;
    }
    
    _totalBytes += (long)origLen;
  }

  /**
   * Clears all remaining bytes from this buffer and releases all internal
   * buffers.
   */
  public void clear() {
    _totalBytes = 0;
    _buffers.clear();
  }

  /**
   * @return the number of "full" packets available in this buffer (where the
   *         actual definition of "full" is up to this buffer).  Regardless of
   *         this value, if this buffer has bytes, they can be read by a call
   *         to readPacket (although this may be less than efficient if
   *         this value is 0).
   */
  public int packetsAvailable()
  {
    // return the number of "full" buffers
    return((_buffers.size() > 0) ?
           (_buffers.getLast().isFullPacket(_packetSize) ?
            _buffers.size() : _buffers.size() - 1) :
           0);
  }

  /**
   * @throws IllegalArgumentException if invalid position and length relative
   *         to the given buffer.
   */
  private static void checkPositionAndLength(int pos, int len,
                                             byte[] buf)
  {
    if((pos < 0) || (len < 0) || ((pos + len) > buf.length)) {
      throw new IllegalArgumentException("bogus position or length given");
    }
  }
  

  /**
   * Utility class which sort of mimics a java.nio.ByteBuffer but doesn't suck
   * as much.  Maintains both a read and write position so that you don't need
   * any of those silly and confusing flip/rewind/reset/etc. methods.
   */
  private static class ByteWrapper
  {
    /** the current read position of this buffer, never > _writePosition */
    private int _readPosition;
    /** the current write position of this buffer, never > _buf.length */
    private int _writePosition;
    /** the actual data buffer */
    private byte[] _buf;

    private ByteWrapper(int size) {
      this(new byte[size], 0, 0);
    }
    
    private ByteWrapper(byte[] buf, int readPosition, int writePosition) {
      _buf = buf;
      _readPosition = readPosition;
      _writePosition = writePosition;
    }

    public int capacity() { return _buf.length; }
    
    public byte[] array() { return _buf; }

    public int write(byte[] b, int pos, int len) {
      int numBytes = Math.min(writeRemaining(), len);
      System.arraycopy(b, pos, _buf, _writePosition, numBytes);
      _writePosition += numBytes;
      return numBytes;
    }

    public long skip(long len) {
      long numBytes = Math.min((long)readRemaining(), len);
      _readPosition += (int)numBytes;
      return numBytes;
    }    

    public int read(byte[] b, int pos, int len) {
      int numBytes = Math.min(readRemaining(), len);
      System.arraycopy(_buf, _readPosition, b, pos, numBytes);
      _readPosition += numBytes;
      return numBytes;
    }

    public boolean hasWriteRemaining() {
      return(writeRemaining() > 0);
    }
    
    public int writeRemaining() {
      return(_buf.length - _writePosition);
    }

    public boolean hasReadRemaining() {
      return(readRemaining() > 0);
    }
    
    public int readRemaining() {
      return(_writePosition - _readPosition);
    }

    /** @return <code>true</code> if this buffer has as many bytes to read as
                its capacity, <code>false</code> otherwise */
    public boolean isFullToCapacity() {
      return(readRemaining() == _buf.length);
    }

    /** @return <code>true</code> if the buffer has no more space for writing
                or at least the given packetSize number of bytes,
                <code>false</code> otherwise */
    public boolean isFullPacket(int packetSize) {
      return(!hasWriteRemaining() || (readRemaining() >= packetSize));
    }
    
    public void clear() {
      _readPosition = _writePosition = 0;
    }
    
  }

  /**
   * PacketInputStream implementation which reads from a PipeBuffer.  One
   * hiccup here is that the single byte {@link #read()} method may fail if an
   * attempt is made to read a byte when none are in the PipeBuffer (as this
   * method is supposed to block in that case and this implementation has no
   * facility for blocking), so avoid that method call if at all
   * possible. Note, implementation is not synchronized.
   */
  public static class InputStreamAdapter extends PacketInputStream
  {
    /** buffer for single byte read calls */
    private SingleByteAdapter _singleByteAdapter = new SingleByteAdapter();
    /** PipeBuffer to read from */
    private PipeBuffer _buffer;

    public InputStreamAdapter() {
      this(PacketInputStream.DEFAULT_PACKET_SIZE);
    }

    public InputStreamAdapter(int packetSize) {
      super(packetSize);
    }
          
    public PipeBuffer getBuffer() {
      return _buffer;
    }

    public void setBuffer(PipeBuffer newBuffer) {
      _buffer = newBuffer;
    }


    /**
     * Returns the PipeBuffer of this InputStreamAdapter, creating if
     * necessary.  If this method creates the PipeBuffer, it will be created
     * with the packet size of this PacketInputStream.  The returned
     * PipeBuffer will be held onto internally for continued use by this
     * InputStreamAdapter.
     */
    public PipeBuffer createPipeBuffer() {
      if(_buffer == null) {
        _buffer = new PipeBuffer(getPacketSize());
      }
      return _buffer;
    }

    /**
     * Shares the PipeBuffer between this InputStreamAdapter and the given
     * OutputStreamAdapter.  If this InputStreamAdapter and the given
     * OutputStreamAdapter already have different PipeBuffers, an IOException
     * will be thrown.  Otherwise, if one of the two adapters already has a
     * PipeBuffer, it will be shared by both adapters.  Otherwise,
     * {@link #createPipeBuffer} is called and the result is shared by both
     * adapters.
     */
    public void connect(OutputStreamAdapter ostream) throws IOException {
      if((ostream.getBuffer() != null) && (getBuffer() != null) &&
         (ostream.getBuffer() != getBuffer())) {
        throw new IOException(
            "Source and sink are already connected to other PipeBuffers");
      }
      PipeBuffer pipeBuffer = ostream.getBuffer();
      if(pipeBuffer == null) {
        pipeBuffer = getBuffer();
      }
      if(pipeBuffer == null) {
        pipeBuffer = createPipeBuffer();
      }
      ostream.setBuffer(pipeBuffer);
      setBuffer(pipeBuffer);
    }
    
    @Override
    public void close() {
      _buffer.closeRead();
    }
    
    @Override
    public int available()
      throws IOException
    {
      return (int)_buffer.remaining();
    }

    @Override
    public int read()
      throws IOException
    {
      if(!_buffer.hasRemaining() && !_buffer.isWriteClosed()) {
        throw new IOException(
            "Cannot call this method with no bytes in the PipeBuffer");
      }
      return _singleByteAdapter.read(this);
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
      if(isFinished()) {
        // all done
        return -1;
      }
      int numBytes = Math.min(len, (int)_buffer.remaining());
      
      _buffer.read(buf, pos, numBytes);
      return numBytes;
    }

    @Override
    public byte[] readPacket(boolean readPartial)
      throws IOException
    {
      if(isFinished()) {
        // all done
        return null;
      }
      
      // readPartial is irrelevant because the PipeBuffer will automagically
      // do a partial read, and has no facility for blocking until an entire
      // packet is available
      return _buffer.readPacket();
    }

    @Override
    public int packetsAvailable()
      throws IOException
    {
      return _buffer.packetsAvailable();
    }

    @Override
    public long skip(long n)
      throws IOException
    {
      if(n <= 0) return 0;

      long toSkip = Math.min(n, _buffer.remaining());
      _buffer.skip(toSkip);
      return toSkip;
    }

    /**
     * @return {@code true} if there will never be anymore bytes to read,
     *         {@code false} otherwise.
     */
    private boolean isFinished() {
      return(!_buffer.hasRemaining() && _buffer.isWriteClosed());
    }
  }

  /**
   * PacketOutputStream implementation which writes to a PipeBuffer.  By
   * default, this class will drop any bytes written after the reader has
   * closed.  Note, implementation is not synchronized.
   */
  public static class OutputStreamAdapter extends PacketOutputStream
  {
    /** buffer for single byte write calls */
    private SingleByteAdapter _singleByteAdapter = new SingleByteAdapter();
    /** PipeBuffer to write to */
    private PipeBuffer _buffer;
    /** flag indicating how to handle write calls made when the reader has
        closed */
    private boolean _throwOnReadClose;

    public OutputStreamAdapter() {
      this(false);
    }
    
    public OutputStreamAdapter(boolean throwOnReadClose) {
      _throwOnReadClose = throwOnReadClose;
    }
    
    public PipeBuffer getBuffer() {
      return _buffer;
    }

    public void setBuffer(PipeBuffer newBuffer) {
      _buffer = newBuffer;
    }

    public boolean getThrowOnReadClose() {
      return _throwOnReadClose;
    }

    /**
     * Sets the throwOnReadClose flag.  Iff <code>true</code>, an exception
     * will be thrown if a write call is made when the reader has been closed.
     * Otherwise, write calls made when the reader is closed will just result
     * in dropped bytes.
     */
    public void setThrowOnReadClose(boolean newThrowOnReadClose) {
      _throwOnReadClose = newThrowOnReadClose;
    }
    
    /**
     * Calls {@link PipeBuffer.InputStreamAdapter#connect} on the given
     * InputStreamAdapter with this OutputStreamAdapter as the parameter.
     */
    public void connect(InputStreamAdapter istream) throws IOException {
      istream.connect(this);
    }
    
    @Override
    public void close() {
      _buffer.closeWrite();
    }
    
    @Override
    public void write(int b) throws IOException {
      _singleByteAdapter.write(b, this);
    }

    @Override
    public void write(byte[] b) throws IOException {
      write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int pos, int len) throws IOException {
      if(_buffer.isReadClosed()) {
        handleReadClosed();
        // don't write if reader is finished (waste of resources)
        return;
      }
      
      _buffer.write(b, pos, len);
    }

    @Override
    public void writePacket(byte[] packet) throws IOException {
      if(_buffer.isReadClosed()) {
        handleReadClosed();
        // don't write if reader is finished (waste of resources)
        return;
      }
      
      _buffer.writePacket(packet, 0, packet.length);
    }

    /**
     * Deals with a read closed situation while still writing.
     */
    private void handleReadClosed() throws IOException {
      if(_throwOnReadClose) {
        throw new IOException("Reader is no longer reading");
      }
    }
    
  }

}
