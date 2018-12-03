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
package org.apache.directory.api.ldap.codec.actions.request.search;


import org.apache.directory.api.asn1.DecoderException;
import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.asn1.ber.tlv.BerValue;
import org.apache.directory.api.asn1.ber.tlv.IntegerDecoder;
import org.apache.directory.api.asn1.ber.tlv.IntegerDecoderException;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapCodecConstants;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.decorators.SearchRequestDecorator;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to store the SearchRequest scope
 * <pre>
 * SearchRequest ::= [APPLICATION 3] SEQUENCE {
 *     ...
 *     scope ENUMERATED {
 *         baseObject   (0),
 *         singleLevel  (1),
 *         wholeSubtree (2) },
 *     ...
 * </pre>
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StoreSearchRequestScope extends GrammarAction<LdapMessageContainer<SearchRequestDecorator>>
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( StoreSearchRequestScope.class );

    /**
     * Instantiates a new action.
     */
    public StoreSearchRequestScope()
    {
        super( "Store SearchRequest scope" );
    }


    /**
     * {@inheritDoc}
     */
    public void action( LdapMessageContainer<SearchRequestDecorator> container ) throws DecoderException
    {
        SearchRequest searchRequest = container.getMessage().getDecorated();

        TLV tlv = container.getCurrentTLV();

        // We have to check that this is a correct scope
        BerValue value = tlv.getValue();
        int scope = 0;

        try
        {
            scope = IntegerDecoder.parse( value, LdapCodecConstants.SCOPE_BASE_OBJECT,
                LdapCodecConstants.SCOPE_WHOLE_SUBTREE );
        }
        catch ( IntegerDecoderException ide )
        {
            String msg = I18n.err( I18n.ERR_05149_BAD_SCOPE, value.toString() );
            LOG.error( msg );
            throw new DecoderException( msg, ide );
        }

        searchRequest.setScope( SearchScope.getSearchScope( scope ) );

        if ( LOG.isDebugEnabled() )
        {
            switch ( scope )
            {
                case LdapCodecConstants.SCOPE_BASE_OBJECT:
                    LOG.debug( I18n.msg( I18n.MSG_05162_SEARCHING_WITH_SCOPE, "BASE_OBJECT" ) );
                    break;

                case LdapCodecConstants.SCOPE_SINGLE_LEVEL:
                    LOG.debug( I18n.msg( I18n.MSG_05162_SEARCHING_WITH_SCOPE, "SINGLE_LEVEL" ) );
                    break;

                case LdapCodecConstants.SCOPE_WHOLE_SUBTREE:
                    LOG.debug( I18n.msg( I18n.MSG_05162_SEARCHING_WITH_SCOPE, "WHOLE_SUBTREE" ) );
                    break;

                default:
                    LOG.debug( I18n.msg( I18n.MSG_05162_SEARCHING_WITH_SCOPE, "UNKNOWN" ) );
            }
        }
    }
}
