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
package org.apache.directory.api.ldap.codec.decorators;


import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.model.message.AbandonListener;
import org.apache.directory.api.ldap.model.message.AbandonableRequest;
import org.apache.directory.api.ldap.model.message.MessageTypeEnum;
import org.apache.directory.api.ldap.model.message.SingleReplyRequest;


/**
 * A decorator for the LdapResultResponse message
 *
 * @param <M> The request to be decorated
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class SingleReplyRequestDecorator<M extends SingleReplyRequest>
    extends ResultResponseRequestDecorator<M> implements SingleReplyRequest, AbandonableRequest
{
    /**
     * Makes Request a MessageDecorator.
     *
     * @param codec The LDAP service instance
     * @param decoratedMessage the decorated message
     */
    public SingleReplyRequestDecorator( LdapApiService codec, M decoratedMessage )
    {
        super( codec, decoratedMessage );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public MessageTypeEnum getResponseType()
    {
        return getDecorated().getResponseType();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void abandon()
    {
        ( ( AbandonableRequest ) getDecorated() ).abandon();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAbandoned()
    {
        return ( ( AbandonableRequest ) getDecorated() ).isAbandoned();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public AbandonableRequest addAbandonListener( AbandonListener listener )
    {
        ( ( AbandonableRequest ) getDecorated() ).addAbandonListener( listener );

        return this;
    }
}
