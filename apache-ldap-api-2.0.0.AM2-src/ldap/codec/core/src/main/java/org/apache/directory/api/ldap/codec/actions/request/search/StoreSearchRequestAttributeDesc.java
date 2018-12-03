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


import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.asn1.ber.tlv.TLV;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.decorators.SearchRequestDecorator;
import org.apache.directory.api.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to store the attribute description
 * <pre>
 * SearchRequest ::= [APPLICATION 3] SEQUENCE {
 *     ...
 *     attributes  AttributeDescriptionList }
 *
 * AttributeDescriptionList ::= SEQUENCE OF
 *     AttributeDescription
 * </pre>
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StoreSearchRequestAttributeDesc extends GrammarAction<LdapMessageContainer<SearchRequestDecorator>>
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( StoreSearchRequestAttributeDesc.class );

    /**
     * Instantiates a new attribute desc action.
     */
    public StoreSearchRequestAttributeDesc()
    {
        super( "Store attribute description" );
    }


    /**
     * {@inheritDoc}
     */
    public void action( LdapMessageContainer<SearchRequestDecorator> container )
    {
        SearchRequestDecorator searchRequestDecorator = container.getMessage();
        TLV tlv = container.getCurrentTLV();
        String attributeDescription = null;

        if ( tlv.getLength() != 0 )
        {
            attributeDescription = Strings.utf8ToString( tlv.getValue().getData() );

            // If the attributeDescription is empty, we won't add it
            if ( !Strings.isEmpty( attributeDescription.trim() ) )
            {
                searchRequestDecorator.getDecorated().addAttributes( attributeDescription );
            }
        }

        // We can have an END transition
        container.setGrammarEndAllowed( true );

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_05159_DECODED_ATT_DESC, attributeDescription ) );
        }
    }
}
