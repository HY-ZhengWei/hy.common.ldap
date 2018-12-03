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
package org.apache.directory.api.ldap.codec.actions.response.search.entry;


import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.decorators.SearchResultEntryDecorator;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to store a Value to an search result entry
 * <pre>
 * PartialAttributeList ::= SEQUENCE OF SEQUENCE {
 *     ...
 *     vals SET OF AttributeValue }
 *
 * AttributeValue ::= OCTET STRING
 * </pre>
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StoreSearchResultAttributeValue extends GrammarAction<LdapMessageContainer<SearchResultEntryDecorator>>
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( StoreSearchResultAttributeValue.class );

    /**
     * Instantiates a new search result attribute value action.
     */
    public StoreSearchResultAttributeValue()
    {
        super( "Stores AttributeValue" );
    }


    /**
     * {@inheritDoc}
     */
    public void action( LdapMessageContainer<SearchResultEntryDecorator> container )
    {
        SearchResultEntryDecorator searchResultEntry = container.getMessage();

        TLV tlv = container.getCurrentTLV();

        // Store the value
        Object value = null;

        try
        {
            if ( tlv.getLength() == 0 )
            {
                searchResultEntry.addAttributeValue( "" );

                if ( LOG.isDebugEnabled() )
                {
                    LOG.debug( I18n.msg( I18n.MSG_05180_NULL_ATTRIBUTE_VALUE ) );
                }
            }
            else
            {
                if ( container.isBinary( searchResultEntry.getCurrentAttribute().getId() ) )
                {
                    value = tlv.getValue().getData();

                    if ( LOG.isDebugEnabled() )
                    {
                        LOG.debug( I18n.msg( I18n.MSG_05181_ATTRIBUTE_VALUE, Strings.dumpBytes( ( byte[] ) value ) ) );
                    }
                }
                else
                {
                    value = Strings.utf8ToString( tlv.getValue().getData() );

                    if ( LOG.isDebugEnabled() )
                    {
                        LOG.debug( I18n.msg( I18n.MSG_05181_ATTRIBUTE_VALUE, value ) );
                    }
                }

                searchResultEntry.addAttributeValue( value );
            }
        }
        catch ( LdapException le )
        {
            // Just swallow the exception, it can't occur here
        }

        // We can have an END transition
        container.setGrammarEndAllowed( true );
    }
}
