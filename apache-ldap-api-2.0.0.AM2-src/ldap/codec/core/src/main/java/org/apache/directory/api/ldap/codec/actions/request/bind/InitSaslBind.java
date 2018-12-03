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


import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.api.ResponseCarryingException;
import org.apache.directory.api.ldap.codec.decorators.BindRequestDecorator;
import org.apache.directory.api.ldap.model.message.BindRequest;
import org.apache.directory.api.ldap.model.message.BindResponseImpl;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to store the BindRequest version MessageID.
 * <pre>
 * BindRequest ::= [APPLICATION 0] SEQUENCE {
 *     ....
 *     authentication          AuthenticationChoice }
 *
 * AuthenticationChoice ::= CHOICE {
 *     ...
 *     sasl                  [3] SaslCredentials }
 *     ...
 *
 * We have to create an Authentication Object to store the credentials.
 * </pre>
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class InitSaslBind extends GrammarAction<LdapMessageContainer<BindRequestDecorator>>
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( InitSaslBind.class );

    /**
     * Instantiates a new action.
     */
    public InitSaslBind()
    {
        super( "Initialize Bind SASL Authentication" );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void action( LdapMessageContainer<BindRequestDecorator> container ) throws DecoderException
    {
        BindRequest bindRequestMessage = container.getMessage();
        TLV tlv = container.getCurrentTLV();

        // We will check that the sasl is not null
        if ( tlv.getLength() == 0 )
        {
            String msg = I18n.err( I18n.ERR_05116_SASL_CREDS_CANT_BE_NULL );
            LOG.error( msg );

            BindResponseImpl response = new BindResponseImpl( bindRequestMessage.getMessageId() );

            throw new ResponseCarryingException( msg, response, ResultCodeEnum.INVALID_CREDENTIALS,
                bindRequestMessage.getDn(), null );
        }

        bindRequestMessage.setSimple( false );

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_05115_SASL_CREDS_CREATED ) );
        }
    }
}
