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


import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.api.MessageDecorator;
import org.apache.directory.api.ldap.model.message.LdapResult;
import org.apache.directory.api.ldap.model.message.Message;
import org.apache.directory.api.ldap.model.message.Referral;
import org.apache.directory.api.ldap.model.message.ReferralImpl;
import org.apache.directory.api.ldap.model.message.ResultResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to init referrals to a LdapResult :
 * <pre>
 * LDAPResult ::= SEQUENCE {
 *     ...
 *     referral   [3] Referral OPTIONNAL }
 * </pre>
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class InitReferrals extends GrammarAction<LdapMessageContainer<MessageDecorator<? extends Message>>>
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( InitReferrals.class );

    /**
     * Instantiates a new init referrals action.
     */
    public InitReferrals()
    {
        super( "Init the referrals list" );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void action( LdapMessageContainer<MessageDecorator<? extends Message>> container ) throws DecoderException
    {
        TLV tlv = container.getCurrentTLV();

        // If we hae a Referrals sequence, then it should not be empty
        if ( tlv.getLength() == 0 )
        {
            String msg = I18n.err( I18n.ERR_05105_REFERRAL_MUST_NOT_BE_NULL );
            LOG.error( msg );

            // This will generate a PROTOCOL_ERROR
            throw new DecoderException( msg );
        }

        ResultResponse response = ( ResultResponse ) container.getMessage();
        LdapResult ldapResult = response.getLdapResult();

        Referral referral = new ReferralImpl();
        ldapResult.setReferral( referral );

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_05105_INITIALISNG_REFERRAL_LIST ) );
        }
    }
}
