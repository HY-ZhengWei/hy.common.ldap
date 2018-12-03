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
package org.apache.directory.api.dsmlv2.response;


import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.model.message.BindResponse;
import org.apache.directory.api.ldap.model.message.BindResponseImpl;
import org.dom4j.Element;


/**
 * DSML Decorator for AuthResponse
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class BindResponseDsml extends AbstractResultResponseDsml<BindResponse> implements BindResponse
{
    /**
     * Creates a new getDecoratedMessage() of AuthResponseDsml.
     * 
     * @param codec The LDAP Service to use
     */
    public BindResponseDsml( LdapApiService codec )
    {
        super( codec, new BindResponseImpl() );
    }


    /**
     * Creates a new getDecoratedMessage() of AuthResponseDsml.
     *
     * @param codec The LDAP Service to use
     * @param ldapMessage the message to decorate
     */
    public BindResponseDsml( LdapApiService codec, BindResponse ldapMessage )
    {
        super( codec, ldapMessage );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Element toDsml( Element root )
    {
        Element element = root.addElement( "authResponse" );

        LdapResultDsml ldapResultDsml = new LdapResultDsml( getCodecService(),
            getDecorated().getLdapResult(), getDecorated() );
        ldapResultDsml.toDsml( element );
        return element;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getServerSaslCreds()
    {
        return getDecorated().getServerSaslCreds();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setServerSaslCreds( byte[] serverSaslCreds )
    {
        getDecorated().setServerSaslCreds( serverSaslCreds );
    }
}
