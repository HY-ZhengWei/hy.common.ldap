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
package org.apache.directory.api.ldap.extras.controls.vlv_impl;


import org.apache.directory.api.asn1.actions.AbstractReadOctetString;


/**
 * The action used to store the contextId value in VLV reponse
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class StoreContextIdResponse extends AbstractReadOctetString<VirtualListViewResponseContainer>
{
    /**
     * Instantiates a new contextId action.
     */
    public StoreContextIdResponse()
    {
        super( "VirtualListViewResponse contextId", true );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected void setOctetString( byte[] value, VirtualListViewResponseContainer vlvContainer )
    {
        vlvContainer.getDecorator().setContextId( value );

        // The last element is optional, we can quit here
        vlvContainer.setGrammarEndAllowed( true );
    }
}
