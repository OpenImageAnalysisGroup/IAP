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

/**
 * An exception indicating that a command sent to a MongoDB server returned a failure.
 */
public class CommandFailureException extends MongoException {
    private static final long serialVersionUID = -1180715413196161037L;
    private final CommandResult commandResult;

    /**
     * Construct a new instance with the CommandResult from a failed command
     *
     * @param commandResult the result
     * @deprecated for internal use only
     */
    @Deprecated
    public CommandFailureException(CommandResult commandResult){
        super(ServerError.getCode(commandResult), commandResult.toString());
        this.commandResult = commandResult;
    }

    /**
     * Gets the address of the server that the command executed on.
     *
     * @return the address of the server that the command executed on
     * @since 2.13
     */
    public ServerAddress getServerAddress() {
        return commandResult.getServerUsed();
    }

    /**
     * Gets the error message from the command failure, typically from the "errmsg" property of the document returned from the failed
     * command.
     *
     * @return the error message
     */
    public String getErrorMessage() {
        return commandResult.getErrorMessage();
    }

    /**
     * Gets the command result document.
     *
     * @return the command result
     * @deprecated Use either {@link #getErrorMessage()} or {@link #getCode()} or {@link #getServerAddress()}
     */
    @Deprecated
    public CommandResult getCommandResult() {
        return commandResult;
    }
}
