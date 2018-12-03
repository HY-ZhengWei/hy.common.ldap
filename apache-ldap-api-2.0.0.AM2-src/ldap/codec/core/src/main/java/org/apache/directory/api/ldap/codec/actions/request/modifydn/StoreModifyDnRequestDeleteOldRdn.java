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
import org.apache.directory.api.asn1.ber.tlv.BerValue;
import org.apache.directory.api.asn1.ber.tlv.BooleanDecoder;
import org.apache.directory.api.asn1.ber.tlv.BooleanDecoderException;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.decorators.ModifyDnRequestDecorator;
import org.apache.directory.api.ldap.model.message.ModifyDnRequest;
import org.apache.directory.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to store the ModifyDnRequest deleteOldRdn flag
 * <pre>
 * ModifyDNRequest ::= [APPLICATION 12] SEQUENCE { ...
 *     ...
 *     deleteoldrdn BOOLEAN,
 *     ...
 * </pre>
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StoreModifyDnRequestDeleteOldRdn extends GrammarAction<LdapMessageContainer<ModifyDnRequestDecorator>>
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( StoreModifyDnRequestDeleteOldRdn.class );

    /**
     * Instantiates a new action.
     */
    public StoreModifyDnRequestDeleteOldRdn()
    {
        super( "Store ModifyDN request deleteOldRdn flag" );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void action( LdapMessageContainer<ModifyDnRequestDecorator> container ) throws DecoderException
    {
        ModifyDnRequest modifyDnRequest = container.getMessage();

        TLV tlv = container.getCurrentTLV();

        // We get the value. If it's a 0, it's a FALSE. If it's
        // a FF, it's a TRUE. Any other value should be an error,
        // but we could relax this constraint. So if we have
        // something
        // which is not 0, it will be interpreted as TRUE, but we
        // will generate a warning.
        BerValue value = tlv.getValue();

        try
        {
            modifyDnRequest.setDeleteOldRdn( BooleanDecoder.parse( value ) );
        }
        catch ( BooleanDecoderException bde )
        {
            LOG.error( I18n
                .err( I18n.ERR_05125_INVALID_OLD_RDN, Strings.dumpBytes( value.getData() ), bde.getMessage() ) );

            // This will generate a PROTOCOL_ERROR
            throw new DecoderException( bde.getMessage(), bde );
        }

        // We can have an END transition
        container.setGrammarEndAllowed( true );

        if ( LOG.isDebugEnabled() )
        {
            if ( modifyDnRequest.getDeleteOldRdn() )
            {
                LOG.debug( I18n.msg( I18n.MSG_05135_OID_RDN_ATT_WILL_BE_DELETED ) );
            }
            else
            {
                LOG.debug( I18n.msg( I18n.MSG_05136_OID_RDN_ATT_WILL_BE_RETAINED ) );
            }
        }
    }
}
