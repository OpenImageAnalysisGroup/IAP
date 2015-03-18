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

package com.mongodb.gridfs;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;
import org.bson.BSONObject;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The abstract class representing a GridFS file.
 *
 * @mongodb.driver.manual core/gridfs/ GridFS
 */
public abstract class GridFSFile implements DBObject {

    
    // ------------------------------
    // --------- db           -------
    // ------------------------------

    /**
     * Saves the file entry to the files collection
     *
     * @throws MongoException 
     */
    public void save(){
        if ( _fs == null )
            throw new MongoException( "need _fs" );
        _fs._filesCollection.save( this );
    }

    /**
     * Verifies that the MD5 matches between the database and the local file. This should be called after transferring a file.
     *
     * @throws MongoException
     */
    public void validate(){
        if ( _fs == null )
            throw new MongoException( "no _fs" );
        if ( _md5 == null )
            throw new MongoException( "no _md5 stored" );
        
        DBObject cmd = new BasicDBObject( "filemd5" , _id );
        cmd.put( "root" , _fs._bucketName );
        DBObject res = _fs._db.command( cmd );
        if ( res != null && res.containsField( "md5" ) ) {
            String m = res.get( "md5" ).toString();
            if ( m.equals( _md5 ) )
                return;
            throw new MongoException( "md5 differ.  mine [" + _md5 + "] theirs [" + m + "]" );
        }

        // no md5 from the server
        throw new MongoException( "no md5 returned from server: " + res );

    }

    /**
     * Returns the number of chunks that store the file data.
     *
     * @return number of chunks
     */
    public int numChunks(){
        double d = _length;
        d = d / _chunkSize;
        return (int)Math.ceil( d );
    }

    // ------------------------------
    // --------- getters      -------
    // ------------------------------


    /**
     * Gets the id.
     *
     * @return the id of the file.
     */
    public Object getId(){
        return _id;
    }

    /**
     * Gets the filename.
     *
     * @return the name of the file
     */
    public String getFilename(){
        return _filename;
    }

    /**
     * Gets the content type.
     *
     * @return the content type
     */
    public String getContentType(){
        return _contentType;
    }

    /**
     * Gets the file's length.
     *
     * @return the length of the file
     */
    public long getLength(){
        return _length;
    }
    
    /**
     * Gets the size of a chunk.
     *
     * @return the chunkSize
     */
    public long getChunkSize(){
        return _chunkSize;
    }
    
    /**
     * Gets the upload date.
     *
     * @return the date
     */
    public Date getUploadDate(){
        return _uploadDate;
    }

    /**
     * Gets the aliases from the metadata. note: to set aliases, call {@link #put(String, Object)} with {@code "aliases" , List<String>}.
     *
     * @return list of aliases
     */
    @SuppressWarnings("unchecked")
    public List<String> getAliases(){
        return (List<String>)_extradata.get( "aliases" );
    }

    /**
     * Gets the file metadata.
     *
     * @return the metadata
     */
    public DBObject getMetaData(){
        return (DBObject)_extradata.get( "metadata" );
    }

    /**
     * Gets the file metadata.
     *
     * @param metadata metadata to be set
     */
    public void setMetaData(DBObject metadata){
        _extradata.put( "metadata", metadata );
    }

    /**
     * Gets the observed MD5 during transfer
     *
     * @return md5
     */
    public String getMD5(){
        return _md5;
    }

    // ------------------------------
    // --------- DBOBject methods ---
    // ------------------------------

    @Override
    public Object put( String key , Object v ){
        if ( key == null )
            throw new RuntimeException( "key should never be null" );
        else if ( key.equals( "_id" ) )
            _id = v;
        else if ( key.equals( "filename" ) )
            _filename = v == null ? null : v.toString();
        else if ( key.equals( "contentType" ) )
            _contentType = (String)v;
        else if ( key.equals( "length" ) )
            _length = ((Number)v).longValue();
        else if ( key.equals( "chunkSize" ) )
            _chunkSize = ((Number)v).longValue();
        else if ( key.equals( "uploadDate" ) )
            _uploadDate = (Date)v;
        else if ( key.equals( "md5" ) )
            _md5 = (String)v;
        else
            _extradata.put( key , v );
        return v;
    }

    @Override
    public Object get( String key ){
        if ( key == null )
            throw new RuntimeException( "key should never be null" );
        else if ( key.equals( "_id" ) )
            return _id;
        else if ( key.equals( "filename" ) )
            return _filename;
        else if ( key.equals( "contentType" ) )
            return _contentType;
        else if ( key.equals( "length" ) )
            return _length;
        else if ( key.equals( "chunkSize" ) )
            return _chunkSize;
        else if ( key.equals( "uploadDate" ) )
            return _uploadDate;
        else if ( key.equals( "md5" ) )
            return _md5;
        return _extradata.get( key );
    }

    @Override
    public void putAll( BSONObject o ){
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll( Map m ){
        throw new UnsupportedOperationException();
    }

    @Override
    public Map toMap(){
        throw new UnsupportedOperationException();
    }

    @Override
    public Object removeField( String key ){
        throw new UnsupportedOperationException();
    }

    @Override
    @Deprecated
    public boolean containsKey( String s ){
        return containsField( s );
    }

    @Override
    public boolean containsField(String s){
        return keySet().contains( s );
    }

    @SuppressWarnings("unchecked")
    public Set<String> keySet(){
        Set<String> keys = new HashSet();
        keys.addAll(VALID_FIELDS);
        keys.addAll(_extradata.keySet());
        return keys;
    }

    @Override
    public boolean isPartialObject(){
        return false;
    }

    @Override
    public void markAsPartialObject(){
        throw new RuntimeException( "can't load partial GridFSFile file" );
    }
    
    // ----------------------
    // ------- fields -------
    // ----------------------

    @Override
    public String toString(){
        return JSON.serialize( this );
    }

    /**
     * Gets the GridFS associated with this file
     *
     * @return gridFS instance
     */
    protected GridFS getGridFS(){
        return this._fs;
    }

    /**
     * Sets the GridFS associated with this file.
     *
     * @param fs gridFS instance
     */
    protected void setGridFS( GridFS fs ){
        _fs = fs;
    }

    /**
     * @deprecated Please use {@link #getGridFS()} &amp; {@link #setGridFS(GridFS)} instead.
     */
    @Deprecated
    protected GridFS _fs = null;

    Object _id;
    String _filename;
    String _contentType;
    long _length;
    long _chunkSize;
    Date _uploadDate;
    List<String> _aliases;
    DBObject _extradata = new BasicDBObject();
    String _md5;

    @SuppressWarnings("unchecked")
    final static Set<String> VALID_FIELDS = Collections.unmodifiableSet( new HashSet( Arrays.asList( new String[]{ 
                    "_id" , "filename" , "contentType" , "length" , "chunkSize" ,
                    "uploadDate" , "aliases" , "md5"
                } ) ) );
}
