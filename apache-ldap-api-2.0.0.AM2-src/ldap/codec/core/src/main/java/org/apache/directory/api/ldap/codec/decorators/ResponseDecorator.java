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
import org.apache.directory.api.ldap.codec.api.MessageDecorator;
import org.apache.directory.api.ldap.model.message.LdapResult;
import org.apache.directory.api.ldap.model.message.ResultResponse;


/**
 * A decorator for the Response message. It will store the LdapResult.
 * 
 * @param <M> The response to be decorated
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class ResponseDecorator<M extends ResultResponse> extends MessageDecorator<M> implements ResultResponse
{
    /** The LdapResult decorator */
    private LdapResultDecorator ldapResultDecorator;


    /**
     * Makes a AddRequest encodable.
     *
     * @param codec The LDAP service instance
     * @param decoratedMessage the decorated AddRequest
     */
    public ResponseDecorator( LdapApiService codec, M decoratedMessage )
    {
        super( codec, decoratedMessage );

        ldapResultDecorator = new LdapResultDecorator( codec, decoratedMessage.getLdapResult() );
    }


    /**
     * @return the ldapResultDecorator
     */
    @Override
    public LdapResult getLdapResult()
    {
        return ldapResultDecorator;
    }


    /**
     * @param ldapResultDecorator the ldapResultDecorator to set
     */
    public void setLdapResult( LdapResultDecorator ldapResultDecorator )
    {
        this.ldapResultDecorator = ldapResultDecorator;
    }
}
