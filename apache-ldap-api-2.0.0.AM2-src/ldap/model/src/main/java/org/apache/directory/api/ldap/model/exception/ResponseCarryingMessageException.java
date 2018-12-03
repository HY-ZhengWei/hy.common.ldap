/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */

package org.apache.directory.api.ldap.model.exception;


import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.util.exception.RuntimeMultiException;


/**
 * This exception is thrown when a message processing error occurs.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ResponseCarryingMessageException extends RuntimeMultiException
{
    /**
     * Declares the Serial Version Uid.
     * 
     * @see <a
     *      href="http://c2.com/cgi/wiki?AlwaysDeclareSerialVersionUid">Always
     *      Declare Serial Version Uid</a>
     */
    private static final long serialVersionUID = 1L;

    /** The response with the error cause */
    private Message response;


    /**
     * Constructs an Exception without a message.
     */
    public ResponseCarryingMessageException()
    {
        super();
    }


    /**
     * Constructs an Exception without a message.
     *
     * @param cause The original cause
     */
    public ResponseCarryingMessageException( Throwable cause )
    {
        super( "", cause );
    }


    /**
     * Constructs an Exception with a detailed message.
     * 
     * @param message The message associated with the exception.
     */
    public ResponseCarryingMessageException( String message )
    {
        super( message );
    }


    /**
     * Constructs an Exception with a detailed message.
     * 
     * @param message The message associated with the exception.
     * @param cause The original cause
     */
    public ResponseCarryingMessageException( String message, Throwable cause )
    {
        super( message, cause );
    }


    /**
     * Set a response if we get an exception while parsing the message
     * @param response the constructed response
     */
    public void setResponse( Message response )
    {
        this.response = response;
    }


    /**
     * Get the constructed response
     * @return The constructed response
     */
    public Message getResponse()
    {
        return response;
    }
}
