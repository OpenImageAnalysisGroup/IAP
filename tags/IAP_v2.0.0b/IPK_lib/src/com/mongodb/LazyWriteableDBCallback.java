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

package com.mongodb;

import java.util.Iterator;

/**
 * @deprecated This class will be removed in a future release.  There is no replacement.
 */
@Deprecated
public class LazyWriteableDBCallback extends LazyDBCallback {

    /**
     * Construct an instance.
     * @param collection the collection containing the documents to be decoded
     */
    public LazyWriteableDBCallback( DBCollection collection ){
	super(collection);
    }

    @Override
    public Object createObject( byte[] data, int offset ){
        LazyWriteableDBObject o = new LazyWriteableDBObject( data, offset, this );
        //log.info("Created inner BSONObject: " + o);
        // need to detect DBRef but must make sure we dont search through all fields
        // $ref must be 1st key
        Iterator it = o.keySet().iterator();
        if ( it.hasNext() && it.next().equals( "$ref" ) &&
             o.containsField( "$id" ) ){
            return new DBRef( _db, o );
        }
        return o;
    }
}
