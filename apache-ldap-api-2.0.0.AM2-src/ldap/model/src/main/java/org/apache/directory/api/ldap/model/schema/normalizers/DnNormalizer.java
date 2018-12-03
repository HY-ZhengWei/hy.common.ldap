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
package org.apache.directory.api.ldap.model.schema.normalizers;


import org.apache.directory.api.ldap.model.constants.SchemaConstants;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.api.ldap.model.schema.Normalizer;
import org.apache.directory.api.ldap.model.schema.PrepareString;
import org.apache.directory.api.ldap.model.schema.SchemaManager;


/**
 * Normalizer a Dn
 * 
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
@SuppressWarnings("serial")
public class DnNormalizer extends Normalizer
{
    /** A reference to the schema manager used to normalize the Dn */
    private transient SchemaManager schemaManager;


    /**
     * Empty constructor
     */
    public DnNormalizer()
    {
        super( SchemaConstants.DISTINGUISHED_NAME_MATCH_MR_OID );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String normalize( String value ) throws LdapException
    {
        Dn dn = new Dn( schemaManager, value );

        return dn.getNormName();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String normalize( String value, PrepareString.AssertionType assertionType ) throws LdapException
    {
        Dn dn = new Dn( schemaManager, value );

        return dn.getNormName();
    }


    /**
     * Normalize a Dn
     * @param value The Dn to normalize
     * @return A normalized Dn
     * @throws LdapException If the DN is invalid
     */
    public String normalize( Dn value ) throws LdapException
    {
        Dn dn = value;

        if ( !value.isSchemaAware() )
        {
            dn = new Dn( schemaManager, value );
        }

        return dn.getNormName();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setSchemaManager( SchemaManager schemaManager )
    {
        this.schemaManager = schemaManager;
    }
}
