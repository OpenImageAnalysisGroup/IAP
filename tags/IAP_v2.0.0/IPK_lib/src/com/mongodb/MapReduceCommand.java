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

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * This class groups the argument for a map/reduce operation and can build the underlying command object
 *
 * @dochub mapreduce
 * @mongodb.driver.manual applications/map-reduce Map-Reduce
 */
public class MapReduceCommand {

    /**
     * Represents the different options available for outputting the results of a map-reduce operation.
     *
     * @mongodb.driver.manual reference/command/mapReduce/#mapreduce-out-cmd Output options
     */
    public static enum OutputType {
        /**
         * Save the job output to a collection, replacing its previous content
         */
        REPLACE,
        /**
         * Merge the job output with the existing contents of outputTarget collection
         */
        MERGE,
        /**
         * Reduce the job output with the existing contents of outputTarget collection
         */
        REDUCE,
        /**
         * Return results inline, no result is written to the DB server
         */
        INLINE
    }

    /**
     * Represents the command for a map reduce operation Runs the command in REPLACE output type to a named collection
     *
     * @param inputCollection  collection to use as the source documents to perform the map reduce operation.
     * @param map              a JavaScript function that associates or "maps" a value with a key and emits the key and value pair.
     * @param reduce           a JavaScript function that "reduces" to a single object all the values associated with a particular key.
     * @param outputCollection optional - leave null if want to get the result inline
     * @param type             the type of output
     * @param query            specifies the selection criteria using query operators for determining the documents input to the map
     *                         function.
     * @dochub mapreduce
     * @mongodb.driver.manual reference/command/mapReduce/ Map Reduce Command
     */
    public MapReduceCommand(DBCollection inputCollection, String map, String reduce, String outputCollection, OutputType type,
                            DBObject query) {
        _input = inputCollection.getName();
        _map = map;
        _reduce = reduce;
        _outputTarget = outputCollection;
        _outputType = type;
        _query = query;
    }

    /**
     * Sets the verbosity of the MapReduce job,
     * defaults to 'true'
     * 
     * @param verbose
     *            The verbosity level.
     */
    public void setVerbose( Boolean verbose ){
        _verbose = verbose;
    }

    /**
     * Gets the verbosity of the MapReduce job.
     * 
     * @return the verbosity level.
     */
    public Boolean isVerbose(){
        return _verbose;
    }

    /**
     * Get the name of the collection the MapReduce will read from
     * 
     * @return name of the collection the MapReduce will read from     
     */
    public String getInput(){
        return _input;
    }


    /**
     * Get the map function, as a JS String 
     * 
     * @return the map function (as a JS String)
     */
    public String getMap(){
        return _map;
    }

    /**
     * Gets the reduce function, as a JS String
     * 
     * @return the reduce function (as a JS String)
     */
    public String getReduce(){
        return _reduce;
    }

    /**
     * Gets the output target (name of collection to save to) This value is nullable only if OutputType is set to INLINE
     * 
     * @return The outputCollection
     */
    public String getOutputTarget(){
        return _outputTarget;
    }


    /**
     * Gets the OutputType for this instance.
     *
     * @return The outputType.
     */
    public OutputType getOutputType(){
        return _outputType;
    }


    /**
     * Gets the Finalize JS Function 
     * 
     * @return The finalize function (as a JS String).
     */
    public String getFinalize(){
        return _finalize;
    }

    /**
     * Sets the Finalize JS Function 
     * 
     * @param finalize The finalize function (as a JS String)
     */
    public void setFinalize( String finalize ){
        _finalize = finalize;
    }

    /**
     * Gets the query to run for this MapReduce job
     * 
     * @return The query object
     */
    public DBObject getQuery(){
        return _query;
    }

    /**
     * Gets the (optional) sort specification object 
     * 
     * @return the Sort DBObject
     */
    public DBObject getSort(){
        return _sort;
    }

    /**
     * Sets the (optional) sort specification object
     * 
     * @param sort The sort specification object
     */
    public void setSort( DBObject sort ){
        _sort = sort;
    }

    /**
     * Gets the (optional) limit on input
     * 
     * @return The limit specification object
     */
    public int getLimit(){
        return _limit;
    }

    /**
     * Sets the (optional) limit on input
     * 
     * @param limit The limit specification object
     */
    public void setLimit( int limit ){
        _limit = limit;
    }

    /**
     * Gets the max execution time for this command, in the given time unit.
     *
     * @param timeUnit the time unit to return the value in.
     * @return the maximum execution time
     * @mongodb.server.release 2.6
     * @since 2.12.0
     */
    public long getMaxTime(final TimeUnit timeUnit) {
        return timeUnit.convert(_maxTimeMS, MILLISECONDS);
    }

    /**
     * Sets the max execution time for this command, in the given time unit.
     *
     * @param maxTime  the maximum execution time. A non-zero value requires a server version &gt;= 2.6
     * @param timeUnit the time unit that maxTime is specified in
     * @mongodb.server.release 2.6
     * @since 2.12.0
     */
    public void setMaxTime(final long maxTime, final TimeUnit timeUnit) {
        this._maxTimeMS = MILLISECONDS.convert(maxTime, timeUnit);
    }

    /**
     * Gets the (optional) JavaScript  scope 
     * 
     * @return The JavaScript scope
     */
    public Map<String, Object> getScope(){
        return _scope;
    }

    /**
     * Sets the (optional) JavaScript scope
     * 
     * @param scope The JavaScript scope
     */
    public void setScope( Map<String, Object> scope ){
        _scope = scope;
    }

    /**
     * Gets the (optional) JavaScript mode
     *
     * @return The JavaScript mode
     * @since 2.13
     */
    public Boolean getJsMode(){
        return _jsMode;
    }

    /**
     * Sets the (optional) JavaScript Mode
     *
     * @param jsMode Specifies whether to convert intermediate data into BSON format between the execution of the
     *               map and reduce functions.
     * @since 2.13
     */
    public void setJsMode(Boolean jsMode ){
        _jsMode = jsMode;
    }

    /**
     * Gets the (optional) database name where the output collection should reside
     *
     * @return the name of the database the result is stored in, or null.
     */
    public String getOutputDB() {
        return this._outputDB;
    }

    /**
     * Sets the (optional) database name where the output collection should reside
     *
     * @param outputDB the name of the database to send the Map Reduce output to
     */
    public void setOutputDB(String outputDB) {
        this._outputDB = outputDB;
    }

    /**
     * Turns this command into a DBObject representation of this map reduce command.
     *
     * @return a DBObject that contains the MongoDB document representation of this command.
     */
    public DBObject toDBObject() {
        BasicDBObject cmd = new BasicDBObject();

        cmd.put("mapreduce", _input);
        cmd.put("map", _map);
        cmd.put("reduce", _reduce);

        if (_verbose != null)
            cmd.put("verbose", _verbose);

        BasicDBObject out = new BasicDBObject();
        switch(_outputType) {
            case INLINE:
                out.put("inline", 1);
                break;
            case REPLACE:
                out.put("replace", _outputTarget);
                break;
            case MERGE:
                out.put("merge", _outputTarget);
                break;
            case REDUCE:
                out.put("reduce", _outputTarget);
                break;
        }
        if (_outputDB != null)
            out.put("db", _outputDB);
        cmd.put("out", out);

        if (_query != null)
            cmd.put("query", _query);

        if (_finalize != null) 
            cmd.put( "finalize", _finalize );

        if (_sort != null)
            cmd.put("sort", _sort);

        if (_limit > 0)
            cmd.put("limit", _limit);

        if (_scope != null)
            cmd.put("scope", _scope);

        if (_jsMode != null)
            cmd.put("jsMode", _jsMode);

        if (_extra != null) {
            cmd.putAll(_extra);
        }

        if (_maxTimeMS != 0) {
            cmd.put("maxTimeMS", _maxTimeMS);
        }

        return cmd;
    }

    /**
     * @deprecated use the specific setter methods
     */
    @Deprecated
    public void addExtraOption(String name, Object value) {
        if (_extra == null)
            _extra = new BasicDBObject();
        _extra.put(name, value);
    }

    /**
     * @deprecated use the specific field getter methods
     */
    @Deprecated
    public DBObject getExtraOptions() {
        return _extra;
    }

    /**
     * Sets the read preference for this command. See the * documentation for {@link ReadPreference} for more information.
     *
     * @param preference Read Preference to use
     */
    public void setReadPreference(final ReadPreference preference) {
        _readPref = preference;
    }
    
    /**
     * Gets the read preference
     *
     * @return the readPreference
     */
    public ReadPreference getReadPreference(){
        return _readPref;
    }
    
    
    public String toString() { 
        return toDBObject().toString();
    }

    final String _input;
    final String _map;
    final String _reduce;
    final String _outputTarget;
    ReadPreference _readPref;
    String _outputDB = null;
    final OutputType _outputType;
    final DBObject _query;
    String _finalize;
    DBObject _sort;
    int _limit;
    Map<String, Object> _scope;
    Boolean _verbose = true;
    DBObject _extra;
    private long _maxTimeMS;
    Boolean _jsMode;
}
