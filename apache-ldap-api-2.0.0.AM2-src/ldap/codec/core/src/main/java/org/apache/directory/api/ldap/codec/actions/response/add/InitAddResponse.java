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
package org.apache.directory.api.ldap.codec.actions.response.add;


import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.decorators.AddResponseDecorator;
import org.apache.directory.api.ldap.model.message.AddResponseImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to initialize the AddResponse response
 * <pre>
 * LdapMessage ::= ... AddResponse ...
 * AddResponse ::= [APPLICATION 9] SEQUENCE { ...
 * </pre>
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class InitAddResponse extends GrammarAction<LdapMessageContainer<AddResponseDecorator>>
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( InitAddResponse.class );


    /**
     * Instantiates a new action.
     */
    public InitAddResponse()
    {
        super( "Init AddResponse" );
    }


    /**
     * {@inheritDoc}
     */
    public void action( LdapMessageContainer<AddResponseDecorator> container ) throws DecoderException
    {
        // Now, we can allocate the AddResponse Object
        AddResponseDecorator addResponse = new AddResponseDecorator(
            container.getLdapCodecService(), new AddResponseImpl( container.getMessageId() ) );
        container.setMessage( addResponse );

        // We will check that the request is not null
        TLV tlv = container.getCurrentTLV();

        int expectedLength = tlv.getLength();

        if ( expectedLength == 0 )
        {
            String msg = I18n.err( I18n.ERR_05146_NULL_ADD_RESPONSE );
            LOG.error( msg );
            throw new DecoderException( msg );
        }

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_05167_ADD_RESPONSE ) );
        }
    }
}
