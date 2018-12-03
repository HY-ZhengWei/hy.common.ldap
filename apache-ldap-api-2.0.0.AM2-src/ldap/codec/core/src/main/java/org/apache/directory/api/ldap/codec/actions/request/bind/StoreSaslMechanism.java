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
package org.apache.directory.api.ldap.codec.actions.request.bind;


import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.decorators.BindRequestDecorator;
import org.apache.directory.api.ldap.model.message.BindRequest;
import org.apache.directory.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to store the BindRequest version MessageID.
 * <pre>
 * SaslCredentials ::= SEQUENCE {
 *     mechanism   LDAPSTRING,
 *     ...
 * </pre>
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StoreSaslMechanism extends GrammarAction<LdapMessageContainer<BindRequestDecorator>>
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( StoreSaslMechanism.class );

    /**
     * Instantiates a new action.
     */
    public StoreSaslMechanism()
    {
        super( "Store SASL mechanism" );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void action( LdapMessageContainer<BindRequestDecorator> container )
    {
        BindRequest bindRequestMessage = container.getMessage();
        TLV tlv = container.getCurrentTLV();

        // We have to handle the special case of a 0 length
        // mechanism
        if ( tlv.getLength() == 0 )
        {
            bindRequestMessage.setSaslMechanism( "" );
        }
        else
        {
            bindRequestMessage.setSaslMechanism( Strings.utf8ToString( tlv.getValue().getData() ) );
        }

        // We can have an END transition
        container.setGrammarEndAllowed( true );

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_05118_THE_SASL_MECHANISM_IS, bindRequestMessage.getSaslMechanism() ) );
        }
    }
}
