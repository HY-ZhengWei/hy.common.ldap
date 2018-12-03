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
package org.apache.directory.api.ldap.extras.controls.ppolicy_impl;


import org.apache.directory.api.asn1.ber.grammar.GrammarAction;
import org.apache.directory.api.i18n.I18n;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The action used to initialize the PasswordPolicyResponseControlContainer object
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class PPolicyInit extends GrammarAction<PasswordPolicyContainer>
{
    /** The logger */
    private static final Logger LOG = LoggerFactory.getLogger( PPolicyInit.class );

    /**
     * Instantiates a new PPolicyInit action.
     */
    public PPolicyInit()
    {
        super( "Initialize the PasswordPolicyResponseControlContainer" );
    }


    /**
     * {@inheritDoc}
     */
    public void action( PasswordPolicyContainer container )
    {
        // As all the values are optional or defaulted, we can end here
        container.setGrammarEndAllowed( true );

        if ( LOG.isDebugEnabled() )
        {
            LOG.debug( I18n.msg( I18n.MSG_08103_PP_RESPONSE_CONTROL_CONTAINER_INITIALIZED ) );
        }
    }
}
