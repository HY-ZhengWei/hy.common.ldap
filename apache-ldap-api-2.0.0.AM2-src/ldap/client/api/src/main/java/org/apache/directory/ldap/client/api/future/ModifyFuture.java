/*
 * Licensed to the Apache Software Foundation (ASF) under one
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
package org.apache.directory.ldap.client.api.future;

import org.apache.directory.api.ldap.model.message.ModifyResponse;
import org.apache.directory.ldap.client.api.LdapConnection;


/**
 * A Future to manage ModifyRequest.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ModifyFuture extends UniqueResponseFuture<ModifyResponse>
{
    /**
     * Creates a new instance of ModifyFuture.
     *
     * @param connection the LDAP connection
     * @param messageId The associated messageId
     */
    public ModifyFuture( LdapConnection connection, int messageId )
    {
        super( connection, messageId );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append( "ModifyFuture" ).append( super.toString() );

        return sb.toString();
    }
}
