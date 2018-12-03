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
package org.apache.directory.api.ldap.model.message;


import java.util.Map;


/**
 * Root interface for all LDAP message type interfaces.
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public interface Message
{
    /**
     * Gets the LDAP message type code associated with this Message. Each
     * request and response type has a unique message type code defined by the
     * protocol in <a href="http://www.faqs.org/rfcs/rfc2251.html">RFC 2251</a>.
     * 
     * @return the message type code.
     */
    MessageTypeEnum getType();


    /**
     * Gets the controls associated with this message mapped by OID.
     * 
     * @return Map of OID strings to Control object instances.
     */
    Map<String, Control> getControls();


    /**
     * Gets the control associated with the given OID.
     * 
     * @param oid The Cntrol's OID we are looking for
     * @return The Control object instance with the OID.
     */
    Control getControl( String oid );


    /**
     * Checks whether or not this message has the specified control.
     *
     * @param oid the OID of the control
     * @return true if this message has the control, false if it does not
     */
    boolean hasControl( String oid );


    /**
     * Adds a control to this Message.
     * 
     * @param control the control to add.
     * @return A Message reference
     */
    Message addControl( Control control );


    /**
     * Adds an array of controls to this Message.
     * 
     * @param controlsToAdd the controls to add.
     * @return A Message reference
     */
    Message addAllControls( Control[] controlsToAdd );


    /**
     * Deletes a control removing it from this Message.
     * 
     * @param control the control to remove.
     * @return A Message reference
     */
    Message removeControl( Control control );


    /**
     * Gets the session unique message sequence id for this message. Requests
     * and their responses if any have the same message id. Clients at the
     * initialization of a session start with the first message's id set to 1
     * and increment it with each transaction.
     * 
     * @return the session unique message id.
     */
    int getMessageId();


    /**
     * Gets a message scope parameter. Message scope parameters are temporary
     * variables associated with a message and are set locally to be used to
     * associate housekeeping information with a request or its processing.
     * These parameters are never transmitted nor recieved, think of them as
     * transient data associated with the message or its processing. These
     * transient parameters are not locked down so modifications can occur
     * without firing LockExceptions even when this Lockable is in the locked
     * state.
     * 
     * @param key the key used to access a message parameter.
     * @return the transient message parameter value.
     */
    Object get( Object key );


    /**
     * Sets a message scope parameter. These transient parameters are not locked
     * down so modifications can occur without firing LockExceptions even when
     * this Lockable is in the locked state.
     * 
     * @param key the parameter key
     * @param value the parameter value
     * @return the old value or null
     */
    Object put( Object key, Object value );


    /**
     * Sets the Message ID for this request
     * @param messageId The message Id
     * @return A Message reference
     */
    Message setMessageId( int messageId );
}
