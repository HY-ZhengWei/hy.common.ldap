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
package org.apache.directory.api.dsmlv2.response;


import org.apache.directory.api.ldap.codec.api.LdapApiService;
import org.apache.directory.api.ldap.model.message.MessageTypeEnum;
import org.apache.directory.api.ldap.model.message.SearchResultDone;
import org.apache.directory.api.ldap.model.message.SearchResultDoneImpl;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;


/**
 * DSML Decorator for SearchResultDone
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class SearchResultDoneDsml extends AbstractResultResponseDsml<SearchResultDone>
    implements SearchResultDone
{
    private static final String SEARCH_RESULT_DONE_TAG = "searchResultDone";


    /**
     * Creates a new getDecoratedMessage() of SearchResultDoneDsml.
     * 
     * @param codec The LDAP Service to use
     */
    public SearchResultDoneDsml( LdapApiService codec )
    {
        super( codec, new SearchResultDoneImpl() );
    }


    /**
     * Creates a new getDecoratedMessage() of SearchResultDoneDsml.
     *
     * @param codec The LDAP Service to use
     * @param ldapMessage the message to decorate
     */
    public SearchResultDoneDsml( LdapApiService codec, SearchResultDone ldapMessage )
    {
        super( codec, ldapMessage );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public MessageTypeEnum getType()
    {
        return getDecorated().getType();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Element toDsml( Element root )
    {
        Element element;

        if ( root != null )
        {
            element = root.addElement( SEARCH_RESULT_DONE_TAG );
        }
        else
        {
            element = new DefaultElement( SEARCH_RESULT_DONE_TAG );
        }

        LdapResultDsml ldapResultDsml =
            new LdapResultDsml( getCodecService(), getDecorated().getLdapResult(), getDecorated() );
        
        ldapResultDsml.toDsml( element );

        return element;
    }
}
