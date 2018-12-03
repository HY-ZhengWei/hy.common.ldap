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
package org.apache.directory.api.ldap.codec.actions.response.search.reference;


import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.decorators.SearchResultReferenceDecorator;
import org.apache.directory.api.ldap.model.exception.LdapURLEncodingException;
import org.apache.directory.api.ldap.model.message.Referral;
import org.apache.directory.api.ldap.model.message.ReferralImpl;
import org.apache.directory.api.ldap.model.message.SearchResultReference;
import org.apache.directory.api.ldap.model.url.LdapUrl;
import org.apache.directory.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to store a reference into a searchResultReference
 * <pre>
 * LdapMessage ::= ... SearchResultReference ...
 * SearchResultReference ::= [APPLICATION 19] SEQUENCE OF LDAPURL
 * </pre>
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StoreReference extends GrammarAction<LdapMessageContainer<SearchResultReferenceDecorator>>
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( StoreReference.class );

    /**
     * Instantiates a new store reference action.
     */
    public StoreReference()
    {
        super( "Store a reference" );
    }


    /**
     * {@inheritDoc}
     */
    public void action( LdapMessageContainer<SearchResultReferenceDecorator> container ) throws DecoderException
    {
        SearchResultReference searchResultReference = container.getMessage();

        // Get the Value and store it in the BindRequest
        TLV tlv = container.getCurrentTLV();

        // Get the referral, or create it if not existing
        Referral referral = searchResultReference.getReferral();

        if ( referral == null )
        {
            referral = new ReferralImpl();
            searchResultReference.setReferral( referral );
        }

        // We have to handle the special case of a 0 length list of referrals
        LdapUrl url = LdapUrl.EMPTY_URL;

        if ( tlv.getLength() == 0 )
        {
            referral.addLdapUrl( "" );
        }
        else
        {
            String urlStr = Strings.utf8ToString( tlv.getValue().getData() );

            try
            {
                url = new LdapUrl( urlStr );
                referral.addLdapUrl( urlStr );
            }
            catch ( LdapURLEncodingException luee )
            {
                LOG.error( I18n.err( I18n.ERR_05103_INVALID_URL, urlStr, luee.getMessage() ) );
                throw new DecoderException( I18n.err( I18n.ERR_05104_INVALID_URL, luee.getMessage() ), luee );
            }
        }

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_05184_SEARCH_REFERENCE_URL, url ) );
        }

        // We can have an END transition
        container.setGrammarEndAllowed( true );
    }
}
