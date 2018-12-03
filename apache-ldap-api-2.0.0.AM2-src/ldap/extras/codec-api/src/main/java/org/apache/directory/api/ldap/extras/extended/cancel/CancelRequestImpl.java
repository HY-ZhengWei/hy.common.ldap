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
package org.apache.directory.api.ldap.extras.extended.cancel;


import org.apache.directory.api.ldap.model.message.AbstractExtendedRequest;


/**
 * Implement the extended Cancel Request as described in RFC 3909.
 * 
 * It's grammar is :
 * 
 * <pre>
 * cancelRequestValue ::= SEQUENCE {
 *        cancelID        MessageID
 *                        -- MessageID is as defined in [RFC2251]
 * }
 * </pre>
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class CancelRequestImpl extends AbstractExtendedRequest implements CancelRequest
{
    /** The cancelId of the request to be canceled */
    private int cancelId;

    /**
     * Creates a new instance of CancelRequest.
     *
     * @param messageId the message id
     * @param cancelId the message id of the request to cancel
     */
    public CancelRequestImpl( int messageId, int cancelId )
    {
        super( messageId );
        setRequestName( EXTENSION_OID );

        this.cancelId = cancelId;
    }


    /**
     * Creates a new instance of CancelRequest.
     */
    public CancelRequestImpl()
    {
        setRequestName( EXTENSION_OID );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getCancelId()
    {
        return cancelId;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setCancelId( int cancelId )
    {
        this.cancelId = cancelId;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public CancelResponse getResultResponse()
    {
        if ( getResponse() == null )
        {
            setResponse( new CancelResponseImpl( cancelId ) );
        }

        return ( CancelResponse ) getResponse();
    }
}
