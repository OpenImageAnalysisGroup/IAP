/*
 * Copyright (c) 2008-2014 MongoDB, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bson.types;

import java.io.Serializable;
import java.util.Date;

/**
 * This is used for internal increment values. For storing normal dates in MongoDB, you should use java.util.Date {@code time} is seconds
 * since epoch {@code inc} is an ordinal.
 */
public class BSONTimestamp implements Comparable<BSONTimestamp>, Serializable {

    private static final long serialVersionUID = -3268482672267936464L;
    
    static final boolean D = Boolean.getBoolean( "DEBUG.DBTIMESTAMP" );

    /**
     * Construct a new instance with a null time and a 0 increment.
     */
    public BSONTimestamp(){
        _inc = 0;
        _time = null;
    }

    /**
     * Construct a new instance for the given time and increment.
     *
     * @param time the number of seconds since the epoch
     * @param inc  the increment.
     */
    public BSONTimestamp(final int time, final int inc) {
        _time = new Date( time * 1000L );
        _inc = inc;
    }

    /**
     * Gets the time in seconds since epoch.
     *
     * @return an int representing time in seconds since epoch
     */
    public int getTime(){
        if ( _time == null )
            return 0;
        return (int)(_time.getTime() / 1000);
    }
    
    /**
     * Gets the increment value.
     *
     * @return an incrementing ordinal for operations within a given second
     */
    public int getInc(){
        return _inc;
    }

    @Override
    public String toString(){
        return "TS time:" + _time + " inc:" + _inc;
    }
    
    @Override
    public int compareTo(BSONTimestamp ts) {
        if(getTime() != ts.getTime()) {
            return getTime() - ts.getTime();
        }
        else{
            return getInc() - ts.getInc();
        }
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + _inc;
        result = prime * result + getTime();
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (obj instanceof BSONTimestamp) {
            BSONTimestamp t2 = (BSONTimestamp) obj;
            return getTime() == t2.getTime() && getInc() == t2.getInc();
        }
        return false;
    }

    final int _inc;
    final Date _time;

}
