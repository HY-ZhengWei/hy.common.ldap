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
package org.apache.directory.api.ldap.model.schema.registries;


import java.util.Iterator;

import org.apache.directory.api.i18n.I18n;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.exception.LdapUnwillingToPerformException;
import org.apache.directory.api.ldap.model.message.ResultCodeEnum;
import org.apache.directory.api.ldap.model.schema.SchemaObjectType;
import org.apache.directory.api.ldap.model.schema.SyntaxChecker;


/**
 * An immutable wrapper of the SyntaxChecker registry.
 *
 * @author <a href="mailto:dev@directory.apache.org">Apache Directory Project</a>
 */
public class ImmutableSyntaxCheckerRegistry implements SyntaxCheckerRegistry
{
    /** The wrapped SyntaxChecker registry */
    SyntaxCheckerRegistry immutableSyntaxCheckerRegistry;


    /**
     * Creates a new instance of ImmutableSyntaxCheckerRegistry.
     *
     * @param syntaxCheckerRegistry The wrapped SyntaxChecker registry
     */
    public ImmutableSyntaxCheckerRegistry( SyntaxCheckerRegistry syntaxCheckerRegistry )
    {
        immutableSyntaxCheckerRegistry = syntaxCheckerRegistry;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void register( SyntaxChecker syntaxChecker ) throws LdapException
    {
        throw new LdapUnwillingToPerformException( ResultCodeEnum.NO_SUCH_OPERATION, I18n.err( I18n.ERR_13711_CANNOT_MODIFY_SC_REGISTRY_COPY ) );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SyntaxChecker unregister( String numericOid ) throws LdapException
    {
        throw new LdapUnwillingToPerformException( ResultCodeEnum.NO_SUCH_OPERATION, I18n.err( I18n.ERR_13711_CANNOT_MODIFY_SC_REGISTRY_COPY ) );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void unregisterSchemaElements( String schemaName ) throws LdapException
    {
        throw new LdapUnwillingToPerformException( ResultCodeEnum.NO_SUCH_OPERATION, I18n.err( I18n.ERR_13711_CANNOT_MODIFY_SC_REGISTRY_COPY ) );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public ImmutableSyntaxCheckerRegistry copy()
    {
        return ( ImmutableSyntaxCheckerRegistry ) immutableSyntaxCheckerRegistry.copy();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int size()
    {
        return immutableSyntaxCheckerRegistry.size();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains( String oid )
    {
        return immutableSyntaxCheckerRegistry.contains( oid );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getOidByName( String name ) throws LdapException
    {
        return immutableSyntaxCheckerRegistry.getOidByName( name );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public String getSchemaName( String oid ) throws LdapException
    {
        return immutableSyntaxCheckerRegistry.getSchemaName( oid );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SchemaObjectType getType()
    {
        return immutableSyntaxCheckerRegistry.getType();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<SyntaxChecker> iterator()
    {
        return immutableSyntaxCheckerRegistry.iterator();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SyntaxChecker lookup( String oid ) throws LdapException
    {
        return immutableSyntaxCheckerRegistry.lookup( oid );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<String> oidsIterator()
    {
        return immutableSyntaxCheckerRegistry.oidsIterator();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void renameSchema( String originalSchemaName, String newSchemaName ) throws LdapException
    {
        throw new LdapUnwillingToPerformException( ResultCodeEnum.NO_SUCH_OPERATION, I18n.err( I18n.ERR_13711_CANNOT_MODIFY_SC_REGISTRY_COPY ) );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SyntaxChecker get( String oid )
    {
        return immutableSyntaxCheckerRegistry.get( oid );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() throws LdapException
    {
        throw new LdapUnwillingToPerformException( ResultCodeEnum.NO_SUCH_OPERATION, I18n.err( I18n.ERR_13711_CANNOT_MODIFY_SC_REGISTRY_COPY ) );
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public SyntaxChecker unregister( SyntaxChecker schemaObject ) throws LdapException
    {
        throw new LdapUnwillingToPerformException( ResultCodeEnum.NO_SUCH_OPERATION, I18n.err( I18n.ERR_13711_CANNOT_MODIFY_SC_REGISTRY_COPY ) );
    }
}
