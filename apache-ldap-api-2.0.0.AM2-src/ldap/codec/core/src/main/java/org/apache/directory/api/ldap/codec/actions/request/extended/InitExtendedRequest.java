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
package org.apache.directory.api.ldap.codec.actions.request.extended;


import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.decorators.ExtendedRequestDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to initialize the ExtendedRequest message
 * <pre>
 * LdapMessage ::= ... ExtendedRequest ...
 * ExtendedRequest ::= [APPLICATION 23] SEQUENCE {
 * </pre>
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class InitExtendedRequest extends GrammarAction<LdapMessageContainer<ExtendedRequestDecorator<?>>>
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( InitExtendedRequest.class );


    /**
     * Instantiates a new action.
     */
    public InitExtendedRequest()
    {
        super( "Init ExtendedRequest" );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void action( LdapMessageContainer<ExtendedRequestDecorator<?>> container )
    {
        /*
         * It is the responsibility of the LdapCodecService to instantiate new
         * extended requests and responses. So we must delegate this task over
         * to it instead of creating the requests and responses manually. This
         * is because we use a plugin model that allows us to use specific 
         * types for extended requests and responses rather than using a 
         * generic type.
         * 
         * So we have to wait until we at least get our hands on ExtendedRequest 
         * OID before we can delegate instantiation to the LdapCodecService.
         */

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_05125_EXTENDED_REQUEST_PROCESSED ) );
        }
    }
}
