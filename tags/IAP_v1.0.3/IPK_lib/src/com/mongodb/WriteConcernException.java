/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
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
 *
 */

package com.mongodb;

/**
 * An exception representing an error reported due to a write failure.
 */
public class WriteConcernException extends MongoException {

    private static final long serialVersionUID = 841056799207039974L;

    private final CommandResult commandResult;

    /**
     * Construct a new instance with the CommandResult from getlasterror command
     *
     * @param commandResult the command result
     */
    public WriteConcernException(final CommandResult commandResult) {
        super(commandResult.getCode(), commandResult.toString());
        this.commandResult = commandResult;
    }

    /**
     * Gets the getlasterror command result document.
     *
     * @return the command result
     */
    public CommandResult getCommandResult() {
        return commandResult;
    }
}
