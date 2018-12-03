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
package org.apache.directory.api.ldap.codec.actions.request.abandon;


import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.asn1.ber.tlv.BerValue;
import org.apache.directory.api.asn1.ber.tlv.IntegerDecoder;
import org.apache.directory.api.asn1.ber.tlv.IntegerDecoderException;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.decorators.AbandonRequestDecorator;
import org.apache.directory.api.ldap.model.message.AbandonRequest;
import org.apache.directory.api.ldap.model.message.AbandonRequestImpl;
import org.apache.directory.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to initialize the AbandonRequest
 * <pre>
 * LdapMessage ::= ... AbandonRequest ...
 * AbandonRequest ::= [APPLICATION 16] MessageID
 * </pre>
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class InitAbandonRequest extends GrammarAction<LdapMessageContainer<AbandonRequestDecorator>>
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( InitAbandonRequest.class );


    /**
     * Instantiates a new action.
     */
    public InitAbandonRequest()
    {
        super( "Init Abandon Request" );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void action( LdapMessageContainer<AbandonRequestDecorator> container ) throws DecoderException
    {
        // Create the AbandonRequest LdapMessage instance and store it in the container
        AbandonRequest internalAbandonRequest = new AbandonRequestImpl();
        internalAbandonRequest.setMessageId( container.getMessageId() );
        AbandonRequestDecorator abandonRequest = new AbandonRequestDecorator(
            container.getLdapCodecService(), internalAbandonRequest );
        container.setMessage( abandonRequest );

        // The current TLV should be a integer
        // We get it and store it in MessageId
        TLV tlv = container.getCurrentTLV();

        BerValue value = tlv.getValue();

        if ( ( value == null ) || ( value.getData() == null ) )
        {
            String msg = I18n.err( I18n.ERR_05109_ABANDON_REQ_MSG_ID_NULL );
            LOG.error( msg );

            // This will generate a PROTOCOL_ERROR
            throw new DecoderException( msg );
        }

        try
        {
            int abandonnedMessageId = IntegerDecoder.parse( value, 0, Integer.MAX_VALUE );

            abandonRequest.setAbandoned( abandonnedMessageId );

            if ( LOG.isDebugEnabled() )
            {
                LOG.debug( I18n.msg( I18n.MSG_05110_ABANDON_MSG_ID_DECODED, Integer.valueOf( abandonnedMessageId ) ) );
            }

            container.setGrammarEndAllowed( true );
        }
        catch ( IntegerDecoderException ide )
        {
            LOG.error( I18n
                .err( I18n.ERR_05110_INVALID_ABANDON_REQ_MSG_ID, Strings.dumpBytes( value.getData() ), ide.getMessage() ) );

            // This will generate a PROTOCOL_ERROR
            throw new DecoderException( ide.getMessage(), ide );
        }
    }
}
