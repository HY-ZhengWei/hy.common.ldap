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
package org.apache.directory.api.ldap.codec.actions.response.search.done;


import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.decorators.SearchResultDoneDecorator;
import org.apache.directory.api.ldap.model.message.SearchResultDoneImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to initialize the SearchResultDone response
 * <pre>
 * LdapMessage ::= ... SearchResultDone ...
 * SearchResultDone ::= [APPLICATION 5] SEQUENCE { ...
 * </pre>
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class InitSearchResultDone extends GrammarAction<LdapMessageContainer<SearchResultDoneDecorator>>
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( InitSearchResultDone.class );


    /**
     * Instantiates a new action.
     */
    public InitSearchResultDone()
    {
        super( "Init SearchResultDone" );
    }


    /**
     * {@inheritDoc}
     */
    public void action( LdapMessageContainer<SearchResultDoneDecorator> container )
    {
        // Now, we can allocate the SearchResultDone Object
        SearchResultDoneDecorator searchResultDone = new SearchResultDoneDecorator(
            container.getLdapCodecService(), new SearchResultDoneImpl( container.getMessageId() ) );
        container.setMessage( searchResultDone );

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_05178_SEARCH_RESULT_DONE ) );
        }
    }
}
