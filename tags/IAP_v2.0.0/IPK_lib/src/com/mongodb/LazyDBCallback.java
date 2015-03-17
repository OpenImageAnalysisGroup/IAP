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

import org.bson.LazyBSONCallback;
import org.bson.types.ObjectId;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * A {@code BSONCallback} for the creation of {@code LazyDBObject} and {@code LazyDBList} instances.
 */
public class LazyDBCallback extends LazyBSONCallback implements DBCallback {

    /**
     * Construct an instance.
     *
     * @param collection the {@code DBCollection} containing the document.  This parameter is no longer used.
     */
    public LazyDBCallback( DBCollection collection ){
        _collection = collection;
        _db = _collection == null ? null : _collection.getDB();
    }

    @Override
    public Object createObject( byte[] data, int offset ){
        LazyDBObject o = new LazyDBObject( data, offset, this );
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

    @Override
    public List createArray(byte[] data, int offset) {
        return new LazyDBList(data, offset, this);
    }

    @Override
    public Object createDBRef( String ns, ObjectId id ){
        return new DBRef( _db, ns, id );
    }

    final DBCollection _collection;
    final DB _db;
    private static final Logger log = Logger.getLogger( LazyDBCallback.class.getName() );
}
