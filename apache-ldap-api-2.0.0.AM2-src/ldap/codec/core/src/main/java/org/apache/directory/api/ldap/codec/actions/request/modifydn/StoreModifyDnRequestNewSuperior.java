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
package org.apache.directory.api.ldap.codec.actions.request.modifydn;


import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.api.ResponseCarryingException;
import org.apache.directory.api.ldap.codec.decorators.ModifyDnRequestDecorator;
import org.apache.directory.api.ldap.model.exception.LdapInvalidDnException;
import org.apache.directory.api.ldap.model.message.ModifyDnRequest;
import org.apache.directory.api.ldap.model.message.ModifyDnResponseImpl;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to store the ModifyDnRequest new Superior
 * <pre>
 * ModifyDNRequest ::= [APPLICATION 12] SEQUENCE { ...
 *     ...
 *     newSuperior [0] LDAPDN OPTIONAL }
 * </pre>
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StoreModifyDnRequestNewSuperior extends GrammarAction<LdapMessageContainer<ModifyDnRequestDecorator>>
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( StoreModifyDnRequestNewSuperior.class );

    /**
     * Instantiates a new action.
     */
    public StoreModifyDnRequestNewSuperior()
    {
        super( "Store new superior" );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void action( LdapMessageContainer<ModifyDnRequestDecorator> container ) throws DecoderException
    {
        ModifyDnRequest modifyDnRequest = container.getMessage();

        // Get the Value and store it in the modifyDNRequest
        TLV tlv = container.getCurrentTLV();

        // We have to handle the special case of a 0 length matched
        // Dn
        Dn newSuperior = Dn.EMPTY_DN;

        if ( tlv.getLength() == 0 )
        {

            if ( modifyDnRequest.getDeleteOldRdn() )
            {
                // This will generate a PROTOCOL_ERROR
                throw new DecoderException( I18n.err( I18n.ERR_05128_NULL_SUPERIOR ) );
            }
            else
            {
                if ( LOG.isWarnEnabled() )
                {
                    LOG.warn( I18n.msg( I18n.MSG_05139_NULL_NEW_SUPERIOR ) );
                }
            }

            modifyDnRequest.setNewSuperior( newSuperior );
        }
        else
        {
            byte[] dnBytes = tlv.getValue().getData();
            String dnStr = Strings.utf8ToString( dnBytes );

            try
            {
                newSuperior = new Dn( dnStr );
            }
            catch ( LdapInvalidDnException ine )
            {
                String msg = I18n.err( I18n.ERR_05129_INVALID_NEW_SUPERIOR, dnStr, Strings.dumpBytes( dnBytes ) );
                LOG.error( I18n.err( I18n.ERR_05114_ERROR_MESSAGE, msg, ine.getMessage() ) );

                ModifyDnResponseImpl response = new ModifyDnResponseImpl( modifyDnRequest.getMessageId() );
                throw new ResponseCarryingException( msg, response, ResultCodeEnum.INVALID_DN_SYNTAX,
                    modifyDnRequest.getName(), ine );
            }

            modifyDnRequest.setNewSuperior( newSuperior );
        }

        // We can have an END transition
        container.setGrammarEndAllowed( true );

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_05140_NEW_SUPERIOR_DN, newSuperior ) );
        }
    }
}
