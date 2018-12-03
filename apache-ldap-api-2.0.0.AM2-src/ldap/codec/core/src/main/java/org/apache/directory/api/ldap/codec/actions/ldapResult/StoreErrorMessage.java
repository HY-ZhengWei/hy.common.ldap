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
package org.apache.directory.api.ldap.codec.actions.ldapResult;


import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.api.MessageDecorator;
import org.apache.directory.api.ldap.model.message.LdapResult;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.ldap.model.message.ResultResponse;
import org.apache.directory.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to set the LdapResult error message.
 *
 * <pre>
 *  LDAPResult ::= SEQUENCE {
 *     ...
 *     errorMessage LDAPString,
 *     ...
 * </pre>
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StoreErrorMessage extends GrammarAction<LdapMessageContainer<MessageDecorator<? extends Message>>>
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( StoreErrorMessage.class );

    /**
     * Instantiates a new error message action.
     */
    public StoreErrorMessage()
    {
        super( "Store error message" );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void action( LdapMessageContainer<MessageDecorator<? extends Message>> container )
    {
        // Get the Value and store it in the BindResponse
        TLV tlv = container.getCurrentTLV();
        String errorMessage;

        // We have to handle the special case of a 0 length error
        // message
        if ( tlv.getLength() == 0 )
        {
            errorMessage = "";
        }
        else
        {
            errorMessage = Strings.utf8ToString( tlv.getValue().getData() );
        }

        ResultResponse response = ( ResultResponse ) container.getMessage();
        LdapResult ldapResult = response.getLdapResult();
        ldapResult.setDiagnosticMessage( errorMessage );

        // We can have an END transition
        container.setGrammarEndAllowed( true );

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_05106_ERROR_MESSAGE_IS, errorMessage ) );
        }
    }
}
