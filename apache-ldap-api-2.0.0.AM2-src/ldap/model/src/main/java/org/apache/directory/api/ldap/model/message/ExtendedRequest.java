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


/**
 * Extended protocol request message used to add to more operations to the
 * protocol. Here's what <a href="http://www.faqs.org/rfcs/rfc2251.html"> RFC
 * 2251</a> says about it:
 * 
 * <pre>
 *  4.12. Extended Operation
 * 
 *   An extension mechanism has been added in this version of LDAP, in
 *   order to allow additional operations to be defined for services not
 *   available elsewhere in this protocol, for instance digitally signed
 *   operations and results.
 * 
 *   The extended operation allows clients to make requests and receive
 *   responses with predefined syntaxes and semantics.  These may be
 *   defined in RFCs or be private to particular implementations.  Each
 *   request MUST have a unique OBJECT IDENTIFIER assigned to it.
 * 
 *        ExtendedRequest ::= [APPLICATION 23] SEQUENCE {
 *                requestName      [0] LDAPOID,
 *                requestValue     [1] OCTET STRING OPTIONAL }
 * 
 *   The requestName is a dotted-decimal representation of the OBJECT
 *   IDENTIFIER corresponding to the request. The requestValue is
 *   information in a form defined by that request, encapsulated inside an
 *   OCTET STRING.
 * </pre>
 * <br>
 *  
 *  @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 * 
 */
public interface ExtendedRequest extends SingleReplyRequest
{
    /**
     * Gets the Object Identifier corresponding to the extended request type.
     * This is the <b>requestName</b> portion of the ExtendedRequst PDU.
     * 
     * @return the dotted-decimal representation as a String of the OID
     */
    String getRequestName();


    /**
     * Sets the Object Identifier corresponding to the extended request type.
     * 
     * @param oid the dotted-decimal representation as a String of the OID
     * @return The ExtendedRequest instance
     */
    ExtendedRequest setRequestName( String oid );


    /**
     * {@inheritDoc}
     */
    @Override
    ExtendedRequest setMessageId( int messageId );


    /**
     * {@inheritDoc}
     */
    @Override
    ExtendedRequest addControl( Control control );


    /**
     * {@inheritDoc}
     */
    @Override
    ExtendedRequest addAllControls( Control[] controls );


    /**
     * {@inheritDoc}
     */
    @Override
    ExtendedRequest removeControl( Control control );
}
