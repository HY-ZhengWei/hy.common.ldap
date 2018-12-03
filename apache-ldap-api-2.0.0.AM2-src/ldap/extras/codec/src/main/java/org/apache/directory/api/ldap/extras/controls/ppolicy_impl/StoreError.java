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


import org.apache.directory.api.asn1.actions.AbstractReadInteger;
import org.apache.directory.api.asn1.ber.Asn1Container;
import org.apache.directory.api.ldap.extras.controls.ppolicy.PasswordPolicyErrorEnum;


/**
 * The action used to store the error
 * 
 * @param <C> The container type
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StoreError<C extends Asn1Container> extends AbstractReadInteger<C>
{

    /**
     * Instantiates a new StoreError action.
     */
    public StoreError()
    {
        super( "PPolicy error" );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void setIntegerValue( int value, Asn1Container container )
    {
        PasswordPolicyContainer ppolicyContainer = ( PasswordPolicyContainer ) container;

        PasswordPolicyErrorEnum error = PasswordPolicyErrorEnum.get( value );
        ppolicyContainer.getPasswordPolicyResponseControl().getResponse().setPasswordPolicyError( error );

        ppolicyContainer.setGrammarEndAllowed( true );
    }
}
