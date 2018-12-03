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
package org.apache.directory.api.ldap.codec.actions.response.compare;


import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.decorators.CompareResponseDecorator;
import org.apache.directory.api.ldap.model.message.CompareResponseImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to initialize the CompareResponse
 * <pre>
 * LdapMessage ::= ... CompareResponse ...
 * CompareResponse ::= [APPLICATION 15] LDAPResult
 * </pre>
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class InitCompareResponse extends GrammarAction<LdapMessageContainer<CompareResponseDecorator>>
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( InitCompareResponse.class );


    /**
     * Instantiates a new action.
     */
    public InitCompareResponse()
    {
        super( "Compare Response initialization" );
    }


    /**
     * {@inheritDoc}
     */
    public void action( LdapMessageContainer<CompareResponseDecorator> container ) throws DecoderException
    {
        // Now, we can allocate the CompareResponse Object
        CompareResponseDecorator compareResponse = new CompareResponseDecorator(
            container.getLdapCodecService(), new CompareResponseImpl( container.getMessageId() ) );
        container.setMessage( compareResponse );

        // We will check that the request is not null
        TLV tlv = container.getCurrentTLV();

        if ( tlv.getLength() == 0 )
        {
            String msg = I18n.err( I18n.ERR_05148_NULL_COMPARE_REQUEST );
            LOG.error( msg );
            throw new DecoderException( msg );
        }

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_05169_COMPARE_RESPONSE ) );
        }
    }
}
