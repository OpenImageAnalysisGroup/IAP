/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.math3.geometry.euclidean.twod;


/** Simple container for a two-points segment.
 * @version $Id: Segment.java,v 1.1 2012-05-22 20:42:00 klukas Exp $
 * @since 3.0
 */
public class Segment {

    /** Start point of the segment. */
    private final Vector2D start;

    /** End point of the segments. */
    private final Vector2D end;

    /** Line containing the segment. */
    private final Line     line;

    /** Build a segment.
     * @param start start point of the segment
     * @param end end point of the segment
     * @param line line containing the segment
     */
    public Segment(final Vector2D start, final Vector2D end, final Line line) {
        this.start  = start;
        this.end    = end;
        this.line   = line;
    }

    /** Get the start point of the segment.
     * @return start point of the segment
     */
    public Vector2D getStart() {
        return start;
    }

    /** Get the end point of the segment.
     * @return end point of the segment
     */
    public Vector2D getEnd() {
        return end;
    }

    /** Get the line containing the segment.
     * @return line containing the segment
     */
    public Line getLine() {
        return line;
    }

}
