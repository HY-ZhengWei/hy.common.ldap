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
package org.apache.directory.api.ldap.codec.actions;


import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.ldap.codec.api.LdapMessageContainer;
import org.apache.directory.api.ldap.codec.api.MessageDecorator;
import org.apache.directory.api.ldap.model.message.Message;


/**
 * The action used to indicate that the grammar can terminate after this action
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class AllowGrammarEnd extends GrammarAction<LdapMessageContainer<MessageDecorator<? extends Message>>>
{
    /**
     * Instantiates a new value action.
     */
    public AllowGrammarEnd()
    {
        super( "Allow a grammar end" );
    }


    /**
     * {@inheritDoc}
     */
    public void action( LdapMessageContainer<MessageDecorator<? extends Message>> container )
    {
        container.setGrammarEndAllowed( true );
    }
}
