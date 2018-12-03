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
package org.apache.directory.api.dsmlv2.request;


import java.nio.ByteBuffer;

import org.apache.directory.api.asn1.EncoderException;
import org.apache.directory.api.dsmlv2.ParserUtils;
import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.model.message.AbandonListener;
import org.apache.directory.api.ldap.model.message.AbandonableRequest;
import org.apache.directory.api.ldap.model.message.ResultResponse;
import org.apache.directory.api.ldap.model.message.ResultResponseRequest;
import org.dom4j.Element;


/**
 * Abstract class for DSML requests.
 *
 * @param <E> The response request result type
 * @param <F> The response result type
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public abstract class AbstractResultResponseRequestDsml<E extends ResultResponseRequest, F extends ResultResponse>
    extends AbstractRequestDsml<E>
    implements ResultResponseRequest, AbandonableRequest
{
    /**
     * Creates a new instance of AbstractRequestDsml.
     *
     * @param codec The LDAP Service to use
     * @param ldapMessage the message to decorate
     */
    public AbstractResultResponseRequestDsml( LdapApiService codec, E ldapMessage )
    {
        super( codec, ldapMessage );
    }


    /**
     * Creates the Request Element and adds RequestID and Controls.
     *
     * @param root the root element
     * @return the Request Element of the given name containing
     */
    @Override
    public Element toDsml( Element root )
    {
        Element element = root.addElement( getRequestName() );

        // Request ID
        int requestID = getDecorated().getMessageId();
        if ( requestID > 0 )
        {
            element.addAttribute( "requestID", Integer.toString( requestID ) );
        }

        // Controls
        ParserUtils.addControls( getCodecService(), element, getDecorated().getControls().values() );

        return element;
    }


    /**
     * Gets the name of the request according to the type of the decorated element.
     *
     * @return
     *      the name of the request according to the type of the decorated element.
     */
    private String getRequestName()
    {
        switch ( getDecorated().getType() )
        {
            case ABANDON_REQUEST:
                return "abandonRequest";

            case ADD_REQUEST:
                return "addRequest";

            case BIND_REQUEST:
                return "authRequest";

            case COMPARE_REQUEST:
                return "compareRequest";

            case DEL_REQUEST:
                return "delRequest";

            case EXTENDED_REQUEST:
                return "extendedRequest";

            case MODIFYDN_REQUEST:
                return "modDNRequest";

            case MODIFY_REQUEST:
                return "modifyRequest";

            case SEARCH_REQUEST:
                return "searchRequest";

            default:
                return "error";
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int computeLength()
    {
        return 0;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ByteBuffer encode( ByteBuffer buffer ) throws EncoderException
    {
        return null;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ResultResponse getResultResponse()
    {
        return getDecorated().getResultResponse();
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
