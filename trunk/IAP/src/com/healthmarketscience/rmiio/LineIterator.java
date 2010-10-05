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

import java.io.BufferedReader;
import java.io.IOException;
import static com.healthmarketscience.rmiio.RmiioUtil.closeQuietly;

/**
 * CloseableIOIterator that reads lines from a BufferedReader, 
 * optionally trimming whitespace and/or skipping blank lines.
 *
 * @author James Ahlborn
 */
public class LineIterator extends AbstractCloseableIOIterator<String> {

  /** 
   * We'll read lines from this reader
   */
  private final BufferedReader _reader;

  /**
   * The line to be returned by the next call to next()
   */
  private String _next;

  /**
   * If true, we'll trim() the lines before emitting them.
   */
  private boolean _trimWhitespace;

  /**
   * If true, we'll skip over blank lines.
   */
  private boolean _skipBlankLines;

  /**
   * Creates a new <code>LineIterator</code> instance that will
   * read lines from the given {@link BufferedReader} and return them from
   * calls to next().
   *
   * @param reader the source of lines for this iterator
   * @param trimWhitespace if true, leading and trailing whitespace will be
   *                       trimmed from each line
   * @param skipBlankLines if true, empty lines will be skipped
   */
  public LineIterator(BufferedReader reader, 
                      boolean trimWhitespace,
                      boolean skipBlankLines)
    throws IOException
  {
    _reader = reader;
    _skipBlankLines = skipBlankLines;
    _trimWhitespace = trimWhitespace;
    getNext();
  }

  private void getNext() throws IOException
  {
    while(true) {
      _next = _reader.readLine();
      if(_next != null) {
        if (_trimWhitespace) {
          _next = _next.trim();
        }
        if(_skipBlankLines && _next.length() == 0) {
          continue;
        }
      }
      return;
    }
  }

  public boolean hasNext() throws IOException
  {
    return(_next != null);
  }

  @Override
  protected String nextImpl() throws IOException
  {
    String cur = _next;
    getNext();
    return cur;
  }

  @Override
  protected void closeImpl() {
    closeQuietly(_reader);
  }

}
